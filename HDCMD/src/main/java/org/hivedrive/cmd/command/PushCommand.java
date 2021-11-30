package org.hivedrive.cmd.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.crypto.SecretKey;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.hivedrive.cmd.model.FileMetadata;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.model.RepositoryConfigFileData;
import org.hivedrive.cmd.model.UserKeys;
import org.hivedrive.cmd.service.ConnectionService;
import org.hivedrive.cmd.service.FileCompresssingService;
import org.hivedrive.cmd.service.FileSplittingService;
import org.hivedrive.cmd.service.RepositoryConfigService;
import org.hivedrive.cmd.service.SignatureService;
import org.hivedrive.cmd.service.SymetricEncryptionService;
import org.hivedrive.cmd.service.UserKeysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;

@Component("push")
@Command(name = "push", mixinStandardHelpOptions = true, version = "0.1",
         description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
public class PushCommand implements Runnable {

	
	@Autowired
	private ConnectionService connectionService;

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

    
	private StopWatch stopWatch;

	private File workDirectory;
    
    @Override
	public void run() {
    	UserKeys keys = userKeysService.getKeys();
    	
		if(repositoryConfigService.getRepositoryDirectory().exists()) {
			workDirectory = new File(repositoryConfigService.getRepositoryDirectory(), ".temp");
			workDirectory.mkdir();
			List<PartInfo> parts = generatePartsForRepository();
			sendParts(parts);
		}
		
	}

	private void sendParts(List<PartInfo> parts) {
		connectionService.sendParts(parts);
		
	}

	private List<PartInfo> generatePartsForRepository() {
		List<PartInfo> parts = new ArrayList<>();
		Collection<File> allFiles = FileUtils.listFiles(
				repositoryConfigService.getRepositoryDirectory(), 
				TrueFileFilter.TRUE, 
				TrueFileFilter.TRUE);
		int sentFiles = 0;
		for (File sourceFile : allFiles) {
			stopWatch = StopWatch.createStarted();
			sentFiles++;
			double currentPercentage = 100 * sentFiles / (double)allFiles.size();
			log(currentPercentage + "%");
			File packedFile = packFile(sourceFile);
			File encryptedFile = encryptFile(packedFile);
			packedFile.delete();
			File directoryWithSplittedFiles = splitFiles(encryptedFile);
			encryptedFile.delete();
			parts.addAll(generatePartsFromFile(directoryWithSplittedFiles, sourceFile));
		}
		return parts;
	}

    private List<PartInfo> generatePartsFromFile(File directoryWithSplittedFiles, 
    		File sourceFile) {
    	List<PartInfo> parts = new ArrayList<>();
    	String fileId = repositoryConfigService.getRepositoryDirectory().toURI()
    			.relativize(sourceFile.toURI()).getPath();
    	File[] files = directoryWithSplittedFiles.listFiles();
    	for (File part : files) {
    		
    		PartInfo partInfo = new PartInfo();
    		partInfo.setPart(part);
    		partInfo.setOwnerPublicKey(
    				userKeysService.getKeys().getPublicAsymetricKeyAsString());
    		
    		String fileSign = signatureService.signByClient(part);
    		partInfo.setFileSign(fileSign);
    		
    		FileMetadata metadata = createFileMetadata(fileId, part);
    		partInfo.setFileMetadata(metadata);
    		String encrypedFileMetadata = encryptionService.encrypt(metadata.toJSON());
    		partInfo.setEncryptedFileMetadata(encrypedFileMetadata);
    		
    		parts.add(partInfo);
		}
    	return parts;
		
	}

	private FileMetadata createFileMetadata(String fileId, File file) {
		FileMetadata metadata = new FileMetadata();
		metadata.setRepository(repositoryConfigService.getConfig().getRepositoryName());
		metadata.setFileId(fileId);
		Integer partIndex = Integer.valueOf(
				FilenameUtils.getExtension(file.getName()));
		metadata.setPartIndex(partIndex);
		return metadata;
	}

	private File splitFiles(File encryptedFile) {
		log(stopWatch.formatTime() + " - Splitting");
    	File directory = encryptedFile.getParentFile();
    	File directoryToSplit = new File(directory.getAbsolutePath() + "/parts");
    	directoryToSplit.mkdir();
		fileSplittingService.splitFileIntoDirectory(encryptedFile, directoryToSplit);
		return directoryToSplit;
	}

	private File packFile(File file) {
		log(stopWatch.formatTime() + " - Packing");
		File zip = new File(StringUtils.substringBefore(file.getAbsolutePath(), ".") + ".zip");
		fileComporessingService.compressFile(file, zip);
		return zip;
	}


	private File encryptFile(File file) {
		log(stopWatch.formatTime() + " - Encrypting");
		try {
			File encryptedFile = new File(file.getAbsolutePath() + ".enc");
			encryptionService.encrypt(file, encryptedFile);
			return encryptedFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	private void log(String message) {
		System.out.println(message);
		
	}


}
