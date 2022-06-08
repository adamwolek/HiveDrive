package org.hivedrive.cmd.model;

import java.io.File;

public class TempFile {

	private File originFile;
	private File tempFile;
	
	public TempFile(File sourceFile) {
		this.originFile = sourceFile;
		this.tempFile = sourceFile;
	}
	
	private TempFile(TempFile tempFile) {
		this.originFile = tempFile.originFile;
		this.tempFile = tempFile.tempFile;
	}
	
	public File getOriginFile() {
		return originFile;
	}

	public void setOriginFile(File originFile) {
		this.originFile = originFile;
	}

	public File getTempFile() {
		return tempFile;
	}
	public void setTempFile(File tempFile) {
		this.tempFile = tempFile;
	}
	
	@Override
	public TempFile clone() {
		return new TempFile(this);
	}
	
	
}
