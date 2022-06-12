package org.hivedrive.cmd.model;

import org.hivedrive.cmd.tool.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;



public class FileMetadata {

	private static Logger logger = LoggerFactory.getLogger(FileMetadata.class);
	
	private String repository;
	private int partIndex;
	private String filePath;
	
	public String getRepository() {
		return repository;
	}
	public void setRepository(String repository) {
		this.repository = repository;
	}
	public int getPartIndex() {
		return partIndex;
	}
	public void setPartIndex(int partIndex) {
		this.partIndex = partIndex;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String toJSON() {
		try {
			return JSONUtils.createWrtier().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			logger.error("Error: ", e);
			return "error";
		}
	}
	
	public static FileMetadata parseJSON(String json) {
		try {
			FileMetadata fileMetadata = JSONUtils.mapper().reader()
					.readValue(json, FileMetadata.class);
			return fileMetadata;
		} catch (Exception e) {
			logger.error("Error: ", e);
			return null;
		}
	}
	
	@Override
	public String toString() {
		return toJSON();
	}
	
	
	
}
