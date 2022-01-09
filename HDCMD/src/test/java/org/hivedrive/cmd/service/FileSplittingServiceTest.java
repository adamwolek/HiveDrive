package org.hivedrive.cmd.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.config.TestConfig;
import org.hivedrive.cmd.tool.FileGenerator;
import org.hivedrive.cmd.tool.PartFileNameGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("unitTests")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class FileSplittingServiceTest {

	
	
	@TempDir
	public File tempFolder;
	
	@Autowired
	private FileSplittingService splittingService;
	
	@Test
	public void splitBigFileTest() throws IOException {
		File outputFolder = new File(tempFolder.getAbsolutePath() + "/outputFolder");
		outputFolder.mkdir();
		
		File bigFile = new File(tempFolder.getAbsolutePath() + "/bigFile");
		FileGenerator.createBigFile(bigFile, tempFolder);
		splittingService.splitFileIntoDirectory(bigFile, outputFolder);
		
		double howManyFilesDouble = Math.ceil(FileGenerator.EXAMPLE_FILES_SIZE_IN_BYTES / (double)FileSplittingService.MAX_SIZE_IN_BYTES); 
		long howManyFilesShouldBeCreated = Math.round(howManyFilesDouble);
		
		assertEquals(howManyFilesShouldBeCreated, outputFolder.listFiles().length);
		
		File sourceFolder = outputFolder;
		File mergedFile = new File(tempFolder.getAbsolutePath() + "/mergedFile");
		splittingService.mergeFilesFromDirectory(sourceFolder, mergedFile);
		
		Assertions.assertTrue(FileUtils.contentEquals(bigFile, mergedFile));
	}
	
}
