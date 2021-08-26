package org.hivedrive.cmd;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.GsonBuilder;

public class FileMetadata {

	private String repository;
	private String fileId;
	private int partIndex;
	

	

	
	
	public String getRepository() {
		return repository;
	}
	public void setRepository(String repository) {
		this.repository = repository;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public int getPartIndex() {
		return partIndex;
	}
	public void setPartIndex(int partIndex) {
		this.partIndex = partIndex;
	}



	public String toJSON() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}
	
	@Override
	public String toString() {
		return toJSON();
	}
	
	
	
}
