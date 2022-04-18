package org.hivedrive.cmd.main;

import java.util.Arrays;

import java.util.Map;

import org.hivedrive.cmd.command.HiveDriveCommand;
import org.hivedrive.cmd.service.SymetricEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;

@Component
public class MainApplicationRunner implements CommandLineRunner, ExitCodeGenerator {

	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private IFactory factory;
	
	private int exitCode;

	
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
	
//    @Override
//    public void run(String... args) throws Exception {
//    	Map<String,Object> beans = context.getBeansWithAnnotation(Command.class);
//    	for (Object command : beans.values()) {
//    		Command commandAnnotation = command.getClass().getAnnotation(Command.class);
//    		if(commandAnnotation.name().equals(args[0])) {
//    			String[] argsWithoutFirst = Arrays.copyOfRange(args, 1, args.length);
//    			exitCode = new CommandLine(command, factory).execute(argsWithoutFirst);
//    			return;
//    		}
//		}
//    	System.out.println("Cannot find required Command");
//    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}