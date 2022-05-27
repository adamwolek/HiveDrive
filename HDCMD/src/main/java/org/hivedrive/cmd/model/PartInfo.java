package org.hivedrive.cmd.model;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class PartInfo {
	
	private String ownerPublicKey;
	
	private String fileSign;
	
	@JsonIgnore
	private FileMetadata fileMetadata;
	
	private String encryptedFileMetadata;
	
	
	/**
	 * MD5 z pliku źródłowego
	 */
	private String fileHash;
	
	private File part;
	
	
	
	public String getFileHash() {
		return fileHash;
	}
	public void setFileHash(String fileHash) {
		this.fileHash = fileHash;
	}
	public String getOwnerPublicKey() {
		return ownerPublicKey;
	}
	public void setOwnerPublicKey(String ownerPublicKey) {
		this.ownerPublicKey = ownerPublicKey;
	}
	public String getFileSign() {
		return fileSign;
	}
	public void setFileSign(String fileSign) {
		this.fileSign = fileSign;
	}
	public FileMetadata getFileMetadata() {
		return fileMetadata;
	}
	public void setFileMetadata(FileMetadata fileMetadata) {
		this.fileMetadata = fileMetadata;
	}
	public String getEncryptedFileMetadata() {
		return encryptedFileMetadata;
	}
	public void setEncryptedFileMetadata(String encryptedFileMetadata) {
		this.encryptedFileMetadata = encryptedFileMetadata;
	}
	public File getPart() {
		return part;
	}
	public void setPart(File part) {
		this.part = part;
	}
	
	
	
}
