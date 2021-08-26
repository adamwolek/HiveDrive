package org.hivedrive.cmd.exception;

public class EncryptionFailedException extends RuntimeException {

	public EncryptionFailedException(Exception e) {
		super(e);
	}

}
