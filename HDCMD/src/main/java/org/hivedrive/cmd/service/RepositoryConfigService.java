package org.hivedrive.cmd.service;

import java.io.File;
import java.io.IOException;

import org.hivedrive.cmd.model.RepositoryConfigFileData;
import org.hivedrive.cmd.service.common.UserKeysService;
import org.hivedrive.cmd.tool.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.SerializationFeature;

@Lazy
@Service
public class RepositoryConfigService {

	private File repositoryDirectory;

	private RepositoryConfigFileData config;

	@Autowired
	private UserKeysService userKeysService;
	
	private void init() {
		if (getConfigFile().exists()) {
			this.config = loadConfig();
		}
	}

	private RepositoryConfigFileData loadConfig()  {
		var mapper = JSONUtils.mapper();
		try {
			return mapper.readValue(getConfigFile(), RepositoryConfigFileData.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void initConfig(File keyFile, String repositoryName, File newRepositoryDirectory) {
		try {
			this.repositoryDirectory = newRepositoryDirectory;
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

	private void save(RepositoryConfigFileData config) throws StreamWriteException, DatabindException, IOException {
		var mapper = JSONUtils.mapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.writeValue(getConfigFile(), config);
	}

	private File getConfigFile() {
		File configFolder = getConfigFolder();
		return new File(configFolder, "hivedriveConfig");
	}

	private File getConfigFolder() {
		File configFolder = new File(repositoryDirectory, ".hivedrive");
		configFolder.mkdir();
		return configFolder;
	}

	public File getRepositoryDirectory() {
		return repositoryDirectory;
	}

	public void setRepositoryDirectory(File repositoryDirectory)  {
		this.repositoryDirectory = repositoryDirectory;
		init();
		if(config != null) {
			userKeysService.loadKeys(new File(config.getKeysPath()));
		}
	}

	public RepositoryConfigFileData getConfig() {
		return config;
	}

	public void setConfig(RepositoryConfigFileData config) {
		this.config = config;
	}

}
