package org.hivedrive.cmd.to;

import java.time.LocalDateTime;

import org.apache.commons.codec.digest.DigestUtils;
import org.hivedrive.cmd.status.PartStatus;

public class PartTO {

	private LocalDateTime createDate;
	private PartStatus status;
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

	private void refreshGlobalId() {
		this.globalId = DigestUtils.md5Hex(ownerId) + "|" + repository + "|" + groupId + "|"
				+ orderInGroup;
	}

	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	public PartStatus getStatus() {
		return status;
	}

	public void setStatus(PartStatus status) {
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
		refreshGlobalId();
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
		refreshGlobalId();
	}

	public int getOrderInGroup() {
		return orderInGroup;
	}

	public void setOrderInGroup(int orderInGroup) {
		this.orderInGroup = orderInGroup;
		refreshGlobalId();
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
		refreshGlobalId();
	}
}
