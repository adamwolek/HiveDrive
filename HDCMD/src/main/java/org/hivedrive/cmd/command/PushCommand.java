package org.hivedrive.cmd.command;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.hivedrive.cmd.helper.FileNameHelper;
import org.hivedrive.cmd.model.FileMetadata;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.service.ConnectionService;
import org.hivedrive.cmd.service.FileCompresssingService;
import org.hivedrive.cmd.service.FileSplittingService;
import org.hivedrive.cmd.service.RepositoryConfigService;
import org.hivedrive.cmd.service.SignatureService;
import org.hivedrive.cmd.service.SymetricEncryptionService;
import org.hivedrive.cmd.service.UserKeysService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static org.hivedrive.cmd.helper.FileNameHelper.*;

@Lazy
@Component("push")
@Command(name = "push", mixinStandardHelpOptions = true, version = "0.1", description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
public class PushCommand implements Runnable {

	@Option(names = { "-dir", "--directory" }, description = "")
	private File repositoryDirectory = new File(System.getProperty("user.dir"));
	
	private Logger logger = LoggerFactory.getLogger(PushCommand.class);
	
	@Autowired
	public PushCommand(ConnectionService connectionService,
			SymetricEncryptionService encryptionService, FileSplittingService fileSplittingService,
			FileCompresssingService fileComporessingService, SignatureService signatureService,
			UserKeysService userKeysService, RepositoryConfigService repositoryConfigService) {
		super();
		this.connectionService = connectionService;
		this.encryptionService = encryptionService;
		this.fileSplittingService = fileSplittingService;
		this.fileComporessingService = fileComporessingService;
		this.signatureService = signatureService;
		this.userKeysService = userKeysService;
		this.repositoryConfigService = repositoryConfigService;
	}

	private ConnectionService connectionService;
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
			repositoryConfigService.setRepositoryDirectory(repositoryDirectory);
			
			userKeysService.loadKeys();
			
			workDirectory = new File(repositoryConfigService.getRepositoryDirectory(), ".temp");
			workDirectory.mkdir();
			List<PartInfo> parts = generatePartsForRepository();
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
		connectionService.sendParts(parts);
		logger.info("Sent " + parts.size() + " files.");

	}

	private List<PartInfo> generatePartsForRepository() {
		List<PartInfo> parts = new ArrayList<>();
		
		IOFileFilter filter = new IOFileFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return false;
			}
			
			@Override
			public boolean accept(File file) {
				List<String> excludedNames = Arrays.asList(".hivedrive", ".temp");
				if(excludedNames.contains(file.getName())) {
					return false;
				}
				return true;
			}
		};
		
		File directoryToSplit = new File(workDirectory, "/parts");
		directoryToSplit.mkdir();
		Collection<File> allFiles = FileUtils.listFiles(
				repositoryConfigService.getRepositoryDirectory(), filter, filter);
		int sentFiles = 0;
		for (File sourceFile : allFiles) {
			stopWatch = StopWatch.createStarted();
			sentFiles++;
			double currentPercentage = 100 * sentFiles / (double) allFiles.size();
			log(currentPercentage + "%");
			
			File packedFile = packFile(sourceFile);
			File encryptedFile = encryptFile(packedFile);
			packedFile.delete();
			
			List<File> partedFile = splitFileToDirectory(encryptedFile, directoryToSplit);
			encryptedFile.delete();
			
			List<PartInfo> partsFromFile = createPartsObjectsFromFile(partedFile, sourceFile);
			parts.addAll(partsFromFile);
			logger.info("File " + sentFiles + " splitted into " + partsFromFile.size() + " parts");
		}
		
		
		return parts;
	}

	private List<PartInfo> createPartsObjectsFromFile(List<File> partedFile, File sourceFile) {
		String fileId = repositoryConfigService.getRepositoryDirectory().toURI().relativize(sourceFile.toURI())
				.getPath();
		return partedFile.stream().map(partOfFile -> {
			PartInfo partInfo = new PartInfo();
			partInfo.setPart(partOfFile);
			partInfo.setOwnerPublicKey(userKeysService.getKeys().getPublicAsymetricKeyAsString());

			String fileSign = signatureService.signByClient(partOfFile);
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
