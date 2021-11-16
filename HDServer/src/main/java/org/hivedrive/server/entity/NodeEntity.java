package org.hivedrive.server.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.collections4.Get;

@Entity
public class NodeEntity {

	 @Id
	 @GeneratedValue(strategy=GenerationType.IDENTITY)
	 private Long id;
	 
	 private String publicKey;
	
	 private String status;
	 
	 private String ipAddress;

	 public Long getId() {
		 return id;
	 }
	 
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
