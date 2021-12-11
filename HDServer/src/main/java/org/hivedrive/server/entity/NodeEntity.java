package org.hivedrive.server.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class NodeEntity {

	@Id
	 private String publicKey;
	
	 private String status;
	 
	 private String ipAddress;

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	 
}
