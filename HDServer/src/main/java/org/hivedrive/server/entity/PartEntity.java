package org.hivedrive.server.entity;

import java.io.File;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class PartEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	/**
	 * Owner
	 */
	@ManyToOne
	private NodeEntity node;
	
	
	private File pathToPart;
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
	
	public NodeEntity getNode() {
		return node;
	}
	public void setNode(NodeEntity node) {
		this.node = node;
	}
	public File getPathToPart() {
		return pathToPart;
	}
	public void setPathToPart(File pathToPart) {
		this.pathToPart = pathToPart;
	}
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
}
