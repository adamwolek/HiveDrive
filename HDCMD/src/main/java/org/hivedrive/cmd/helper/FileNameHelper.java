package org.hivedrive.cmd.helper;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

public class FileNameHelper {

	public static File changeExtension(File source, String newExtension) {
		String baseName = StringUtils.substringBeforeLast(source.getName(), ".");
		File newFile = new File(source.getParentFile(), baseName + "." + newExtension);
		return newFile;
	}
	
	public static File removeExtension(File source) {
		String baseName = StringUtils.substringBeforeLast(source.getName(), ".");
		File newFile = new File(source.getParentFile(), baseName);
		return newFile;
	}
	
	public static File addExtension(File source, String newExtension) {
		File newFile = new File(source.getAbsolutePath() + "." + newExtension);
		return newFile;
	}
	
	public static File changeDirectory(File source, File newDirectory) {
		File newFile = new File(newDirectory, source.getName());
		return newFile;
	}
	
	
}
