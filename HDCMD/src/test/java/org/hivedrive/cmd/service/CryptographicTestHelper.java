package org.hivedrive.cmd.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;

public class CryptographicTestHelper {
	
	private static final int sizeOfSourceFileInMB = 100;
	
	public static void fillSourceFileByRandomContent(File sourceFile) {
		try (FileOutputStream fos = new FileOutputStream(sourceFile)){
			byte[] bytes = new byte[1024];
			for (int i = 0; i < sizeOfSourceFileInMB * 1024; i++) {
				Random random = new Random();
				random.nextBytes(bytes);
				fos.write(bytes);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String hash(File sourceFile) throws IOException, FileNotFoundException {
		return DigestUtils.md5Hex(new FileInputStream(sourceFile));
	}
}
