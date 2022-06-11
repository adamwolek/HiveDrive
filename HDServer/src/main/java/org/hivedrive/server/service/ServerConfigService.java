package org.hivedrive.server.service;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.hivedrive.cmd.service.common.UserKeysService;
import org.hivedrive.server.config.LocalConfiguration;
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

@Service
public class ServerConfigService {

	private List<SpaceForSave> spacesForSave;
	
	 Logger logger = LoggerFactory.getLogger(ServerConfigService.class);
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private PartRepository partRepository;
	
	@Autowired
	private UserKeysService userKeysService;
	
	@Value("${hdConfigPath}")
    private File hdConfigPath;
	
	@Value("${keysPath}")
    private File keysPath;

	private LocalConfiguration localConfig;
	
	@PostConstruct
	void init() {
		this.localConfig = loadLocalConfig();
		this.spacesForSave = extractSpaces();
		loadKeys();
	}

	private void loadKeys() {
		if(keysPath.exists()) {
			userKeysService.loadKeys(keysPath);
		} else {
			userKeysService.generateNewKeys();
			userKeysService.saveKeptKeys(hdConfigPath);
		}
		
	}

	
	private LocalConfiguration loadLocalConfig() {
		try {
			if(hdConfigPath != null) {
				return loadConfig(hdConfigPath);
			} else {
				return Stream.of(new File(getJarPath()).listFiles())
					.filter(file -> file.getName().equals("node-config.json"))
					.findAny()
					.map(this::loadConfig)
					.get();
			}
		} catch (Exception e) {
			logger.error("Can't load local configuration. Shutdown node .", e);
			((ConfigurableApplicationContext) context).close();
			return null;
		}
	}
	

	private List<SpaceForSave> extractSpaces() {
		return localConfig.getSpaces().stream()
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

	
	
	public void setCentralServers(List<String> centralServers) {
		localConfig.setCentralServers(centralServers);
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

	public List<String> getCentralServers() {
		return localConfig.getCentralServers();
	}

	public boolean isTestMode() {
		return localConfig.isTestMode();
	}

	
	
	
}
