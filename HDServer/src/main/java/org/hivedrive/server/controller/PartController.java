package org.hivedrive.server.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.hivedrive.server.to.PartTO;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/part")
public class PartController {

	@PostMapping
	void post(@RequestBody PartTO part) {
		
	}
	
	@PutMapping
	void put(@RequestBody PartTO part) {
		
	}
	
	@GetMapping
	PartTO get() {
		
		return null;
	}
	
	@GetMapping("/all")
	List<PartTO> getAll() {
		
		return null;
	}
	
	@PostMapping(path = "/content", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	void postContent(@RequestPart MultipartFile content) {
		
	}
	
	@GetMapping(path = "/content", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
	ResponseEntity<Resource> getContent() throws FileNotFoundException {
		File file = new File("");
		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
		return ResponseEntity.ok()
	            .contentLength(file.length())
	            .contentType(MediaType.APPLICATION_OCTET_STREAM)
	            .body(resource);
	}
	
}
