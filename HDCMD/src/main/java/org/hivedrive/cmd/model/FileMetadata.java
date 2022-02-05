package org.hivedrive.cmd.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.hivedrive.cmd.tool.JSONUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;



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
		try {
			return JSONUtils.createWrtier().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "error";
		}
	}
	
	@Override
	public String toString() {
		return toJSON();
	}
	
	
	
}
