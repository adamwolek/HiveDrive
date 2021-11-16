package org.hivedrive.cmd.service;

import static org.hivedrive.cmd.service.CryptographicTestHelper.hash;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.config.HDTestSpringExtension;
import org.hivedrive.cmd.config.TestConfig;
import org.hivedrive.cmd.exception.DecryptionFailedException;
import org.hivedrive.cmd.model.UserKeys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.base.Stopwatch;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class AsymetricEncryptionServiceTest {
	
	private final String sourceText = "abcdefghijklmnoprstuwxyz0123456789";
	
	
	@TempDir
	public File tempFolder;
   
	@Autowired
	private AsymetricEncryptionService encryptionService;
	
	@Autowired
	private UserKeysService userKeysService;
	
//	@BeforeEach
//	private void beforeTest() {
//		userKeysService.setKeys(userKeysService.generateNewKeys());
//	}
	
//	@Tag("TimeTesting")
//	@Test
//	public void timeTest() throws IOException {
//		File sourceFile = new File(tempFolder, "/sourceFile.src");
//		File encryptedFile = new File(tempFolder, "/encryptedFile.enc");
//		File decryptedFile = new File(tempFolder, "/decryptedFile");
//		
//		CryptographicTestHelper.fillSourceFileByRandomContent(sourceFile);
//		UserKeys userKeys = UserKeys.generateNewKeys();
//		
//		encryptionService.init(userKeys.getPublicAsymetricKey());
//		Stopwatch stopwatch = Stopwatch.createStarted();
//		encryptionService.encryptAnyway(sourceFile, encryptedFile);
//		System.out.println("Szyfrowanie asymetryczne: " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
//		
////		assertEquals(hash(sourceFile), hash(decryptedFile));
//	}
	
	@Tag("Crypto")
	@Test
	public void testEncryptionWithPrivateKey() throws IOException {
		File sourceFile = new File(tempFolder, "/sourceFile.src");
		File encryptedFile = new File(tempFolder, "/encryptedFile.enc");
		File decryptedFile = new File(tempFolder, "/decryptedFile");
		
		FileUtils.writeStringToFile(sourceFile, sourceText, "UTF-8");
		
		encryptionService.encryptWithPrivateKey(sourceFile, encryptedFile);
		
		encryptionService.decryptWithPublicKey(encryptedFile, decryptedFile);
		
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
		
		encryptionService.encryptWithPublicKey(sourceFile, encryptedFile);
		
		encryptionService.decryptWithPrivateKey(encryptedFile, decryptedFile);
		
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
			
			encryptionService.encryptWithPublicKey(sourceFile, encryptedFile);
			
			encryptionService.decryptWithPublicKey(encryptedFile, decryptedFile);
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
			
			encryptionService.encryptWithPrivateKey(sourceFile, encryptedFile);
			
			encryptionService.decryptWithPrivateKey(encryptedFile, decryptedFile);
	    });
	}
	
}
