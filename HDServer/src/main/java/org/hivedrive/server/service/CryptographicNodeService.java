package org.hivedrive.server.service;

import java.io.File;

import javax.annotation.PostConstruct;

import org.hivedrive.cmd.model.UserKeys;
import org.springframework.stereotype.Service;

@Service
public class CryptographicNodeService {

	private UserKeys keys;

	@PostConstruct
    public void init() {
		File keysFolder = new File("./keys");
		File[] files = keysFolder.listFiles();
		if(files.length == 1) {
			keys = UserKeys.load(files[0]);
		}
    }

	public UserKeys getKeys() {
		return keys;
	}

	public void setKeys(UserKeys keys) {
		this.keys = keys;
	}
	
	
	
}
