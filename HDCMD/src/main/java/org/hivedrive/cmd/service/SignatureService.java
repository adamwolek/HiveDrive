package org.hivedrive.cmd.service;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.Cipher;

public class SignatureService {

	private PrivateKey privateAsymetricKey;
	private PublicKey publicAsymetricKey;
	private Key key;
	private Cipher cipher;

	public SignatureService(PrivateKey privateAsymetricKey) {
		this.key = privateAsymetricKey;
		this.privateAsymetricKey = privateAsymetricKey;
	}

	public SignatureService(PublicKey publicAsymetricKey) {
		this.key = publicAsymetricKey;
		this.publicAsymetricKey = publicAsymetricKey;
	}
	
	public String sign(String textToSign) {
		return sign(textToSign.getBytes());
	}
	
	public String sign(File file) {
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
	
	public String sign(byte[] dataToSign) {
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
	
}
