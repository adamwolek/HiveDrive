package org.hivedrive.server.controller;

import java.util.List;

import org.hivedrive.server.mappers.NodeMapper;
import org.hivedrive.server.service.NodeService;
import org.hivedrive.server.to.NodeTO;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Autowired
	private NodeMapper mapper;
	
	@PostMapping
	void post(@RequestBody NodeTO node) {
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
