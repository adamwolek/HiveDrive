package org.hivedrive.cmd.helper;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

public class FileNameHelper {

	private FileNameHelper() {}
	
	public static File changeExtension(File source, String newExtension) {
		String baseName = getBaseName(source);
		return new File(source.getParentFile(), baseName + "." + newExtension);
	}

	public static File removeExtension(File source) {
		String baseName = getBaseName(source);
		return new File(source.getParentFile(), baseName);
	}
	
	public static File addExtension(File source, String newExtension) {
		return new File(source.getAbsolutePath() + "." + newExtension);
	}
	
	public static File changeDirectory(File source, File newDirectory) {
		return new File(newDirectory, source.getName());
	}

	private static String getBaseName(File source) {
		return StringUtils.substringBeforeLast(source.getName(), ".");
	}
	
}
