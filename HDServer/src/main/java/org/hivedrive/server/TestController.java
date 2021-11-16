package org.hivedrive.server;

import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.repository.NodeRepository;
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
		
		NodeEntity node = new NodeEntity();
		node.setPublicKey("test" + System.currentTimeMillis());
		repository.save(node);
		
		Iterable<NodeEntity> findAll = repository.findAll();
	    return ResponseEntity.ok().body("test2");
	  }
	
}
