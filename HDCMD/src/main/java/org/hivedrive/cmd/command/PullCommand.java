package org.hivedrive.cmd.command;

import static org.hivedrive.cmd.helper.FileNameHelper.changeDirectory;
import static org.hivedrive.cmd.helper.FileNameHelper.removeExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hivedrive.cmd.helper.LocalRepoOperationsHelper;
import org.hivedrive.cmd.model.FileMetadata;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.service.C2NConnectionService;
import org.hivedrive.cmd.service.FileCompresssingService;
import org.hivedrive.cmd.service.FileSplittingService;
import org.hivedrive.cmd.service.RepositoryConfigService;
import org.hivedrive.cmd.service.SymetricEncryptionService;
import org.hivedrive.cmd.service.common.SignatureService;
import org.hivedrive.cmd.service.common.UserKeysService;
import org.hivedrive.cmd.session.P2PSession;
import org.hivedrive.cmd.to.PartTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Lazy
@Component("pull")
@Command(name = "pull", mixinStandardHelpOptions = true, version = "0.1", description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
public class PullCommand implements Runnable {

	@Option(names = { "-dir", "--directory" }, description = "")
	private File repositoryDirectory = new File(System.getProperty("user.dir"));

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
	
	@Override
	public void run() {
		repositoryConfigService.setRepositoryDirectory(repositoryDirectory);
		
		hashesInLocalRepo = LocalRepoOperationsHelper.getAllFiles(repositoryDirectory).stream()
		.map(LocalRepoOperationsHelper::fileHash)
		.collect(Collectors.toSet());
		
		execInTempDirectory(workDirectory -> {
			String repository = repositoryConfigService.getConfig().getRepositoryName();
			File directoryForParts = new File(workDirectory, "/parts");
			ImmutableListMultimap<String, PartInfo> groupedParts = connectionService
					.getAllPartsForRepository(repository).stream()
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
		});
		logger.info("Pulling finished");
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


	private File unpackFile(File source) {
		try {
			File rawFile = changeDirectory(removeExtension(source), 
					repositoryConfigService.getRepositoryDirectory());
			fileComporessingService.uncompressFile(source, rawFile);
			FileUtils.forceDelete(source);
			return rawFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private File decryptFile(File source) {
		try {
			File decryptedFile = new File(source.getParentFile(), source.getName() + ".zip");
			encryptionService.decrypt(source, decryptedFile);
			FileUtils.forceDelete(source);
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
			try {
				FileUtils.forceDelete(wholeFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
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
