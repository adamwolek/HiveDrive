package org.hivedrive.cmd.to;

import java.time.LocalDateTime;

import org.hivedrive.cmd.status.PartStatus;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

public class PartTO {

	private Long id;
	
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime createDate;
	private PartStatus status;
	
	private String fileId;
	/**
	 * Name of repository created by repository owner
	 */
	private String repository;
	/**
	 * Number of part in group
	 */
	private int orderInGroup;
	/**
	 * Public key of owner
	 */
	private String ownerId;

	private NodeTO nodeWhichContainsPart;
	
	private String encryptedFileMetadata;
	
	
	public String getEncryptedFileMetadata() {
		return encryptedFileMetadata;
	}

	public void setEncryptedFileMetadata(String encryptedFileMetadata) {
		this.encryptedFileMetadata = encryptedFileMetadata;
	}

	public NodeTO getNodeWhichContainsPart() {
		return nodeWhichContainsPart;
	}

	public void setNodeWhichContainsPart(NodeTO nodeWhichContainsPart) {
		this.nodeWhichContainsPart = nodeWhichContainsPart;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
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
