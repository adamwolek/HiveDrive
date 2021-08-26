package org.hivedrive.cmd.exception;

public class LoadingKeysError extends RuntimeException {

	public LoadingKeysError(Exception e) {
		super(e);
	}

}
