package org.hivedrive.server.service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.hivedrive.cmd.exception.GenerateKeysError;
import org.springframework.stereotype.Service;

@Service
public class NodeKeysService {

	private String privateAsymetricKey = "MIIJQgIBADANBgkqhkiG9w0BAQEFAASCCSwwggkoAgEAAoICAQCFjf7x";
	private String publicAsymetricKey = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAhY3";
	
	public String getPrivateAsymetricKeyAsString() {
		return privateAsymetricKey;
	}
	public void setPrivateAsymetricKeyAsString(String privateAsymetricKey) {
		this.privateAsymetricKey = privateAsymetricKey;
	}
	public String getPublicAsymetricKeyAsString() {
		return publicAsymetricKey;
	}
	public void setPublicAsymetricKeyAsString(String publicAsymetricKey) {
		this.publicAsymetricKey = publicAsymetricKey;
	}
	
	public PrivateKey getPrivateAsymetricKey() {
		try {
			byte[] decoded = Base64.getDecoder().decode(privateAsymetricKey);
			KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
			PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(decoded));
			return privateKey;
		} catch (Exception e) {
			throw new GenerateKeysError(e);
		}
	}
	
	public PublicKey getPublicAsymetricKey() {
		try {
			byte[] decoded = Base64.getDecoder().decode(publicAsymetricKey);
			KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
			PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(decoded));
			return publicKey;
		} catch (Exception e) {
			throw new GenerateKeysError(e);
		}
	}
	
	
	
}
