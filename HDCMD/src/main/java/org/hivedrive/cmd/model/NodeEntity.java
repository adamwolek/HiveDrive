package org.hivedrive.cmd.model;

public class NodeEntity {

	private String publicKey;

	private String status;
	 
	private String ipAddress;
	
	private Long freeSpace;
	
	private Long usedSpace;

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

	public Long getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(Long freeSpace) {
		this.freeSpace = freeSpace;
	}

	public Long getUsedSpace() {
		return usedSpace;
	}

	public void setUsedSpace(Long usedSpace) {
		this.usedSpace = usedSpace;
	}
	
	
}
