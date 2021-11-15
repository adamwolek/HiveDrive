package org.hivedrive.cmd.service;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.hivedrive.cmd.command.PushCommand;
import org.hivedrive.cmd.model.RepositoryConfigFileData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class RepositoryConfigService {

	
	private File repositoryDirectory;

	private RepositoryConfigFileData config;
	
	@PostConstruct
	void init() throws StreamReadException, DatabindException, IOException {
		this.repositoryDirectory = new File(System.getProperty("user.dir"));
		if(getConfigFile().exists()) {
			this.config = loadConfig();
		}
	}
	
	private RepositoryConfigFileData loadConfig() throws StreamReadException, DatabindException, IOException {
		var mapper = new ObjectMapper();
		return mapper.readValue(getConfigFile(), RepositoryConfigFileData.class);
	}

	public void initConfig(File keyFile, String repositoryName) {
		try {
			RepositoryConfigFileData config = createNewConfig(keyFile, repositoryName);
			save(config);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private RepositoryConfigFileData createNewConfig(File keyFile, String repositoryName) {
		var config = new RepositoryConfigFileData();
		config.setKeysPath(keyFile.getAbsolutePath());
		config.setRepositoryName(repositoryName);
		return config;
	}

	private void save(RepositoryConfigFileData config) 
			throws StreamWriteException, DatabindException, IOException  {
		File configFolder = getConfigFolder();
		var mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.writeValue(getConfigFile(), config);
	}

	private File getConfigFile() {
		File configFolder = getConfigFolder();
		File configFile = new File(configFolder, "hivedriveConfig");
		return configFile;
	}
	
	private File getConfigFolder() {
		File configFolder = new File(repositoryDirectory, ".hivedrive");
		configFolder.mkdir();
		return configFolder;
	}

	public File getRepositoryDirectory() {
		return repositoryDirectory;
	}

	public void setRepositoryDirectory(File repositoryDirectory) {
		this.repositoryDirectory = repositoryDirectory;
	}

	public RepositoryConfigFileData getConfig() {
		return config;
	}

	public void setConfig(RepositoryConfigFileData config) {
		this.config = config;
	}
	
	
	
}
