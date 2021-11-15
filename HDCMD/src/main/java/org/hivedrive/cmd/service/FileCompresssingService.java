package org.hivedrive.cmd.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

@Service
public class FileCompresssingService {

	public void compressFile(File inputFile, File outputFile) {
		try {
	        FileOutputStream fos = new FileOutputStream(outputFile);
	        ZipOutputStream zipOut = new ZipOutputStream(fos);
	        FileInputStream fis = new FileInputStream(inputFile);
	        ZipEntry zipEntry = new ZipEntry(inputFile.getName());
	        zipOut.putNextEntry(zipEntry);
	        byte[] bytes = new byte[1024];
	        int length;
	        while((length = fis.read(bytes)) >= 0) {
	            zipOut.write(bytes, 0, length);
	        }
	        zipOut.close();
	        fis.close();
	        fos.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void uncompressFile(File inputFile, File outputFile) {
		try (ZipFile zipFile = new ZipFile(inputFile)){
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			if(zipFile.size() != 1) {
				throw new RuntimeException("ZIP contains " + zipFile.size() 
				+ " entries. There should be only 1");
			}
			while(entries.hasMoreElements()){
			    ZipEntry entry = entries.nextElement();
			    InputStream inputStream = zipFile.getInputStream(entry);
			    try (FileOutputStream fos = new FileOutputStream(outputFile)){
			    	IOUtils.copy(inputStream, fos);
			    }
			    inputStream.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
