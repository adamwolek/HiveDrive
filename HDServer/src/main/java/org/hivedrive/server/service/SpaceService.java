package org.hivedrive.server.service;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.springframework.stereotype.Service;

@Service
public class SpaceService {

	public Integer getDefaultSpace() {
		int freeSpace= 0;
		for (Path path : FileSystems.getDefault().getRootDirectories()) {
			freeSpace += path.toFile().getFreeSpace();
		}
		return freeSpace;
	}

}
