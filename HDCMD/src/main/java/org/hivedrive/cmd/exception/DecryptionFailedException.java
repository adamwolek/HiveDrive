package org.hivedrive.cmd.exception;

public class DecryptionFailedException extends RuntimeException {

	public DecryptionFailedException(Exception e) {
		super(e);
	}

}
