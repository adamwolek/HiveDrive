package org.hivedrive.cmd.model;

import java.io.File;


import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Base64;
import java.security.KeyFactory;

import javax.annotation.PostConstruct;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.exception.GenerateKeysError;
import org.hivedrive.cmd.exception.LoadingKeysError;
import org.hivedrive.cmd.exception.SaveKeysError;
import org.hivedrive.cmd.service.RepositoryConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
			String json = FileUtils.readFileToString(clientKeys, "UTF-8");
			UserKeys keys = new Gson().fromJson(json, UserKeys.class);
			return keys;
		} catch (Exception e) {
			throw new LoadingKeysError(e);
		}
	}
	
}
