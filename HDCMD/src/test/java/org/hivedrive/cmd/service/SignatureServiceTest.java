package org.hivedrive.cmd.service;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.lang3.RandomStringUtils;
import org.hivedrive.cmd.config.TestConfig;
import org.hivedrive.cmd.model.UserKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("unitTests")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class SignatureServiceTest {

	@Autowired
	SignatureService signatureService;
	
	@Autowired
	UserKeysService userKeysService;
	
	
	
	@Test
	void checkSignature() {
		userKeysService.generateNewKeys();
		String text = RandomStringUtils.random(10000);
		
		String signature = signatureService.signByClient(text);
		
		boolean result = signatureService.verifySign(
				signature, text, userKeysService.getKeys().getPublicAsymetricKeyAsString());
		
		assertTrue(result);
	}
	
	@Test
	void checkSignatureWithWrongPublicKey() {
		UserKeys firstPair = userKeysService.generateNewKeys();
		UserKeys secondPair = userKeysService.generateNewKeys();
		String text = RandomStringUtils.random(10000);
		
		String signature = signatureService.sign(text, firstPair.getPrivateAsymetricKey());
		
		boolean result = signatureService.verifySign(
				signature, text, secondPair.getPublicAsymetricKeyAsString());
		
		assertFalse(result);
	}

}
