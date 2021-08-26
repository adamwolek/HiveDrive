package org.hivedrive.cmd;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;

import javax.crypto.SecretKey;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.hivedrive.cmd.jdbc.JDBCUtils;
import org.hivedrive.cmd.model.UserKeys;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "generateKey", mixinStandardHelpOptions = true, version = "0.1",
description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
public class GenerateKeyCommand implements Runnable {

	
	@Override
	public void run() {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			System.out.println("Start");
			Connection connection = JDBCUtils.getConnection();
			Statement statement = connection.createStatement();
			statement.execute("CREATE TABLE Persons (\n"
					+ "    PersonID int,\n"
					+ "    LastName varchar(255),\n"
					+ "    FirstName varchar(255),\n"
					+ "    Address varchar(255),\n"
					+ "    City varchar(255)\n"
					+ ");");
		} catch (Exception e) {
			e.printStackTrace();
		}
//    	UserKeys keys = UserKeys.generateNewKeys();
		
	}
	
	public static void main(String... args) {
        int exitCode = new CommandLine(new GenerateKeyCommand()).execute(args);
        System.exit(exitCode);
    }
}
