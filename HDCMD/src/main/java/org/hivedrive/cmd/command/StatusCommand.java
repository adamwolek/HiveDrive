package org.hivedrive.cmd.command;

import org.hivedrive.cmd.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Lazy
@Component("status")
@Command(name = "status", mixinStandardHelpOptions = true, version = "0.1", description = "Prints the checksum (MD5 by default) of a file to STDOUT.")

public class StatusCommand implements Runnable {

	@Option(names = { "-node", "--node" }, description = "")
	private String nodeAddress;
	
	private StatusService statusService;

	private Logger logger = LoggerFactory.getLogger(StatusCommand.class);
	
	@Autowired
	public StatusCommand(StatusService statusService) {
		this.statusService = statusService;
	}
	
	@Override
	public void run() {
		if(nodeAddress != null) {
			String status = statusService.getNodeStatistics(nodeAddress);
			logger.info(status);
		}
		
	}
	
}
