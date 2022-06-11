

package org.hivedrive.cmd.command;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.hivedrive.cmd.service.RepositoryConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Lazy
@Component("init")
@Command(name = "init", mixinStandardHelpOptions = true, version = "0.1", description = "")
public class InitCommand implements Runnable {

	@Autowired
	private RepositoryConfigService config;
	
	@Option(names = { "-dir", "--directory" }, description = "")
	private File repositoryDirectory = new File(System.getProperty("user.dir"));

	@Option(names = { "-name", "--name" }, description = "")
	private String repositoryName;

	@Option(names = { "-k", "--key" }, description = "")
	private File key;
	
	@PostConstruct
	public void init() {
		if (repositoryName == null) {
			repositoryName = repositoryDirectory.getName();
		}
	}

	@Override
	public void run() {
		config.setRepositoryDirectory(repositoryDirectory);
		config.initConfig(key, repositoryName, repositoryDirectory);
	}

}
