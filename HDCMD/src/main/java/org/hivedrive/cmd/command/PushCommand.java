package org.hivedrive.cmd.command;

import static org.hivedrive.cmd.helper.FileNameHelper.addExtension;
import static org.hivedrive.cmd.helper.FileNameHelper.changeDirectory;
import static org.hivedrive.cmd.helper.FileNameHelper.changeExtension;
import static org.hivedrive.cmd.helper.RepoOperationsHelper.getAllFiles;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hivedrive.cmd.helper.RepoOperationsHelper;
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

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Lazy
@Component("push")
@Command(name = "push", mixinStandardHelpOptions = true, version = "0.1", description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
public class PushCommand implements Runnable {

	@Option(names = { "-dir", "--directory" }, description = "")
	private File repositoryDirectory = new File(System.getProperty("user.dir"));

	@Option(names = { "-c", "--clean" }, description = "")
	private boolean clean;
	
	private Logger logger = LoggerFactory.getLogger(PushCommand.class);

	@Autowired
	private C2NConnectionService connectionService;
	
	@Autowired
	private SymetricEncryptionService encryptionService;
	
	@Autowired
	private FileSplittingService fileSplittingService;
	
	@Autowired
	private FileCompresssingService fileComporessingService;
	
	@Autowired
	private SignatureService signatureService;
	
	@Autowired
	private UserKeysService userKeysService;
	
	@Autowired
	private RepositoryConfigService repositoryConfigService;
	
	@Autowired
	private RepoOperationsHelper repoOperationsHelper;

	@Override
	public void run() {
		repositoryConfigService.setRepositoryDirectory(repositoryDirectory);
		execInTempDirectory(workDirectory -> {
			int howManyFilesDeleted = clean ? deleteRemoteFilesNonExistingLocally() : 0;
			int howManyOriginFilesSent = sendFilesFromRepository(workDirectory);
			log(howManyFilesDeleted, howManyOriginFilesSent);
		});
	}

	private void log(int howManyFilesDeleted, int howManyOriginFilesSent) {
		if(howManyFilesDeleted > 0) {
			logger.info("Deleted " + howManyFilesDeleted + " files");
		}
		if(howManyOriginFilesSent > 0) {
			logger.info("Sent " + howManyOriginFilesSent + " files");
		}
		if(howManyFilesDeleted == 0 && howManyOriginFilesSent == 0) {
			logger.info("All files in network are up-to-date");
		}
	}

	private int sendFilesFromRepository(File workDirectory) {
		AtomicLong howManyOriginFilesSent = new AtomicLong();
		RepoOperationsHelper.getAllFiles(this.repositoryDirectory).stream()
		.filter(file -> !connectionService.isFilePushedAlready(file))
		.map(TempFile::new)
		.map(file -> this.packFile(file, workDirectory))
		.map(this::encryptFile)
		.flatMap(encryptedFile -> { 
			howManyOriginFilesSent.incrementAndGet();
			return splitFileToDirectory(encryptedFile, new File(workDirectory, "/parts"));
		})
		.map(file -> createPartsObjectsFromFile(file))
		.forEach(connectionService::sendPart);
		return howManyOriginFilesSent.intValue();
	}

	private int deleteRemoteFilesNonExistingLocally() {
		Set<String> deletedFiles = new HashSet<>();
		Set<String> localRepoFiles = getAllFiles(this.repositoryDirectory).stream()
		.map(repoOperationsHelper::fileId)
		.collect(Collectors.toSet());
		
		String repository = repositoryConfigService.getConfig().getRepositoryName();
		connectionService.getAllPartsForRepository(repository).stream()
		.filter(part -> !localRepoFiles.contains(part.getFileId()))
		.forEach(part -> {
			if(connectionService.deletePartWithContent(part)) {
				deletedFiles.add(part.getFileId());
			}
		});
		return deletedFiles.size();
	}
	
	private void execInTempDirectory(Consumer<File> actionInTempDirectory) {
		File workDirectory = new File(repositoryConfigService.getRepositoryDirectory(), ".temp");
		workDirectory.mkdir();
		try {
			actionInTempDirectory.accept(workDirectory);
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

	private PartInfo createPartsObjectsFromFile(TempFile tempFile) {
		PartInfo partInfo = new PartInfo();
		partInfo.setPart(tempFile.getTempFile());
		partInfo.setOwnerPublicKey(userKeysService.getKeys().getPublicAsymetricKeyAsString());

		String fileSign = signatureService.signFileUsingDefaultKeys(tempFile.getOriginFile());
		partInfo.setFileSign(fileSign);
		
		FileMetadata metadata = createFileMetadata(tempFile);
		partInfo.setFileMetadata(metadata);
		String encrypedFileMetadata = encryptionService.encrypt(metadata.toJSON());
		partInfo.setEncryptedFileMetadata(encrypedFileMetadata);
		partInfo.setFileId(repoOperationsHelper.fileId(tempFile.getOriginFile()));
		return partInfo;
	}

	private FileMetadata createFileMetadata(TempFile tempFile) {
		FileMetadata metadata = new FileMetadata();
		metadata.setRepository(repositoryConfigService.getConfig().getRepositoryName());
		metadata.setFilePath(repoOperationsHelper.filePath(tempFile.getOriginFile()));
		Integer partIndex = Integer.valueOf(FilenameUtils.getExtension(tempFile.getTempFile().getName()));
		metadata.setPartIndex(partIndex);
		return metadata;
	}

	private Stream<TempFile> splitFileToDirectory(TempFile encryptedFile, File directoryToSplit) {
		try {
			directoryToSplit.mkdir();
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

	private TempFile packFile(TempFile tempFile, File workDirectory) {
		try {
			File originFile = tempFile.getOriginFile();
			File zip = changeDirectory(changeExtension(originFile, "zip"), workDirectory);
			fileComporessingService.compressFile(originFile, zip);
			tempFile.setTempFile(zip);
			return tempFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private TempFile encryptFile(TempFile tempFile) {
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

}
