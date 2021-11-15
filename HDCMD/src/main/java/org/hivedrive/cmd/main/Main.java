package org.hivedrive.cmd.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import picocli.CommandLine;
import picocli.spring.PicocliSpringFactory;


@SpringBootApplication(scanBasePackages={"org.hivedrive.cmd"})
public class Main {
	public static void main(String... args) {
		System.exit(SpringApplication.exit(
            SpringApplication.run(Main.class, args))
        );
    }
	
}
