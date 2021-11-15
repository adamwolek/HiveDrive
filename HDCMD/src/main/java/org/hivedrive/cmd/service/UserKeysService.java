package org.hivedrive.cmd.service;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.exception.GenerateKeysError;
import org.hivedrive.cmd.exception.LoadingKeysError;
import org.hivedrive.cmd.exception.SaveKeysError;
import org.hivedrive.cmd.model.UserKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Service
public class UserKeysService {

	@Autowired
	private RepositoryConfigService configService;
	
	private UserKeys keys;
	
	@PostConstruct
	public void loadKeys() {
		if(configService.getConfig() != null) {
			File keysFile = new File(configService.getConfig().getKeysPath());
			keys = load(keysFile);
		} else {
			keys = generateNewKeys(); //only for tests!
		}
	}
	
	private UserKeys load(File clientKeys) {
		try {
			String json = FileUtils.readFileToString(clientKeys, "UTF-8");
			UserKeys keys = new Gson().fromJson(json, UserKeys.class);
			return keys;
		} catch (Exception e) {
			throw new LoadingKeysError(e);
		}
	}
	
	public void save(File clientKeys) {
		String json = new GsonBuilder().setPrettyPrinting().create().toJson(this);
		try {
			FileUtils.writeStringToFile(clientKeys, json, "UTF-8");
		} catch (IOException e) {
			throw new SaveKeysError(e);
		}
	}
	
	public UserKeys generateNewKeys() {
		try {
			UserKeys keys = new UserKeys();
			
			SecretKey symetricKey = generateSymetricKey();
			String symetricPrivateBase64 = Base64.getEncoder()
					.encodeToString(symetricKey.getEncoded());
			keys.setPrivateSymetricKey(symetricPrivateBase64);
			
			KeyPair asymetricKey = generateAsymetricKey();
			
			Key pvt = asymetricKey.getPrivate();
			String asymetricPrivateBase64 = Base64.getEncoder()
					.encodeToString(pvt.getEncoded());
			keys.setPrivateAsymetricKey(asymetricPrivateBase64);
			
			Key pub = asymetricKey.getPublic();
			String asymetricPublicBase64 = Base64.getEncoder()
					.encodeToString(pub.getEncoded());
			keys.setPublicAsymetricKey(asymetricPublicBase64);
			
			return keys;
		} catch (Exception e) {
			throw new GenerateKeysError(e);
		}
	}
	
	private SecretKey generateSymetricKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(256);
		SecretKey key = keyGenerator.generateKey();
		return key;
	}
	
	private KeyPair generateAsymetricKey() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(4096);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		return keyPair;
	}

	public UserKeys getKeys() {
		return keys;
	}

	public void setKeys(UserKeys keys) {
		this.keys = keys;
	}
	
	
	
}
