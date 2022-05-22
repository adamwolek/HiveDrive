package org.hivedrive.server.entity;

import java.io.File;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hivedrive.cmd.status.PartStatus;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;


@Entity
public class PartEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Owner
	 */
	@ManyToOne
	private NodeEntity node;

	private String spaceId;
	private String pathToPart;
	private Integer size;
	
	
	private LocalDateTime createDate;
	
	private PartStatus status;
	private String globalId;
	/**
	 * File hash before encryption and compression
	 */
	private String fileHash;
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
	private Integer orderInGroup;

	private String encryptedFileMetadata;
	
	
	
	
	public String getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(String spaceId) {
		this.spaceId = spaceId;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getEncryptedFileMetadata() {
		return encryptedFileMetadata;
	}

	public void setEncryptedFileMetadata(String encryptedFileMetadata) {
		this.encryptedFileMetadata = encryptedFileMetadata;
	}

	public NodeEntity getNode() {
		return node;
	}

	public void setNode(NodeEntity node) {
		this.node = node;
	}

	public String getPathToPart() {
		return pathToPart;
	}

	public void setPathToPart(String pathToPart) {
		this.pathToPart = pathToPart;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
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
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public Integer getOrderInGroup() {
		return orderInGroup;
	}

	public void setOrderInGroup(Integer orderInGroup) {
		this.orderInGroup = orderInGroup;
	}

	public String getHash() {
		return fileHash;
	}

	public void setHash(String hash) {
		this.fileHash = hash;
	}

}
