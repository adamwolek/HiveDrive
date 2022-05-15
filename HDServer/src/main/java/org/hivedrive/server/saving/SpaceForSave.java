package org.hivedrive.server.saving;

import java.io.File;

import org.hivedrive.server.repository.PartRepository;
import org.hivedrive.server.service.PartService;
import org.springframework.util.unit.DataSize;

public class SpaceForSave {

	private File directory;
	private DataSize size;
	
	private PartRepository partRepository;
	
	public SpaceForSave(PartRepository partRepository) {
		this.partRepository = partRepository;
	}
	
	public DataSize spaceLeft() {
		Long usedSize = partRepository.sizeForPath(directory.getAbsolutePath());
		if(usedSize == null) {
			usedSize = 0L;
		}
		return DataSize.ofBytes(this.size.toBytes() - usedSize);
	}
	
	public File getDirectory() {
		return directory;
	}
	public void setDirectory(File directory) {
		this.directory = directory;
	}
	public DataSize getSize() {
		return size;
	}
	public void setSize(DataSize size) {
		this.size = size;
	}
	
	
	
}
