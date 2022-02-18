package org.hivedrive.cmd.command;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.Nullable;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import static org.hivedrive.cmd.helper.FileNameHelper.*;


@Lazy
@Component("pull")
@Command(name = "pull", mixinStandardHelpOptions = true, version = "0.1", description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
public class PullCommand implements Runnable {

	@Option(names = { "-dir", "--directory" }, description = "")
	private File repositoryDirectory = new File(System.getProperty("user.dir"));
	
	private Logger logger = LoggerFactory.getLogger(PullCommand.class);

	private File workDirectory;
	
	@Autowired
	public PullCommand(ConnectionService connectionService,
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

	
	@Override
	public void run() {
		try {
			repositoryConfigService.setRepositoryDirectory(repositoryDirectory);
			
			userKeysService.loadKeys();
			
			workDirectory = new File(repositoryConfigService.getRepositoryDirectory(), ".temp");
			workDirectory.mkdir();
			List<File> downloadedParts = downloadParts();
			logger.info("Downloaded " + downloadedParts.size() + " files");
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


	private List<File> downloadParts() {
		ImmutableListMultimap<String, PartInfo> groupedParts = Multimaps.index(connectionService.downloadParts(workDirectory), part -> part.getFileMetadata().getFileId());
//		for (String fileId : groupedParts.keys()) {
//			ImmutableList<PartInfo> parts = groupedParts.get(fileId);
//			File encryptedFile = mergeIntoOneFile(parts);
//			File packedFile = decryptFile(encryptedFile);
//			File rawFile = unpackFile(packedFile);
//		}
		return groupedParts.keys().stream()
		.map(fileId -> groupedParts.get(fileId))
		.map(this::mergeIntoOneFile)
		.map(this::decryptFile)
		.map(this::unpackFile)
		.collect(Collectors.toList());
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
	
	private File mergeIntoOneFile(List<PartInfo> parts) {
		PartInfo anyPart = Iterables.getFirst(parts, null);
//		if(true) {
//			anyPart.getPart();
//		}
		File wholeFile = declareWholeFile(anyPart);
		try (FileOutputStream mergedFileOS = new FileOutputStream(wholeFile)){
			for (PartInfo part : parts) {
				try (InputStream partIS = new FileInputStream(part.getPart())){
					IOUtils.copy(partIS, mergedFileOS);
					
				} 
			}
			return wholeFile;
		} catch (Exception e) {
			logger.error("Error:", e);
			return null;
		}
	}


	private File declareWholeFile(PartInfo anyPart) {
		File wholeFile = new File(workDirectory, anyPart.getFileMetadata().getFileId());
		if(wholeFile.exists()) {
			try {
				FileUtils.forceDelete(wholeFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return wholeFile;
	}
	
	
}
