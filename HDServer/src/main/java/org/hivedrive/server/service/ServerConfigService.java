package org.hivedrive.server.service;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.Streams;
import org.hivedrive.server.config.LocalConfiguration;
import org.hivedrive.server.config.SpaceConfig;
import org.hivedrive.server.repository.PartRepository;
import org.hivedrive.server.saving.SpaceForSave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.google.gson.Gson;

@Service
public class ServerConfigService {

	private List<SpaceForSave> spacesForSave;
	
	 Logger logger = LoggerFactory.getLogger(ServerConfigService.class);
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private PartRepository partRepository;
	
	@Value("${hdConfigPath}")
    private File hdConfigPath;
	
	@PostConstruct
	void init() {
		try {
			if(hdConfigPath != null) {
				this.spacesForSave = this.mapToSpacesToSave(hdConfigPath);
			} else {
				this.spacesForSave = Stream.of(new File(getJarPath()).listFiles())
					.filter(file -> file.getName().equals("node-config.json"))
					.findAny()
					.map(this::mapToSpacesToSave)
					.get();
			}
		} catch (Exception e) {
			logger.error("Can't load local configuration. Shutdown node .", e);
			((ConfigurableApplicationContext) context).close();
		}
	}

	private List<SpaceForSave> mapToSpacesToSave(File file) {
		return loadConfig(file).getSpaces().stream()
		.map(space -> {
			var spaceToSave = new SpaceForSave(partRepository);
			spaceToSave.setDirectory(new File(space.getPath()));
			spaceToSave.setSize(DataSize.parse(space.getSize()));
			return spaceToSave;
		}).collect(Collectors.toList());
	}

	private LocalConfiguration loadConfig(File file) {
		try {
			return new ObjectMapper().readValue(file, LocalConfiguration.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String getJarPath() throws URISyntaxException {
		return ServerConfigService.class
				.getProtectionDomain()
				.getCodeSource()
				.getLocation()
				.toURI()
				.getPath();
	}
	
	public List<SpaceForSave> getSpacesForSave() {
		return this.spacesForSave;
	}

	
	
}
