package org.hivedrive.server.controller;

import java.io.IOException;

import org.hivedrive.cmd.service.common.AddressService;
import org.hivedrive.cmd.service.common.UserKeysService;
import org.hivedrive.cmd.to.NodeTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

	@Autowired
	private UserKeysService serverKeysService;
	
	@Autowired
	private AddressService addressHelper;
	
	@GetMapping("whoAreYou")
	public ResponseEntity<NodeTO> get() throws IOException {
		NodeTO nodeTO = new NodeTO();
		nodeTO.setAddress(addressHelper.getGlobalAddress());
		nodeTO.setPublicKey(serverKeysService.getKeys().getPublicAsymetricKeyAsString());
		return new ResponseEntity<>(nodeTO, HttpStatus.OK);
	}
	
}
