package org.hivedrive.cmd.command;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;

import javax.crypto.SecretKey;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.hivedrive.cmd.jdbc.JDBCUtils;
import org.hivedrive.cmd.model.UserKeys;
import org.hivedrive.cmd.service.UserKeysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Multimaps;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component("generateKeys")
@Command(name = "generateKeys", mixinStandardHelpOptions = true, version = "0.1",
description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
public class GenerateKeyCommand implements Runnable {

	@Parameters(index = "0", description = "File to store keys")
    private File keyFile;

	@Autowired
    private UserKeysService userKeysService;
	
	@Override
	public void run() {
		UserKeys keys = userKeysService.generateNewKeys();
    	
    	userKeysService.save(keyFile);
    	System.out.println("Keys generated");
	}
}
