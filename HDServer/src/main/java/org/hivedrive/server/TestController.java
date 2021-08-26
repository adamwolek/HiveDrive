package org.hivedrive.server;

import org.hivedrive.cmd.model.UserKeys;
import org.hivedrive.cmd.service.AsymetricEncryptionService;
import org.hivedrive.server.entity.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/drive")
public class TestController {

	@Autowired
	NodeRepository repository;
	
	@GetMapping("/part")
	  public ResponseEntity<String> part() {
		
		Node node = new Node();
		node.setPublicKey("test" + System.currentTimeMillis());
		repository.save(node);
		
		Iterable<Node> findAll = repository.findAll();
	    return ResponseEntity.ok().body("test2");
	  }
	
}
