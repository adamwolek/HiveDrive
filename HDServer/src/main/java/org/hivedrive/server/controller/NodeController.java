package org.hivedrive.server.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.helpers.NodeJsonHelper;
import org.hivedrive.server.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	/**
	 * Adding another node
	 * @param node
	 * @return
	 */
	@PostMapping
	public ResponseEntity<Void> post(@RequestBody NodeTO node) {
		
		Thread thread = new Thread() {
			public void run() {
				System.out.println("dupa");
				
			}
		};
		thread.start();
		
		
		
		ExecutorService executor = Executors.newFixedThreadPool(4);
		executor.execute(() -> {
			System.out.println("dupa2");
		});
		executor.execute(() -> {
			System.out.println("dupa3");
		});
		executor.execute(() -> {
			System.out.println("dupa4");
			
		});
		
		Future<String> wynikObliczen = executor.submit(() -> {
			System.out.println("dupa5");
			return "wynikObliczen";
		});
		

		List<String> modulyDoZbudowania = new ArrayList<>();
		List<Future<String>> przyszleWyniki = modulyDoZbudowania.stream()
		.map(modul -> {
			return executor.submit(() -> {
				//terminal.execute("gradle build " + modul);
				return "zbudowanyKatalog";
			});
		}).collect(Collectors.toList());
		
		List<String> wyniki = przyszleWyniki.stream().map(przyszlyWnik -> przyszlyWnik.get()).collect(Collectors.toList());
		
		if (service.isAbleToAdd(node)) {
			NodeEntity entity = service.saveOrUpdate(node);
			if (entity != null) {
				return new ResponseEntity<>(HttpStatus.CREATED);
			}
		}
		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
	}
	
	@PutMapping
	public ResponseEntity<Void> put(@RequestBody NodeTO node) {
		if (service.isAbleToUpdate(node)) {
			NodeEntity entity = service.saveOrUpdate(node);
			if (entity != null) {
				return new ResponseEntity<>(HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
	}
	
	@GetMapping("/{publicKey}")
	public ResponseEntity<String> get(@PathVariable String publicKey) {
		NodeTO nodeByPublicKey = service.getNodeByPublicKey(publicKey);
		if (nodeByPublicKey != null) {
			return new ResponseEntity<>(NodeJsonHelper.toJson(nodeByPublicKey), HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	/**
	 * 
	 * @return all nodes which are known by this unit
	 */
	@GetMapping("/all")
	public ResponseEntity<String> getAll() {
		List<NodeTO> all = service.getAll();
		return new  ResponseEntity<>(NodeJsonHelper.toJson(all), HttpStatus.OK);
	}
	
}
