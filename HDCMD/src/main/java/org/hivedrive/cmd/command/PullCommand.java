package org.hivedrive.cmd.command;

import static org.hivedrive.cmd.helper.FileNameHelper.changeDirectory;
import static org.hivedrive.cmd.helper.FileNameHelper.removeExtension;
import static org.hivedrive.cmd.helper.LocalRepoOperationsHelper.fileHash;
import static org.hivedrive.cmd.helper.LocalRepoOperationsHelper.getAllFiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.service.C2NConnectionService;
import org.hivedrive.cmd.service.FileCompresssingService;
import org.hivedrive.cmd.service.RepositoryConfigService;
import org.hivedrive.cmd.service.SymetricEncryptionService;
import org.hivedrive.cmd.to.PartTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Lazy
@Component("pull")
@Command(name = "pull", mixinStandardHelpOptions = true, version = "0.1", description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
public class PullCommand implements Runnable {

	@Option(names = { "-dir", "--directory" }, description = "")
	private File repositoryDirectory = new File(System.getProperty("user.dir"));

	@Option(names = { "-c", "--clean" }, description = "")
	private boolean clean;
	
	private Logger logger = LoggerFactory.getLogger(PullCommand.class);

	@Autowired
	private C2NConnectionService connectionService;
	
	@Autowired
	private SymetricEncryptionService encryptionService;
	
	@Autowired
	private FileCompresssingService fileComporessingService;
	
	@Autowired
	private RepositoryConfigService repositoryConfigService;

	private Set<String> hashesInLocalRepo;

	private List<PartTO> allRemoteParts;
	
	@Override
	public void run() {
		repositoryConfigService.setRepositoryDirectory(repositoryDirectory);
		execInTempDirectory(workDirectory -> {
			
			String repository = repositoryConfigService.getConfig().getRepositoryName();
			allRemoteParts = connectionService.getAllPartsForRepository(repository);
			
			if(clean) {
				deleteLocalFilesNonExistingInRemoteRepo();
			}
			downloadMissingFiles(workDirectory);
		});
		logger.info("Pulling finished");
	}

	private void downloadMissingFiles(File workDirectory) {
		hashesInLocalRepo = getAllFiles(repositoryDirectory).stream()
				.map(file -> fileHash(file))
				.collect(Collectors.toSet());
		File directoryForParts = new File(workDirectory, "/parts");
		ImmutableListMultimap<String, PartInfo> groupedParts = allRemoteParts.stream()
		.filter(this::validatePart)
		.filter(this::notExistingInRepo)
		.map(partTO -> connectionService.downloadPart(partTO, directoryForParts))
		.collect(ImmutableListMultimap.toImmutableListMultimap(
				part -> part.getFileMetadata().getFileId(), part -> part));
		groupedParts.keySet().stream()
				.map(fileId -> groupedParts.get(fileId))
				.map(parts -> mergeIntoOneFile(parts, workDirectory))
				.map(this::decryptFile)
				.map(this::unpackFile)
				.forEach(rawFile -> {
					logger.info("File " + rawFile.getName() + " downloaded");
				});
	}

	private void deleteLocalFilesNonExistingInRemoteRepo() {
		Set<String> hashesInRemoteRepo = allRemoteParts.stream()
		.map(PartTO::getFileHash)
		.collect(Collectors.toSet());
		getAllFiles(repositoryDirectory).stream()
		.filter(file -> !hashesInRemoteRepo.contains(fileHash(file)))
		.forEach(file -> {
			deleteFile(file);
		});
	}
	
	private void deleteFile(File localFile) {
		try {
			FileUtils.forceDelete(localFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void execInTempDirectory(Consumer<File> actionInTempDirectory) {
		File workDirectory = new File(repositoryConfigService.getRepositoryDirectory(), ".temp");
		workDirectory.mkdir();
		try {
			actionInTempDirectory.accept(workDirectory);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			deleteFile(workDirectory);
		}
	}


	private File unpackFile(File source) {
		try {
			File rawFile = changeDirectory(removeExtension(source), 
					repositoryConfigService.getRepositoryDirectory());
			fileComporessingService.uncompressFile(source, rawFile);
			deleteFile(source);
			return rawFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private File decryptFile(File source) {
		try {
			File decryptedFile = new File(source.getParentFile(), source.getName() + ".zip");
			encryptionService.decrypt(source, decryptedFile);
			deleteFile(source);
			return decryptedFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private File mergeIntoOneFile(List<PartInfo> parts, File workDirectory) {
		PartInfo anyPart = Iterables.getFirst(parts, null);
		File wholeFile = declareWholeFile(anyPart, workDirectory);
		try (FileOutputStream mergedFileOS = new FileOutputStream(wholeFile)) {
			for (PartInfo part : parts) {
				try (InputStream partIS = new FileInputStream(part.getPart())) {
					IOUtils.copy(partIS, mergedFileOS);
				}
			}
			return wholeFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private File declareWholeFile(PartInfo anyPart, File workDirectory) {
		File wholeFile = new File(workDirectory, anyPart.getFileMetadata().getFileId());
		if (wholeFile.exists()) {
			deleteFile(wholeFile);
		}
		return wholeFile;
	}

	private boolean notExistingInRepo(PartTO part) {
		return !hashesInLocalRepo.contains(part.getFileHash());
	}

	private boolean validatePart(PartTO part) {
		if(areAllFieldsFilled(part)) {
			return true;
		} else {
			logger.warn("W obiekcie PartTO brakuje części danych");
			return false;
		}
	}
	
	private boolean areAllFieldsFilled(PartTO part) {
		if(StringUtils.isBlank(part.getFileHash())) {
			return false;
		}
		if(StringUtils.isBlank(part.getGlobalId())) {
			return false;
		}
		if(StringUtils.isBlank(part.getRepository())) {
			return false;
		}
		if(StringUtils.isBlank(part.getGroupId())) {
			return false;
		}
		if(StringUtils.isBlank(part.getOwnerId())) {
			return false;
		}
		return true;
	}

}
