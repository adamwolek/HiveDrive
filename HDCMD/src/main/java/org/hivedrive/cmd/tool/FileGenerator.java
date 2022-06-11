package org.hivedrive.cmd.tool;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.FileUtils;

public class FileGenerator {

	public static int EXAMPLE_FILES_SIZE_IN_BYTES = 50 * 1024 * 1024;
	
	public static File createBigFile(File bigFile) throws IOException {
		return createFile(bigFile);
	}
	
	public static File createMediumFile(File mediumFile) throws IOException {
        return createFile(mediumFile);
	}
	
	private static File createFile(File file) throws IOException {
		Random random = new Random();
    	byte[] bytes = new byte[EXAMPLE_FILES_SIZE_IN_BYTES];
    	random.nextBytes(bytes);
    	FileUtils.writeByteArrayToFile(file, bytes);
        return file;
	}
	
}
