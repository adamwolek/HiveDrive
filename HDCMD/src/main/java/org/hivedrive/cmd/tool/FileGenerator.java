package org.hivedrive.cmd.tool;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.FileUtils;

public class FileGenerator {

	public static int EXAMPLE_FILES_SIZE_IN_BYTES = 50 * 1024 * 1024;
	
	public static File createBigFile(File bigFile) throws IOException {
		return createFile(bigFile, EXAMPLE_FILES_SIZE_IN_BYTES);
	}
	
	public static File createMediumFile(File mediumFile) throws IOException {
        return createFile(mediumFile, (int)Math.round(0.05 * EXAMPLE_FILES_SIZE_IN_BYTES));
	}
	
	private static File createFile(File file, int size) throws IOException {
		Random random = new Random();
    	byte[] bytes = new byte[size];
    	random.nextBytes(bytes);
    	FileUtils.writeByteArrayToFile(file, bytes);
        return file;
	}
	
}
