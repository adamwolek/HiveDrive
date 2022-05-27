package org.hivedrive.cmd.model;

import java.util.Objects;

public class NodeEntity {

	private String publicKey;

	private String status;
	 
	private String address;
	
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

	@Override
	public int hashCode() {
		return Objects.hash(address);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeEntity other = (NodeEntity) obj;
		return Objects.equals(address, other.address);
	}
	
	
}
