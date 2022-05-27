package org.hivedrive.cmd.to;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NodeTO {
	/**
	 * Public key which is used by the node's user
	 */
	private String publicKey;

	/**
	 * Status, e.g. new
	 */
	private String status;

	private String address;
	
	/**
	 * Amount of space (in GB) where files can be saved
	 */
	private Long freeSpace;

	/**
	 * Amount of space (in GB) where files have been already saved
	 */
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


	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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
