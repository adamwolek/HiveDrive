package org.hivedrive.cmd.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hivedrive.cmd.exception.EncryptionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.google.common.collect.Streams;

@Service
public class SymetricEncryptionService {
	
	private SecretKey secretKey;
	private Cipher cipher;
//	private IvParameterSpec iv = generateIv();

	@Autowired 
	UserKeysService userKeysService;

	@PostConstruct
	private void init() {
	    this.secretKey = userKeysService.getKeys().getPrivateSymetricKey();
	    try {
			this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	
	public static IvParameterSpec generateIv() {
	    byte[] iv = new byte[16];
	    new SecureRandom().nextBytes(iv);
	    return new IvParameterSpec(iv);
	}
	
	public String encrypt(String input)  {
	    try {
		    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		    byte[] cipherText = cipher.doFinal(input.getBytes());
		    return Base64.getEncoder()
		        .encodeToString(cipherText);
		} catch (Exception e) {
			throw new EncryptionFailedException(e);
		}
	}
	
	public String decrypt(String cipherText) {
	    try {
		    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		    cipher.init(Cipher.DECRYPT_MODE, secretKey);
		    byte[] plainText = cipher.doFinal(Base64.getDecoder()
		        .decode(cipherText));
		    return new String(plainText);
		} catch (Exception e) {
			throw new EncryptionFailedException(e);
		}
	}
	
	
	public void encrypt(File inputFile, File outputFile) {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] iv = cipher.getIV();
			try (FileInputStream fis = new FileInputStream(inputFile);
					FileOutputStream fileOut = new FileOutputStream(outputFile); 
					CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher)) {
				//we put IV on begin of file, because we will need this during decryption
				fileOut.write(iv); 
				while(fis.available() > 0) {
					byte[] bytes = fis.readNBytes(1024);
					cipherOut.write(bytes);
				}
			}
		} catch (Exception e) {
			throw new EncryptionFailedException(e);
		}
	}
	


	
	public void decrypt(File inputFile, File outputFile) {
	    try (FileInputStream fileIn = new FileInputStream(inputFile)) {
	        byte[] fileIv = new byte[16];
	        fileIn.read(fileIv);
	        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(fileIv));
	        try (
	                CipherInputStream cipherIn = new CipherInputStream(fileIn, cipher);
	                FileOutputStream fos = new FileOutputStream(outputFile)
	            ) {
	        	IOUtils.copy(cipherIn, fos);
		            
	        } 
	    } catch (Exception e) {
	    	throw new EncryptionFailedException(e);
		}
	}
	

}
