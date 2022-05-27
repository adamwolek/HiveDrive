package org.hivedrive.cmd.service.common;

import java.io.File;
import java.io.FileInputStream;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.hivedrive.cmd.model.UserKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class SignatureService {

	@Autowired
	private KeysService userKeysService;

	public String signStringUsingDefaultKeys(String textToSign) {
		return signString(textToSign, userKeysService.getKeys().getPrivateAsymetricKey());
	}

	public String signFileUsingDefaultKeys(File file) {
		return signFile(file, userKeysService.getKeys().getPrivateAsymetricKey());
	}

	public String signFile(File file, PrivateKey privateAsymetricKey) {
		try (FileInputStream fis = new FileInputStream(file)) {
			Signature sig = Signature.getInstance("SHA1WithRSA");
			sig.initSign(privateAsymetricKey);
			while (fis.available() > 0) {
				byte[] bytes = fis.readNBytes(1024);
				sig.update(bytes);
			}
			byte[] signatureBytes = sig.sign();
			String signature = Base64.getEncoder().encodeToString(signatureBytes);
			return signature;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String signUsingDefaultKeys(byte[] dataToSign) {
		return sign(dataToSign, userKeysService.getKeys().getPrivateAsymetricKey());
	}

	public String signString(String textToSign, PrivateKey privateAsymetricKey) {
		return sign(textToSign.getBytes(), privateAsymetricKey);
	}
	
	public String sign(byte[] dataToSign, PrivateKey privateAsymetricKey) {
		try {
			Signature sig = Signature.getInstance("SHA1WithRSA");
			sig.initSign(privateAsymetricKey);
			sig.update(dataToSign);
			byte[] signatureBytes = sig.sign();
			String signature = Base64.getEncoder().encodeToString(signatureBytes);
			return signature;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean verifySign(String signature, String dataToVerify, String publicKey) {
		if(StringUtils.isBlank(signature) 
				|| StringUtils.isBlank(dataToVerify) 
				|| StringUtils.isBlank(publicKey)) {
			return false;
		}
		byte[] signInBytes = Base64.getDecoder().decode(signature);
		return verifySign(signInBytes, dataToVerify.getBytes(), publicKey);
	}

	public boolean verifySign(byte[] dataToSign, byte[] dataToVerify, String publicKey) {
		UserKeys anotherUserKeys = new UserKeys();
		anotherUserKeys.setPublicAsymetricKey(publicKey);
		try {
			Signature sig = Signature.getInstance("SHA1WithRSA");
			sig.initVerify(anotherUserKeys.getPublicAsymetricKey());
			sig.update(dataToVerify);
			return sig.verify(dataToSign);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
