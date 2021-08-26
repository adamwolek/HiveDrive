package org.hivedrive.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.hivedrive.cmd.model.UserKeys;
import org.hivedrive.server.service.CryptographicNodeService;

@SpringBootTest
class HiveDriveServerApplicationTests {

	@Autowired
	private CryptographicNodeService service;
	
	@Test
	void keysLoaded() {
		assertNotNull(service.getKeys());
		
	}

}
