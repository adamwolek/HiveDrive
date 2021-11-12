package org.hivedrive.server.to;

public class NodeTO {

	private String publicKey;

	 private String status;
	 
	 private String ipAddress;

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getNickname() {
		return status;
	}

	public void setNickname(String nickname) {
		this.status = nickname;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
}
