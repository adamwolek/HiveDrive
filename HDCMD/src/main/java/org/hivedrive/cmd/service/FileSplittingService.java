package org.hivedrive.cmd.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hivedrive.cmd.tool.PartFileNameGenerator;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class FileSplittingService {

	public static int MAX_SIZE_IN_BYTES = 10 * 1024 * 1024; // 10MB

	public List<File> splitFileIntoDirectory(File inputFile, File outputDirectory) {
		List<File> generatedFiles = new ArrayList<>();
		PartFileNameGenerator fileGenerator = new PartFileNameGenerator(inputFile.getName(),
				outputDirectory);
		try (FileInputStream fis = new FileInputStream(inputFile)) {
			while (fis.available() > 0) {
				byte[] bytes = fis.readNBytes(MAX_SIZE_IN_BYTES);
				File nextFile = fileGenerator.generateNextFile();
				FileUtils.writeByteArrayToFile(nextFile, bytes);
				generatedFiles.add(nextFile);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return generatedFiles;
	}

	public void mergeFilesFromDirectory(File inputDirecotry, File outputFile) {
		try (FileOutputStream fos = new FileOutputStream(outputFile)) {
			List<File> parts = Arrays.asList(inputDirecotry.listFiles()).stream().sorted((f1, f2) -> {
				return FilenameUtils.getExtension(f1.getName())
						.compareTo(FilenameUtils.getExtension(f2.getName()));
			}).collect(Collectors.toList());
			for (File part : parts) {
				byte[] bytes = FileUtils.readFileToByteArray(part);
				fos.write(bytes);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
