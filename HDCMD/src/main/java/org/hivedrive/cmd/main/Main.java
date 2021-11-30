package org.hivedrive.cmd.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

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
