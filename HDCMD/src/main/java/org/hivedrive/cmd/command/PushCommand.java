package org.hivedrive.cmd.command;

import static org.hivedrive.cmd.helper.FileNameHelper.addExtension;

import static org.hivedrive.cmd.helper.FileNameHelper.changeDirectory;
import static org.hivedrive.cmd.helper.FileNameHelper.changeExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.time.StopWatch;
import org.hivedrive.cmd.model.FileMetadata;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.service.C2NConnectionService;
import org.hivedrive.cmd.service.FileCompresssingService;
import org.hivedrive.cmd.service.FileSplittingService;
import org.hivedrive.cmd.service.RepositoryConfigService;
import org.hivedrive.cmd.service.SymetricEncryptionService;
import org.hivedrive.cmd.service.common.SignatureService;
import org.hivedrive.cmd.service.common.UserKeysService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Lazy
@Component("push")
@Command(name = "push", mixinStandardHelpOptions = true, version = "0.1", description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
public class PushCommand implements Runnable {

	@Option(names = { "-dir", "--directory" }, description = "")
	private File repositoryDirectory = new File(System.getProperty("user.dir"));

	private Logger logger = LoggerFactory.getLogger(PushCommand.class);

	@Autowired
	public PushCommand(C2NConnectionService connectionService, SymetricEncryptionService encryptionService,
			FileSplittingService fileSplittingService, FileCompresssingService fileComporessingService,
			SignatureService signatureService, UserKeysService userKeysService,
			RepositoryConfigService repositoryConfigService) {
		super();
		this.connectionService = connectionService;
		this.encryptionService = encryptionService;
		this.fileSplittingService = fileSplittingService;
		this.fileComporessingService = fileComporessingService;
		this.signatureService = signatureService;
		this.userKeysService = userKeysService;
		this.repositoryConfigService = repositoryConfigService;
	}

	private C2NConnectionService connectionService;
	private SymetricEncryptionService encryptionService;
	private FileSplittingService fileSplittingService;
	private FileCompresssingService fileComporessingService;
	private SignatureService signatureService;
	private UserKeysService userKeysService;
	private RepositoryConfigService repositoryConfigService;

	private StopWatch stopWatch;

	private File workDirectory;

	@Override
	public void run() {
		try {
			logger.info("Repository: " + repositoryDirectory.getAbsolutePath());
			Collection<File> allFiles = getAllFiles();
			Collection<File> filesToPush = getAllFiles().stream()
				.filter(file -> !connectionService.isFilePushedAlready(file))
				.collect(Collectors.toList());
			repositoryConfigService.setRepositoryDirectory(repositoryDirectory);

			workDirectory = new File(repositoryConfigService.getRepositoryDirectory(), ".temp");
			workDirectory.mkdir();
			List<PartInfo> parts = generatePartsForRepository(filesToPush);
			sendParts(parts);
			logger.info("Sending finished");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				FileUtils.forceDelete(workDirectory);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendParts(List<PartInfo> parts) {
		//TODO: sprawdzić czy można wysylac pliki - tj. czy odpowiednia ilosc miejsca jest mozliwa do zapisu
		connectionService.sendParts(parts);
		logger.info("Sent " + parts.size() + " files.");
	}

	private List<PartInfo> generatePartsForRepository(Collection<File> filesToPush) throws NoSuchFileException, DirectoryNotEmptyException, IOException {
		List<PartInfo> parts = new ArrayList<>();

		File directoryToSplit = new File(workDirectory, "/parts");
		directoryToSplit.mkdir();

		int sentFiles = 0;
		for (File sourceFile : filesToPush) {
			stopWatch = StopWatch.createStarted();
			sentFiles++;
			double currentPercentage = 100 * sentFiles / (double) filesToPush.size();
			log(currentPercentage + "%");

			File packedFile = packFile(sourceFile);
			File encryptedFile = encryptFile(packedFile);
			Files.delete(packedFile.toPath());

			List<File> partedFile = splitFileToDirectory(encryptedFile, directoryToSplit);
			Files.delete(encryptedFile.toPath());

			List<PartInfo> partsFromFile = createPartsObjectsFromFile(partedFile, sourceFile);
			parts.addAll(partsFromFile);
			logger.info("File " + sentFiles + " splitted into " + partsFromFile.size() + " parts");
		}

		return parts;
	}

	private Collection<File> getAllFiles() {
		IOFileFilter filter = new IOFileFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return false;
			}

			@Override
			public boolean accept(File file) {
				List<String> excludedNames = Arrays.asList(".hivedrive", ".temp");
				return !(excludedNames.contains(file.getName()));
			}
		};
		
		Collection<File> allFiles = FileUtils.listFiles(this.repositoryDirectory, filter,
				filter);
		return allFiles;
	}

	private List<PartInfo> createPartsObjectsFromFile(List<File> partedFile, File sourceFile) {
		String fileId = repositoryConfigService.getRepositoryDirectory().toURI().relativize(sourceFile.toURI())
				.getPath();
		return partedFile.stream().map(partOfFile -> {
			PartInfo partInfo = new PartInfo();
			partInfo.setPart(partOfFile);
			partInfo.setOwnerPublicKey(userKeysService.getKeys().getPublicAsymetricKeyAsString());

			String fileSign = signatureService.signFileUsingDefaultKeys(partOfFile);
			partInfo.setFileSign(fileSign);

			FileMetadata metadata = createFileMetadata(fileId, partOfFile);
			partInfo.setFileMetadata(metadata);
			String encrypedFileMetadata = encryptionService.encrypt(metadata.toJSON());
			partInfo.setEncryptedFileMetadata(encrypedFileMetadata);
			return partInfo;
		}).collect(Collectors.toList());
	}

	private FileMetadata createFileMetadata(String fileId, File file) {
		FileMetadata metadata = new FileMetadata();
		metadata.setRepository(repositoryConfigService.getConfig().getRepositoryName());
		metadata.setFileId(fileId);
		Integer partIndex = Integer.valueOf(FilenameUtils.getExtension(file.getName()));
		metadata.setPartIndex(partIndex);
		return metadata;
	}

	private List<File> splitFileToDirectory(File encryptedFile, File directoryToSplit) {
		log(stopWatch.formatTime() + " - Splitting");
		return fileSplittingService.splitFileIntoDirectory(encryptedFile, directoryToSplit);
	}

	private File packFile(File file) {
		log(stopWatch.formatTime() + " - Packing");
		File zip = changeDirectory(changeExtension(file, "zip"), workDirectory);
		fileComporessingService.compressFile(file, zip);
		return zip;
	}

	private File encryptFile(File file) {
		log(stopWatch.formatTime() + " - Encrypting");
		try {
			File encryptedFile = addExtension(file, "enc");
			encryptionService.encrypt(file, encryptedFile);
			return encryptedFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private void log(String message) {
//		System.out.println(message);
	}

}
