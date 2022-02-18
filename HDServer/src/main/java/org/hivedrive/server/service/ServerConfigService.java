package org.hivedrive.server.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.google.common.io.Files;

@Service
public class ServerConfigService {

	private List<File> locationsWhereYouCanSaveFiles;
	
	@PostConstruct
	void init() {
		this.locationsWhereYouCanSaveFiles = new ArrayList<>();
		this.locationsWhereYouCanSaveFiles.add(Files.createTempDir());
	}
	
	public List<File> getLocationsWhereYouCanSaveFiles() {
		return this.locationsWhereYouCanSaveFiles;
	}

	
	
}
