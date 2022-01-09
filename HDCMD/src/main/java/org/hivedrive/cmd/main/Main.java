package org.hivedrive.cmd.main;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={"org.hivedrive.cmd"})
public class Main {
	public static void main(String... args) {
		System.exit(SpringApplication.exit(
            SpringApplication.run(Main.class, args))
        );
    }
	
}
