package org.hivedrive.server.service;

import org.springframework.stereotype.Service;

@Service
public class ServerKeysService {

	private String privateAsymetricKey = "MIIJQgIBADANBgkqhkiG9w0BAQEFAASCCSwwggkoAgEAAoICAQCFjf7x";
	private String publicAsymetricKey = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAhY3";
	
	public String getPrivateAsymetricKey() {
		return privateAsymetricKey;
	}
	public void setPrivateAsymetricKey(String privateAsymetricKey) {
		this.privateAsymetricKey = privateAsymetricKey;
	}
	public String getPublicAsymetricKey() {
		return publicAsymetricKey;
	}
	public void setPublicAsymetricKey(String publicAsymetricKey) {
		this.publicAsymetricKey = publicAsymetricKey;
	}
	
	
	
}
