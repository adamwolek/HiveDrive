package org.hivedrive.cmd.main;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
public class MainApplicationRunner implements CommandLineRunner, ExitCodeGenerator {

	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private IFactory factory;
	
	private int exitCode;

	public void runCommand(String command) {
		try {
			this.run(command.split("\\s+"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void run(String... args) throws Exception {
		Object commandBean = context.getBean(args[0]);
		if (commandBean != null) {
			String[] argsWithoutFirst = Arrays.copyOfRange(args, 1, args.length);
			exitCode = new CommandLine(commandBean, factory).execute(argsWithoutFirst);
		} else {
			System.out.println("Cannot find required Command");
		}
	}
	
    @Override
    public int getExitCode() {
        return exitCode;
    }
}