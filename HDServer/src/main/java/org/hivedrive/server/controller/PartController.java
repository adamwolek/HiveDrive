package org.hivedrive.server.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.entity.PartEntity;
import org.hivedrive.server.repository.NodeRepository;
import org.hivedrive.server.repository.PartRepository;
import org.hivedrive.server.service.PartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/part")
public class PartController {

	private PartService partService;
	private SenderInfo senderInfo;
	private NodeRepository nodeRepository;

	@Autowired
	public PartController(PartService service, NodeRepository nodeRepository, SenderInfo senderInfo) {
		this.partService = service;
		this.nodeRepository = nodeRepository;
		this.senderInfo = senderInfo;
	}

	@PostMapping
	public ResponseEntity<Void> post(@RequestBody PartTO part) {
		part.setOwnerId(senderInfo.getSenderPublicKey());
		NodeEntity senderNode = nodeRepository.findByPublicKey(senderInfo.getSenderPublicKey());
		if(senderNode == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		} else if (!partService.isAbleToAdd(part)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		PartEntity entity = partService.saveOrUpdate(part);
		if (entity != null) {
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@PutMapping
	public ResponseEntity<Void> put(@RequestBody PartTO part) {
		if (partService.isAbleToUpdate(part)) {
			PartEntity entity = partService.saveOrUpdate(part);
			if (entity != null) {
				return new ResponseEntity<>(HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
	}
	

	@GetMapping
	PartTO get(@RequestParam("publicKey") String publicKey,
			@RequestParam("repository") String repository, 
			@RequestParam("groupId") String groupId,
			@RequestParam("orderInGroup") Integer orderInGroup) {
		PartTO part = partService.get(publicKey, repository, groupId, orderInGroup);
		return part;
	}

	@GetMapping("/all")
	List<PartTO> getAll() {
		List<PartTO> allParts = partService.findAllParts();
		return allParts;
	}

	@PostMapping(path = "/content", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	void postContent(@RequestPart MultipartFile content) {

	}

	@GetMapping(path = "/content", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
	ResponseEntity<Resource> getContent() throws FileNotFoundException {
		File file = new File("");
		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
		return ResponseEntity.ok().contentLength(file.length())
				.contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
	}

}
