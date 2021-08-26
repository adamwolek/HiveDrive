package org.hivedrive.cmd;

import java.io.File;

import com.google.gson.annotations.Expose;

public class PartInfo {
	
	private String ownerPublicKey;
	private String fileSign;
	
	@Expose(serialize = false) 
	private FileMetadata fileMetadata;
	private String encryptedFileMetadata;
	
	private File part;
	
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
