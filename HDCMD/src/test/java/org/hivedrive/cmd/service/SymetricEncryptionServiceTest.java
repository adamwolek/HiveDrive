package org.hivedrive.cmd.service;

import org.apache.commons.io.FileUtils;

import org.hivedrive.cmd.model.UserKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

public class SymetricEncryptionServiceTest {
	
		private final String sourceText = "abcdefghijklmnoprstuwxyz0123456789";
	
		@TempDir
		public File tempFolder;
	   
		@Test
		public void testEncryption() throws IOException {
			File sourceFile = new File(tempFolder.getAbsolutePath() + "sourceFile.src");
			File encryptedFile = new File(tempFolder.getAbsolutePath() + "encryptedFile.enc");
			File decryptedFile = new File(tempFolder.getAbsolutePath() + "decryptedFile");
			
			FileUtils.writeStringToFile(sourceFile, sourceText, "UTF-8");
			UserKeys userKeys = UserKeys.generateNewKeys();
			SymetricEncryptionService service 
				= new SymetricEncryptionService(userKeys.getPrivateSymetricKey());
			
			service.encrypt(sourceFile, encryptedFile);
			
			service.decrypt(encryptedFile, decryptedFile);
			
			final String loadedText = FileUtils.readFileToString(decryptedFile, "UTF-8");
   
			assertEquals(loadedText, sourceText);
		}
		
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
