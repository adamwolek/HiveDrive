package org.hivedrive.cmd.service;

import static org.hivedrive.cmd.service.CryptographicTestHelper.hash;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.exception.DecryptionFailedException;
import org.hivedrive.cmd.model.UserKeys;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.common.base.Stopwatch;

public class AsymetricEncryptionServiceTest {
	
	private final String sourceText = "abcdefghijklmnoprstuwxyz0123456789";
	
	
	@TempDir
	public File tempFolder;
   
	@Tag("TimeTesting")
	@Test
	public void timeTest() throws IOException {
		File sourceFile = new File(tempFolder, "/sourceFile.src");
		File encryptedFile = new File(tempFolder, "/encryptedFile.enc");
		File decryptedFile = new File(tempFolder, "/decryptedFile");
		
		CryptographicTestHelper.fillSourceFileByRandomContent(sourceFile);
		UserKeys userKeys = UserKeys.generateNewKeys();
		
		AsymetricEncryptionService encryptionService 
			= new AsymetricEncryptionService(userKeys.getPublicAsymetricKey());
		Stopwatch stopwatch = Stopwatch.createStarted();
		encryptionService.encryptAnyway(sourceFile, encryptedFile);
		System.out.println("Szyfrowanie asymetryczne: " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
		
//		assertEquals(hash(sourceFile), hash(decryptedFile));
	}
	
	@Tag("Crypto")
	@Test
	public void testEncryptionWithPrivateKey() throws IOException {
		File sourceFile = new File(tempFolder, "/sourceFile.src");
		File encryptedFile = new File(tempFolder, "/encryptedFile.enc");
		File decryptedFile = new File(tempFolder, "/decryptedFile");
		
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
	
	@Tag("Crypto")
	@Test
	public void testEncryptionWithPublicKey() throws IOException {
		File sourceFile = new File(tempFolder, "/sourceFile.src");
		File encryptedFile = new File(tempFolder, "/encryptedFile.enc");
		File decryptedFile = new File(tempFolder, "/decryptedFile");
		
		FileUtils.writeStringToFile(sourceFile, sourceText, "UTF-8");
		UserKeys userKeys = UserKeys.generateNewKeys();
		
		AsymetricEncryptionService encryptionService 
			= new AsymetricEncryptionService(userKeys.getPublicAsymetricKey());
		encryptionService.encryptAnyway(sourceFile, encryptedFile);
		
		AsymetricEncryptionService decryptionService 
			= new AsymetricEncryptionService(userKeys.getPrivateAsymetricKey());
		decryptionService.decrypt(encryptedFile, decryptedFile);
		
		final String loadedText = FileUtils.readFileToString(decryptedFile, "UTF-8");

		assertEquals(loadedText, sourceText);
	}

	@Tag("Crypto")
	@Test
	public void encryptAndDecryptSamePublicKey() throws IOException {
		assertThrows(DecryptionFailedException.class, () -> {
			File sourceFile = new File(tempFolder, "/sourceFile.src");
			File encryptedFile = new File(tempFolder, "/encryptedFile.enc");
			File decryptedFile = new File(tempFolder, "/decryptedFile");
			
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
	
	@Tag("Crypto")
	@Test
	public void encryptAndDecryptSamePrivateKey() throws IOException {
		assertThrows(DecryptionFailedException.class, () -> {
			File sourceFile = new File(tempFolder, "/sourceFile.src");
			File encryptedFile = new File(tempFolder, "/encryptedFile.enc");
			File decryptedFile = new File(tempFolder, "/decryptedFile");
			
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
