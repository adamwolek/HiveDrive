package org.hivedrive.cmd.model;

import java.io.File;

public class TempFile {

	private File originFile;
	private File tempFile;
	private PartInfo partInfo;
	
	public TempFile(File originFile) {
		this.originFile = originFile;
	}
	
	public TempFile(File originFile, PartInfo partInfo) {
		this(originFile);
		this.partInfo = partInfo;
	}
	
	private TempFile(TempFile tempFile) {
		this.originFile = tempFile.originFile;
		this.tempFile = tempFile.tempFile;
		this.partInfo = tempFile.partInfo;
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
	
	
	
	public PartInfo getPartInfo() {
		return partInfo;
	}

	public void setPartInfo(PartInfo partInfo) {
		this.partInfo = partInfo;
	}

	@Override
	public TempFile clone() {
		return new TempFile(this);
	}
	
	
}
