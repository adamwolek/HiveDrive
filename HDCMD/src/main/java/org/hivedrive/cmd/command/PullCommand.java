package org.hivedrive.cmd.command;

import static org.hivedrive.cmd.helper.RepoOperationsHelper.getAllFiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.hivedrive.cmd.helper.RepoOperationsHelper;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.model.TempFile;
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

	@Option(names = { "-dir", "--directory" }, description = "It downloads files from the cloud.")
	private File repositoryDirectory = new File(System.getProperty("user.dir"));

	@Option(names = { "-c", "--clean" }, description = "It downloads files from cloud and removes local files whoch aren't in the cloud.")
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
	
	@Autowired
	private RepoOperationsHelper repoOperationsHelper;

	private Set<String> filesInLocalRepo;

	private List<PartTO> allRemoteParts;
	
	@Override
	public void run() {
		StopWatch stopwatch = StopWatch.createStarted();
		repositoryConfigService.setRepositoryDirectory(repositoryDirectory);
		execInTempDirectory(workDirectory -> {
			
			String repository = repositoryConfigService.getConfig().getRepositoryName();
			allRemoteParts = connectionService.getAllPartsForRepository(repository);
			
			if(clean) {
				deleteLocalFilesNonExistingInRemoteRepo();
			}
			downloadMissingFiles(workDirectory);
		});
		logger.info("Pulling finished in " + stopwatch.getTime(TimeUnit.MILLISECONDS) + "millis");
	}

	private void downloadMissingFiles(File workDirectory) {
		filesInLocalRepo = getAllFiles(repositoryDirectory).stream()
				.map(repoOperationsHelper::fileId)
				.collect(Collectors.toSet());
		File directoryForParts = new File(workDirectory, "/parts");
		ImmutableListMultimap<String, PartInfo> groupedParts = allRemoteParts.stream()
		.filter(this::validatePart)
		.filter(this::notExistingInRepo)
		.parallel()
		.map(partTO -> connectionService.downloadPart(partTO, directoryForParts))
		.collect(ImmutableListMultimap.toImmutableListMultimap(
				part -> part.getFileId(), part -> part));
		groupedParts.keySet().stream()
				.map(fileId -> groupedParts.get(fileId))
				.map(parts -> mergeIntoOneFile(parts, workDirectory))
				.map(this::decryptFile)
				.map(this::unpackFile)
				.forEach(rawFile -> {
					logger.info("File " + rawFile.getTempFile().getName() + " downloaded");
				});
	}

	private void deleteLocalFilesNonExistingInRemoteRepo() {
		Set<String> filesInRemoteRepo = allRemoteParts.stream()
		.map(PartTO::getFileId)
		.collect(Collectors.toSet());
		getAllFiles(repositoryDirectory).stream()
		.filter(file -> !filesInRemoteRepo.contains(repoOperationsHelper.fileId(file)))
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


	private TempFile unpackFile(TempFile tempFile) {
		try {
			File rawFile = new File(repositoryConfigService.getRepositoryDirectory(), 
					tempFile.getPartInfo().getFileMetadata().getFilePath());
			rawFile.getParentFile().mkdirs();
			fileComporessingService.uncompressFile(tempFile.getTempFile(), rawFile);
			deleteFile(tempFile.getTempFile());
			tempFile.setTempFile(rawFile);
			return tempFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private TempFile decryptFile(TempFile tempFile) {
		File file = tempFile.getTempFile();
		try {
			File decryptedFile = new File(file.getParentFile(), file.getName() + ".zip");
			encryptionService.decrypt(file, decryptedFile);
			deleteFile(file);
			tempFile.setTempFile(decryptedFile);
			return tempFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private TempFile mergeIntoOneFile(List<PartInfo> parts, File workDirectory) {
		List<PartInfo> sortedParts = new ArrayList<>(parts);
		sortedParts.sort((p1, p2) -> Integer.compare(
					p1.getFileMetadata().getPartIndex(), 
					p2.getFileMetadata().getPartIndex()));
		PartInfo anyPart = Iterables.getFirst(sortedParts, null);
		File wholeFile = declareWholeFile(anyPart, workDirectory);
		try (FileOutputStream mergedFileOS = new FileOutputStream(wholeFile)) {
			for (PartInfo part : sortedParts) {
				try (InputStream partIS = new FileInputStream(part.getPart())) {
					IOUtils.copy(partIS, mergedFileOS);
				}
			}
			TempFile tempFile = new TempFile();
			tempFile.setPartInfo(anyPart);
			tempFile.setTempFile(wholeFile);
			return tempFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private File declareWholeFile(PartInfo anyPart, File workDirectory) {
		File wholeFile = new File(workDirectory, anyPart.getFileId());
		if (wholeFile.exists()) {
			deleteFile(wholeFile);
		}
		return wholeFile;
	}

	private boolean notExistingInRepo(PartTO part) {
		return !filesInLocalRepo.contains(part.getFileId());
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
		if(StringUtils.isBlank(part.getFileId())) {
			return false;
		}
		if(StringUtils.isBlank(part.getRepository())) {
			return false;
		}
		if(StringUtils.isBlank(part.getOwnerId())) {
			return false;
		}
		return true;
	}

}
