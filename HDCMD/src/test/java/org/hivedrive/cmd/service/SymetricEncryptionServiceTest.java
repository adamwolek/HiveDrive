package org.hivedrive.cmd.service;

import static org.hivedrive.cmd.service.CryptographicTestHelper.*;


import org.apache.commons.io.FileUtils;

import org.hivedrive.cmd.model.UserKeys;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.base.Stopwatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SymetricEncryptionServiceTest {
	
		private final String sourceText = "abcdefghijklmnoprstuwxyz0123456789";
	
		@TempDir
		public File tempFolder;
	   
		@Tag("TimeTesting")
		@Test
		public void testEncryption() throws IOException {
			File sourceFile = new File(tempFolder, "/sourceFile.src");
			File encryptedFile = new File(tempFolder, "/encryptedFile.enc");
			File decryptedFile = new File(tempFolder, "/decryptedFile");
			
			fillSourceFileByRandomContent(sourceFile);
			
			UserKeys userKeys = UserKeys.generateNewKeys();
			SymetricEncryptionService service 
				= new SymetricEncryptionService(userKeys.getPrivateSymetricKey());
			
			Stopwatch stopwatch = Stopwatch.createStarted();
			service.encrypt(sourceFile, encryptedFile);
			System.out.println("Szyfrowanie symetryczne: " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
			
			stopwatch.reset().start();
			service.decrypt(encryptedFile, decryptedFile);
			System.out.println("Deszyfrowanie symetryczne: " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
			
			assertEquals(hash(sourceFile), hash(decryptedFile));
		}
		
		@Tag("Crypto")
		@Test
		public void testTextEncryption() throws IOException {
			
			UserKeys userKeys = UserKeys.generateNewKeys();
			SymetricEncryptionService service 
				= new SymetricEncryptionService(userKeys.getPrivateSymetricKey());
			String encrypted = service.encrypt(sourceText);
			
			String decrypted = service.decrypt(encrypted);
   
			assertEquals(decrypted, sourceText);
		}
}
