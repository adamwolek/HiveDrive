package org.hivedrive.cmd.common;

import org.hivedrive.cmd.config.TestConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


public class BeforeAllAction {

	
	@BeforeAll
	public static void init(){
		System.out.println("BeforeAll init() method called");
	}
}
