package org.hivedrive.cmd.service;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;

import org.hivedrive.cmd.model.UserKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignatureService {


	@Autowired 
	private UserKeysService userKeysService;
	
	
	
	public String signByClient(String textToSign) {
		return sign(textToSign, userKeysService.getKeys().getPrivateAsymetricKey());
	}
	
	public String sign(String textToSign, PrivateKey privateAsymetricKey) {
		return signByClient(textToSign.getBytes());
	}
	
	public String signByClient(File file) {
		return sign(file, userKeysService.getKeys().getPrivateAsymetricKey());
	}
	
	public String sign(File file, PrivateKey privateAsymetricKey) {
		try (FileInputStream fis = new FileInputStream(file)) {
			Signature sig = Signature.getInstance("SHA1WithRSA");
		    sig.initSign(privateAsymetricKey);
			while(fis.available() > 0) {
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
	
	public String signByClient(byte[] dataToSign) {
		return sign(dataToSign, userKeysService.getKeys().getPrivateAsymetricKey());
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
