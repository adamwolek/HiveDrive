package org.hivedrive.server.to;

import java.time.LocalDateTime;

public class PartTO {

	private LocalDateTime createDate;
	private String status;
	private String globalId;
	/**
	 * Name of repository created by repository owner
	 */
	private String repository;
	/**
	 * Encrypted name of the file from which the part was extracted
	 */
	private String groupId;
	/**
	 * Number of part in group
	 */
	private int orderInGroup;
	/**
	 * Public key of owner
	 */
	private String ownerId;
	
	public LocalDateTime getCreateDate() {
		return createDate;
	}
	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getGlobalId() {
		return globalId;
	}
	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}
	public String getRepository() {
		return repository;
	}
	public void setRepository(String repository) {
		this.repository = repository;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public int getOrderInGroup() {
		return orderInGroup;
	}
	public void setOrderInGroup(int orderInGroup) {
		this.orderInGroup = orderInGroup;
	}
	public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
}
