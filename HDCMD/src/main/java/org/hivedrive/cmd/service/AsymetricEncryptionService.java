package org.hivedrive.cmd.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.exception.DecryptionFailedException;
import org.hivedrive.cmd.exception.EncryptionFailedException;
import org.hivedrive.cmd.model.UserKeys;
import org.hivedrive.cmd.service.common.UserKeysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class AsymetricEncryptionService {

	@Autowired
	UserKeysService userKeysService;

	private PrivateKey privateAsymetricKey;
	private PublicKey publicAsymetricKey;
	private Cipher cipher;

	@PostConstruct
	public void init() {
		userKeysService.onKeysLoaded(() -> {
			UserKeys keys = userKeysService.getKeys();
			if (keys != null) {
				this.privateAsymetricKey = keys.getPrivateAsymetricKey();
				this.publicAsymetricKey = keys.getPublicAsymetricKey();
				try {
					this.cipher = Cipher.getInstance("RSA");
				} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	public void encryptWithPrivateKey(File inputFile, File outputFile) {
		encrypt(inputFile, outputFile, privateAsymetricKey);
	}

	public void encryptWithPublicKey(File inputFile, File outputFile) {
		encrypt(inputFile, outputFile, publicAsymetricKey);
	}

	public void encrypt(File inputFile, File outputFile, Key key) {
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

	public void encryptAnywayWithPrivateKey(File inputFile, File outputFile) {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, this.privateAsymetricKey);
			try (FileInputStream fis = new FileInputStream(inputFile);
					FileOutputStream stream = new FileOutputStream(outputFile)) {
				while (fis.available() > 0) {
					byte[] sourceBytes = fis.readNBytes(240);
					byte[] encryptedFileBytes = cipher.doFinal(sourceBytes);
					stream.write(encryptedFileBytes);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			throw new EncryptionFailedException(e);
		}
	}

	public void decryptWithPrivateKey(File inputFile, File outputFile) {
		decrypt(inputFile, outputFile, privateAsymetricKey);
	}

	public void decryptWithPublicKey(File inputFile, File outputFile) {
		decrypt(inputFile, outputFile, publicAsymetricKey);
	}

	public void decrypt(File inputFile, File outputFile, Key key) {
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
