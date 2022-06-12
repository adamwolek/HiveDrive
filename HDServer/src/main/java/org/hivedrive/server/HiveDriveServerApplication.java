package org.hivedrive.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({
	"org.hivedrive.server", 
	"org.hivedrive.cmd.service.common", 
	"org.hivedrive.cmd.session"})
@SpringBootApplication
public class HiveDriveServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HiveDriveServerApplication.class, args);
		
		//comment
	}

}
