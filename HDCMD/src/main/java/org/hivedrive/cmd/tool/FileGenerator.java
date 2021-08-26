package org.hivedrive.cmd.tool;

import java.io.File;
import java.io.RandomAccessFile;

public class FileGenerator {

	public static int EXAMPLE_FILES_SIZE_IN_BYTES = 100 * 1024 * 1024;
	
	public static File createBigFile(File bigFile, File tempFolder) {
        try (RandomAccessFile rafile = new RandomAccessFile(bigFile, "rw")){
            rafile.setLength(EXAMPLE_FILES_SIZE_IN_BYTES); // 3GB
            return bigFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
}
