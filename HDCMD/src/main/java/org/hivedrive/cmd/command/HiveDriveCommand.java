package org.hivedrive.cmd.command;

import java.io.File;

import org.hivedrive.cmd.service.SymetricEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Lazy
@Component
@Command(name = "hivedrive", mixinStandardHelpOptions = true, version = "0.1", description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
public class HiveDriveCommand implements Runnable {

	@Parameters(index = "1", description = "The directory which will be synchronized.")
	private String task;

	@Option(names = { "-dir", "--directoryToSynchronize" }, description = "")
	private File directoryToSynchronize;

	@Option(names = { "-rId", "--repositoryId" }, description = "")
	private String repositoryId = "main";

	@Option(names = { "-k", "--key" }, description = "")
	private File key;

	@Autowired
	private SymetricEncryptionService encryptionService;

	@Override
	public void run() {
		System.out.println("test");

	}

}
