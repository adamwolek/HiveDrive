package org.hivedrive.cmd.command.outdated;

import picocli.CommandLine;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.hivedrive.cmd.model.FileMetadata;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.model.UserKeys;
import org.hivedrive.cmd.service.FileCompresssingService;
import org.hivedrive.cmd.service.FileSplittingService;
import org.hivedrive.cmd.service.SignatureService;
import org.hivedrive.cmd.service.SymetricEncryptionService;
import org.hivedrive.cmd.service.UserKeysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Command(name = "synchronize", mixinStandardHelpOptions = true, version = "0.1",
         description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
class SynchronizeCommand implements Runnable {

    @Parameters(index = "0", description = "The directory which will be synchronized.")
    private String task;

    @Option(names = {"-dir", "--directoryToSynchronize"}, description = "")
    private File directoryToSynchronize;
    
    @Option(names = {"-rId", "--repositoryId"}, description = "")
    private String repositoryId = "main";
    
    @Option(names = {"-k", "--key"}, description = "")
    private File key;
    
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

	private StopWatch stopWatch;
    
    @Override
	public void run() {
    	UserKeys keys = userKeysService.generateNewKeys();
		SecretKey key = keys.getPrivateSymetricKey();
    	
		if(directoryToSynchronize.exists()) {
			Collection<File> allFiles = FileUtils.listFiles(
					directoryToSynchronize, 
					TrueFileFilter.TRUE, 
					TrueFileFilter.TRUE);
			log("Synchronizing " + allFiles.size() + " files");
			int sentFiles = 0;
			List<PartInfo> parts = new ArrayList<>();
			for (File sourceFile : allFiles) {
				stopWatch = StopWatch.createStarted();
				sentFiles++;
				double currentPercentage = 100 * sentFiles / (double)allFiles.size();
				log(currentPercentage + "%");
				File packedFile = packFile(sourceFile);
				File encryptedFile = encryptFile(packedFile, key);
				packedFile.delete();
				File directoryWithSplittedFiles = splitFiles(encryptedFile);
				encryptedFile.delete();
				parts.addAll(generatePartsInfo(directoryWithSplittedFiles, sourceFile, keys));
				log(stopWatch.formatTime() + " - End");
				
			}
		}
		
	}

    private List<PartInfo> generatePartsInfo(File directoryWithSplittedFiles, 
    		File sourceFile, UserKeys keys) {
    	log(stopWatch.formatTime() + " - Signing");
    	List<PartInfo> parts = new ArrayList<>();
    	String fileId = directoryToSynchronize.toURI()
    			.relativize(sourceFile.toURI()).getPath();
    	File[] files = directoryWithSplittedFiles.listFiles();
    	for (File part : files) {
    		
    		PartInfo partInfo = new PartInfo();
    		partInfo.setPart(part);
    		partInfo.setOwnerPublicKey(keys.getPublicAsymetricKeyAsString());
    		
    		signatureService.init(keys.getPrivateAsymetricKey());
    		String fileSign = signatureService.sign(part);
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
		metadata.setRepository(repositoryId);
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

	private File decryptFile(File encryptedFile, SecretKey key) {
    	try {
			File decryptedFile = new File(
					StringUtils.substringBefore(encryptedFile.getAbsolutePath(), ".") + ".dec");
			encryptionService.decrypt(encryptedFile, decryptedFile);
			return decryptedFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	private File encryptFile(File file, SecretKey key) {
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

    private File getWorkDirectory() {
    	File work = new File("./work");
    	work.mkdir();
    	return work;
    }
    
    

}
