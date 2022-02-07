package org.hivedrive.server.controller;

import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.server.entity.PartEntity;
import org.hivedrive.server.service.NodeKeysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

@RestController
public class AuthenticationController {

	@Autowired
	private NodeKeysService serverKeysService;
	
	@GetMapping("whoAreYou")
	public ResponseEntity<NodeTO> get() {
		NodeTO nodeTO = new NodeTO();
		nodeTO.setIpAddress("localhost:8080");
		nodeTO.setPublicKey(serverKeysService.getPublicAsymetricKeyAsString());
		return new ResponseEntity<>(nodeTO, HttpStatus.OK);
	}
	
}
