package org.hivedrive.server.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;


@Component
@RequestScope
public class SenderInfo {

	private String senderPublicKey;
	private String sign;
	private String body;
	private boolean isAuthenticated;
	
	public String getSenderPublicKey() {
		return senderPublicKey;
	}
	public void setSenderPublicKey(String senderPublicKey) {
		this.senderPublicKey = senderPublicKey;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public boolean isAuthenticated() {
		return isAuthenticated;
	}
	public void setAuthenticated(boolean isAuthenticated) {
		this.isAuthenticated = isAuthenticated;
	}
	@Override
	public String toString() {
		return "SenderInfo [senderPublicKey=" + senderPublicKey + ", sign=" + sign + ", body=" + body
				+ ", isAuthenticated=" + isAuthenticated + "]";
	}
	
	
}
