package org.hivedrive.cmd.command;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.model.RepositoryConfigFileData;
import org.hivedrive.cmd.service.RepositoryConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component("init")
@Command(name = "init", mixinStandardHelpOptions = true, version = "0.1", description = "")
public class InitCommand implements Runnable {

	@Autowired
	private RepositoryConfigService config;
	
	@Option(names = {"-dir", "--directory"}, description = "")
    private File repositoryDirectory = new File(System.getProperty("user.dir"));
	
	@Option(names = {"-name", "--name"}, description = "")
    private String repositoryName;
    
    @Option(names = {"-k", "--key"}, description = "")
    private File key;
	
    @PostConstruct
    public void init(){
    	if(repositoryName == null) {
    		repositoryName = repositoryDirectory.getName();
    	}
    }
    
	@Override
	public void run() {
		config.initConfig(key, repositoryName);
	}

	
}
