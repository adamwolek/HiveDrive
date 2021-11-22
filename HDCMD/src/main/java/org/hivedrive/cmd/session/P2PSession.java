package org.hivedrive.cmd.session;

import java.time.LocalDateTime;

import org.hivedrive.cmd.model.Node;

public class P2PSession {

	public static enum Status{
		
	}
	
	private Node node;
	private LocalDateTime createDate;
	private Status status;
	
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	public LocalDateTime getCreateDate() {
		return createDate;
	}
	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	
	
	
	
}
