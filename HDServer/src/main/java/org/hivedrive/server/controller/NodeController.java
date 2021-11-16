package org.hivedrive.server.controller;

import java.util.List;

import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.service.NodeService;
import org.hivedrive.server.to.NodeTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/node")
public class NodeController {

	
	@Autowired
	private NodeService service;
	
	public NodeController(NodeService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<Void> post(@RequestBody NodeTO node) {
		NodeEntity entity = service.post(node);
		if (entity != null) {
			return new ResponseEntity<>(HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}
	
	@PutMapping
	void put(@RequestBody NodeTO node) {
		
	}
	
	@GetMapping
	NodeTO get() {
		
		return new NodeTO();
	}
	
	@GetMapping("/all")
	List<NodeTO> getAll() {
		
		return null;
	}
	
}
