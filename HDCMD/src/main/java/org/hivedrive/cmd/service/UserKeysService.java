package org.hivedrive.cmd.service;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
import org.hivedrive.cmd.tool.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Lazy
@Service
public class UserKeysService {

	@Autowired
	private RepositoryConfigService configService;

	private PropertyChangeSupport support;
	
	private UserKeys keys;
	
	public UserKeysService() {
		support = new PropertyChangeSupport(this);
	}

	@PostConstruct
	public void loadKeys() {
		configService.addPropertyChangeListener(event -> {
			if (configService.getConfig() != null) {
				File keysFile = new File(configService.getConfig().getKeysPath());
				setKeys(load(keysFile));
			}
		});
	}

	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		support.addPropertyChangeListener(pcl);
    }
	
	private UserKeys load(File clientKeys) {
		try {
			return JSONUtils.read(clientKeys, UserKeys.class);
		} catch (Exception e) {
			throw new LoadingKeysError(e);
		}
	}

	public void save(File clientKeys) {
		try {
			JSONUtils.write(clientKeys, keys);
		} catch (Exception e) {
			throw new SaveKeysError(e);
		}
	}

	public UserKeys generateNewKeys() {
		try {
			keys = new UserKeys();

			SecretKey symetricKey = generateSymetricKey();
			String symetricPrivateBase64 = Base64.getEncoder()
					.encodeToString(symetricKey.getEncoded());
			keys.setPrivateSymetricKey(symetricPrivateBase64);

			KeyPair asymetricKey = generateAsymetricKey();

			Key pvt = asymetricKey.getPrivate();
			String asymetricPrivateBase64 = Base64.getEncoder().encodeToString(pvt.getEncoded());
			keys.setPrivateAsymetricKey(asymetricPrivateBase64);

			Key pub = asymetricKey.getPublic();
			String asymetricPublicBase64 = Base64.getEncoder().encodeToString(pub.getEncoded());
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
		UserKeys oldValue = this.keys;
		this.keys = keys;
		support.firePropertyChange("keys", oldValue, this.keys);
		
	}

}
