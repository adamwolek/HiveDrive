package org.hivedrive.cmd.model;

import java.io.File;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.exception.GenerateKeysError;
import org.hivedrive.cmd.exception.LoadingKeysError;
import org.hivedrive.cmd.tool.JSONUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UserKeys {
	
	private String privateSymetricKey;
	private String privateAsymetricKey;
	private String publicAsymetricKey;
	
	public SecretKey getPrivateSymetricKey() {
		byte[] decoded = Base64.getDecoder().decode(privateSymetricKey);
		SecretKey key = new SecretKeySpec(decoded, 0, decoded.length, "AES");
		return key;
	}
	public void setPrivateSymetricKey(String privateSymetricKey) {
		this.privateSymetricKey = privateSymetricKey;
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
	
	public void setPrivateAsymetricKey(String privateAsymetricKey) {
		this.privateAsymetricKey = privateAsymetricKey;
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
	public void setPublicAsymetricKey(String publicAsymetricKey) {
		this.publicAsymetricKey = publicAsymetricKey;
	}

	public String getPublicAsymetricKeyAsString() {
		return publicAsymetricKey;
	}
	
	public static UserKeys load(File clientKeys) {
		try {
			return JSONUtils.read(clientKeys, UserKeys.class);
		} catch (Exception e) {
			throw new LoadingKeysError(e);
		}
	}
	
}
