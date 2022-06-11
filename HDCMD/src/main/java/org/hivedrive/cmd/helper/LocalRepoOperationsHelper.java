package org.hivedrive.cmd.helper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.springframework.util.DigestUtils;

public class LocalRepoOperationsHelper {

	public static Collection<File> getAllFiles(File repositoryDirectory) {
		IOFileFilter filter = new IOFileFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return false;
			}
			@Override
			public boolean accept(File file) {
				List<String> excludedNames = Arrays.asList(".hivedrive", ".temp");
				return !(excludedNames.contains(file.getName()));
			}
		};
		Collection<File> allFiles = FileUtils.listFiles(repositoryDirectory, filter,
				filter);
		return allFiles;
	}
	
	public static String fileHash(File file) {
		String fileHash = null;
		try {
			fileHash = DigestUtils.md5DigestAsHex(FileUtils.readFileToByteArray(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileHash;
	}
	
}
