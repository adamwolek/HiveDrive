package org.hivedrive.cmd.exception;

public class GenerateKeysError extends RuntimeException {

	public GenerateKeysError(Exception e) {
		super(e);
	}

}
