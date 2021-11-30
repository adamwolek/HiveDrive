package org.hivedrive.cmd.session;

import java.time.LocalDateTime;

import org.hivedrive.cmd.model.NodeEntity;

public class P2PSession {

	public static enum Status{
		
	}
	
	private NodeEntity node;
	private LocalDateTime createDate;
	private Status status;
	
	public NodeEntity getNode() {
		return node;
	}
	public void setNode(NodeEntity node) {
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
