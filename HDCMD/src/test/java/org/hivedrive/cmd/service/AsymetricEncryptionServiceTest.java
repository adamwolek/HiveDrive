package org.hivedrive.cmd.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.exception.DecryptionFailedException;
import org.hivedrive.cmd.exception.EncryptionFailedException;
import org.hivedrive.cmd.model.UserKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AsymetricEncryptionServiceTest {
	private final String sourceText = "abcdefghijklmnoprstuwxyz0123456789";
	
	@TempDir
	public File tempFolder;
   
	@Test
	public void testEncryptionWithPrivateKey() throws IOException {
		File sourceFile = new File(tempFolder.getAbsolutePath() + "sourceFile.src");
		File encryptedFile = new File(tempFolder.getAbsolutePath() + "encryptedFile.enc");
		File decryptedFile = new File(tempFolder.getAbsolutePath() + "decryptedFile");
		
		FileUtils.writeStringToFile(sourceFile, sourceText, "UTF-8");
		UserKeys userKeys = UserKeys.generateNewKeys();
		
		AsymetricEncryptionService encryptionService 
			= new AsymetricEncryptionService(userKeys.getPrivateAsymetricKey());
		encryptionService.encrypt(sourceFile, encryptedFile);
		
		AsymetricEncryptionService decryptionService 
			= new AsymetricEncryptionService(userKeys.getPublicAsymetricKey());
		decryptionService.decrypt(encryptedFile, decryptedFile);
		
		final String loadedText = FileUtils.readFileToString(decryptedFile, "UTF-8");

		assertEquals(loadedText, sourceText);
	}
	
	@Test
	public void testEncryptionWithPublicKey() throws IOException {
		File sourceFile = new File(tempFolder.getAbsolutePath() + "sourceFile.src");
		File encryptedFile = new File(tempFolder.getAbsolutePath() + "encryptedFile.enc");
		File decryptedFile = new File(tempFolder.getAbsolutePath() + "decryptedFile");
		
		FileUtils.writeStringToFile(sourceFile, sourceText, "UTF-8");
		UserKeys userKeys = UserKeys.generateNewKeys();
		
		AsymetricEncryptionService encryptionService 
			= new AsymetricEncryptionService(userKeys.getPublicAsymetricKey());
		encryptionService.encrypt(sourceFile, encryptedFile);
		
		AsymetricEncryptionService decryptionService 
			= new AsymetricEncryptionService(userKeys.getPrivateAsymetricKey());
		decryptionService.decrypt(encryptedFile, decryptedFile);
		
		final String loadedText = FileUtils.readFileToString(decryptedFile, "UTF-8");

		assertEquals(loadedText, sourceText);
	}
	
	@Test
	public void encryptAndDecryptSamePublicKey() throws IOException {
		assertThrows(DecryptionFailedException.class, () -> {
			File sourceFile = new File(tempFolder.getAbsolutePath() + "sourceFile.src");
			File encryptedFile = new File(tempFolder.getAbsolutePath() + "encryptedFile.enc");
			File decryptedFile = new File(tempFolder.getAbsolutePath() + "decryptedFile");
			
			FileUtils.writeStringToFile(sourceFile, sourceText, "UTF-8");
			UserKeys userKeys = UserKeys.generateNewKeys();
			
			AsymetricEncryptionService encryptionService 
				= new AsymetricEncryptionService(userKeys.getPublicAsymetricKey());
			encryptionService.encrypt(sourceFile, encryptedFile);
			
			AsymetricEncryptionService decryptionService 
				= new AsymetricEncryptionService(userKeys.getPublicAsymetricKey());
			decryptionService.decrypt(encryptedFile, decryptedFile);
	    });
	}
	
	@Test
	public void encryptAndDecryptSamePrivateKey() throws IOException {
		assertThrows(DecryptionFailedException.class, () -> {
			File sourceFile = new File(tempFolder.getAbsolutePath() + "sourceFile.src");
			File encryptedFile = new File(tempFolder.getAbsolutePath() + "encryptedFile.enc");
			File decryptedFile = new File(tempFolder.getAbsolutePath() + "decryptedFile");
			
			FileUtils.writeStringToFile(sourceFile, sourceText, "UTF-8");
			UserKeys userKeys = UserKeys.generateNewKeys();
			
			AsymetricEncryptionService encryptionService 
				= new AsymetricEncryptionService(userKeys.getPrivateAsymetricKey());
			encryptionService.encrypt(sourceFile, encryptedFile);
			
			AsymetricEncryptionService decryptionService 
				= new AsymetricEncryptionService(userKeys.getPrivateAsymetricKey());
			decryptionService.decrypt(encryptedFile, decryptedFile);
	    });
	}
}
