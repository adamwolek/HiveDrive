package org.hivedrive.server.controller;

import java.io.IOException;

import org.hivedrive.cmd.service.common.AddressService;
import org.hivedrive.cmd.service.common.UserKeysService;
import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.server.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

	@Autowired
	private NodeService nodeService;
	
	@GetMapping("whoAreYou")
	public ResponseEntity<NodeTO> get() throws IOException {
		return new ResponseEntity<>(nodeService.getMe(), HttpStatus.OK);
	}
	
}
 