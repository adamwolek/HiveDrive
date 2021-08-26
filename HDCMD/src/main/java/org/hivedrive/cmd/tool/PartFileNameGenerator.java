package org.hivedrive.cmd.tool;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

public class PartFileNameGenerator {

	private File directory;
	private int fileIndex = 0;
	private String baseName;
	
	public PartFileNameGenerator(String baseName, File directory) {
		this.baseName = StringUtils.substringBefore(baseName, ".");
		this.directory = directory;
	}
	
	public File generateNextFile() {
		String newFilePath = directory.getAbsolutePath() + "/" + baseName + "." + fileIndex;
		fileIndex++;
		File newFile = new File(newFilePath);
		return newFile;
	}
	
	
}
