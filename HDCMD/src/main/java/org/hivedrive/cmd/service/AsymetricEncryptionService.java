package org.hivedrive.cmd.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hivedrive.cmd.exception.DecryptionFailedException;
import org.hivedrive.cmd.exception.EncryptionFailedException;

public class AsymetricEncryptionService {

	private PrivateKey privateAsymetricKey;
	private PublicKey publicAsymetricKey;
	private Key key;
	private Cipher cipher;

	public AsymetricEncryptionService(PrivateKey privateAsymetricKey) {
		this();
		this.key = privateAsymetricKey;
		this.privateAsymetricKey = privateAsymetricKey;
	}

	public AsymetricEncryptionService(PublicKey publicAsymetricKey) {
		this();
		this.key = publicAsymetricKey;
		this.publicAsymetricKey = publicAsymetricKey;
	}

	private AsymetricEncryptionService() {
		try {
			this.cipher = Cipher.getInstance("RSA");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new RuntimeException(e);
		}
	}

	public void encrypt(File inputFile, File outputFile) {
		try {
	        cipher.init(Cipher.ENCRYPT_MODE, key);
	        byte[] sourceFileBytes = FileUtils.readFileToByteArray(inputFile);
	        byte[] encryptedFileBytes = cipher.doFinal(sourceFileBytes);
	        try (FileOutputStream stream = new FileOutputStream(outputFile)) {
	            stream.write(encryptedFileBytes);
	        }
	    } catch (Exception e) {
	    	throw new EncryptionFailedException(e);
		}
	}

	public void decrypt(File inputFile, File outputFile) {
		try {
	        cipher.init(Cipher.DECRYPT_MODE, key);
	        byte[] encryptedFileBytes = FileUtils.readFileToByteArray(inputFile);
	        byte[] decryptedFileBytes = cipher.doFinal(encryptedFileBytes);
	        try (FileOutputStream stream = new FileOutputStream(outputFile)) {
	            stream.write(decryptedFileBytes);
	        }
	    } catch (Exception e) {
	    	throw new DecryptionFailedException(e);
		}
		
	}

}
