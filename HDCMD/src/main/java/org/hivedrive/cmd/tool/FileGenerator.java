package org.hivedrive.cmd.tool;

import java.io.File;
import java.io.RandomAccessFile;

public class FileGenerator {

	public static int EXAMPLE_FILES_SIZE_IN_BYTES = 50 * 1024 * 1024;
	
	public static File createBigFile(File bigFile) {
        try (RandomAccessFile rafile = new RandomAccessFile(bigFile, "rw")){
            rafile.setLength(EXAMPLE_FILES_SIZE_IN_BYTES);
            return bigFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
}
