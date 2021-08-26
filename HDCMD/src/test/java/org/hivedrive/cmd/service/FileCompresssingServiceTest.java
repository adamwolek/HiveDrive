package org.hivedrive.cmd.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.tool.FileGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileCompresssingServiceTest {
	
	@TempDir
	public File tempFolder;
	
	@Test
	public void compressFileTest() throws IOException {
		
		File bigSourceFile = new File(tempFolder.getAbsolutePath() + "/bigFile");
		FileGenerator.createBigFile(bigSourceFile, tempFolder);
		
		File packedFile = new File(tempFolder.getAbsolutePath() + "/packedFile");
		FileCompresssingService service = new FileCompresssingService();
		service.compressFile(bigSourceFile, packedFile);
		
		double compressionLevel = FileUtils.sizeOf(packedFile) / (double)FileUtils.sizeOf(bigSourceFile);
		System.out.println("Compression level: " + compressionLevel);
		
		File unpackedFile = new File(tempFolder.getAbsolutePath() + "/unpackedFile");
		service.uncompressFile(packedFile, unpackedFile);
		
		Assertions.assertTrue(FileUtils.contentEquals(bigSourceFile, unpackedFile));
	}
}
