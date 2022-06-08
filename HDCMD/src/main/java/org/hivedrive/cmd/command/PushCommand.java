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
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.time.StopWatch;
import org.hivedrive.cmd.model.FileMetadata;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.model.TempFile;
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
import org.springframework.util.DigestUtils;

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
			repositoryConfigService.setRepositoryDirectory(repositoryDirectory);
			workDirectory = new File(repositoryConfigService.getRepositoryDirectory(), ".temp");
			workDirectory.mkdir();
			
			getAllFiles().stream()
			.filter(file -> !connectionService.isFilePushedAlready(file))
			.map(TempFile::new)
			.map(this::packFile)
			.map(this::encryptFile)
			.flatMap(encryptedFile -> splitFileToDirectory(encryptedFile, new File(workDirectory, "/parts")))
			.map(file -> createPartsObjectsFromFile(file.getTempFile(), fileId(file), getHash(file)))
			.forEach(part -> connectionService.sendPart(part));
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


	private String fileId(TempFile file) {
		String fileId = repositoryConfigService.getRepositoryDirectory()
				.toURI().relativize(file.getTempFile().toURI()).getPath();
		return fileId;
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

	private PartInfo createPartsObjectsFromFile(File partOfFile, String fileId, String hash) {
		PartInfo partInfo = new PartInfo();
		partInfo.setPart(partOfFile);
		partInfo.setOwnerPublicKey(userKeysService.getKeys().getPublicAsymetricKeyAsString());

		String fileSign = signatureService.signFileUsingDefaultKeys(partOfFile);
		partInfo.setFileSign(fileSign);
		
		partInfo.setFileHash(hash);
		
		FileMetadata metadata = createFileMetadata(fileId, partOfFile);
		partInfo.setFileMetadata(metadata);
		String encrypedFileMetadata = encryptionService.encrypt(metadata.toJSON());
		partInfo.setEncryptedFileMetadata(encrypedFileMetadata);
		return partInfo;
	}

	private String getHash(TempFile file) {
		try {
			return DigestUtils.md5DigestAsHex(FileUtils.readFileToByteArray(file.getTempFile()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private FileMetadata createFileMetadata(String fileId, File file) {
		FileMetadata metadata = new FileMetadata();
		metadata.setRepository(repositoryConfigService.getConfig().getRepositoryName());
		metadata.setFileId(fileId);
		Integer partIndex = Integer.valueOf(FilenameUtils.getExtension(file.getName()));
		metadata.setPartIndex(partIndex);
		return metadata;
	}

	private Stream<TempFile> splitFileToDirectory(TempFile encryptedFile, File directoryToSplit) {
		try {
			directoryToSplit.mkdir();
			log(stopWatch.formatTime() + " - Splitting");
			Stream<TempFile> spliitedFile = fileSplittingService
					.splitFileIntoDirectory(encryptedFile.getTempFile(), directoryToSplit).stream()
					.map(part -> {
						TempFile partFile = encryptedFile.clone();
						partFile.setTempFile(part);
						return partFile;
					});
			FileUtils.forceDelete(encryptedFile.getTempFile());
			return spliitedFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private TempFile packFile(TempFile tempFile) {
		try {
			log(stopWatch.formatTime() + " - Packing");
			File file = tempFile.getTempFile();
			File zip = changeDirectory(changeExtension(file, "zip"), workDirectory);
			fileComporessingService.compressFile(file, zip);
			FileUtils.forceDelete(file);
			tempFile.setTempFile(zip);
			return tempFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private TempFile encryptFile(TempFile tempFile) {
		log(stopWatch.formatTime() + " - Encrypting");
		try {
			File file = tempFile.getTempFile();
			File encryptedFile = addExtension(file, "enc");
			encryptionService.encrypt(file, encryptedFile);
			FileUtils.forceDelete(file);
			tempFile.setTempFile(encryptedFile);
			return tempFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private void log(String message) {
//		System.out.println(message);
	}

}
