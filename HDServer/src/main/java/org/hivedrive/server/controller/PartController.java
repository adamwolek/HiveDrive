package org.hivedrive.server.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.status.PartStatus;
import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.entity.PartEntity;
import org.hivedrive.server.repository.NodeRepository;
import org.hivedrive.server.repository.PartRepository;
import org.hivedrive.server.service.PartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;

@RestController
@RequestMapping("/part")
public class PartController {

	private Logger logger = LoggerFactory.getLogger(PartController.class);
	
	private PartService partService;
	private SenderInfo senderInfo;
	private NodeRepository nodeRepository;
	private PartRepository partRepository;


	@Autowired
	public PartController(PartService service, NodeRepository nodeRepository, SenderInfo senderInfo, 
			PartRepository partRepository) {
		this.partService = service;
		this.nodeRepository = nodeRepository;
		this.senderInfo = senderInfo;
		this.partRepository = partRepository;
	}

	@PostMapping
	public ResponseEntity<Long> post(@RequestBody PartTO part) {
		System.out.println("Order: " + part.getOrderInGroup());
		part.setOwnerId(senderInfo.getSenderPublicKey());
		NodeEntity senderNode = nodeRepository.findByPublicKey(senderInfo.getSenderPublicKey());
		if(senderNode == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		} else if (!partService.isAbleToAdd(part, senderInfo)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		//docelowo
		part.setStatus(PartStatus.WAITING_FOR_APPROVAL);
		part.setStatus(PartStatus.ACCEPTED);
		PartEntity entity = partService.saveOrUpdate(part);
		if (entity != null) {
			return new ResponseEntity<>(entity.getId(), HttpStatus.ACCEPTED);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@DeleteMapping("/{partId}")
	public ResponseEntity<Void> delete(@PathVariable(name= "partId") Long partId) throws IOException {
		logger.debug("Delete part: " + partId);
		PartEntity part = partRepository.getById(partId);
		if (!partService.isAbleToDelete(part, senderInfo)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		FileUtils.forceDelete(new File(part.getPathToPart()));
		if (partService.delete(partId)) {
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	

	@PutMapping
	public ResponseEntity<Void> put(@RequestBody PartTO part) {
		if (partService.isAbleToUpdate(part, senderInfo)) {
			PartEntity entity = partService.saveOrUpdate(part);
			if (entity != null) {
				return new ResponseEntity<>(HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
	}
	

	@GetMapping
	ResponseEntity<Collection<PartTO>> get(
			@RequestParam(name = "id", required = false) Long id, 
			@RequestParam(name = "repository", required = false) String repository) {
		if(id == null) {
			Collection<PartTO> parts = partService.get(senderInfo.getSenderPublicKey(), repository);
			return new ResponseEntity<>(parts, HttpStatus.OK);
		} else {
			PartTO part = partService.getById(id);
			if(!part.getOwnerId().equals(senderInfo.getSenderPublicKey())) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			return new ResponseEntity<>(Lists.newArrayList(part), HttpStatus.OK);
		}
	}
	
	@GetMapping("/{fileId}/exists")
	ResponseEntity<Boolean> doesExists(@PathVariable(name= "fileId") String fileId) {
		boolean doesExist = partService.doesExist(fileId);
		logger.info("File globalId " + fileId + " does " + (doesExist ? "" : "NOT") + " exist");
		return new ResponseEntity<>(doesExist, HttpStatus.OK);
	}
	
	@GetMapping("/all")
	List<PartTO> getAll() {
		List<PartTO> allParts = partService.findAllParts();
		return allParts;
	}

	@PostMapping(path = "/content/{partId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	ResponseEntity<Void> postContent(@PathVariable Long partId, @RequestPart(name = "part") MultipartFile content) throws IOException {
		Optional<PartEntity> part = partRepository.findById(partId);
		if(part.isPresent()) {
			File savedFile = partService.createFileForPart(part.get(), content.getBytes());
			logger.info("Content for partId: " + partId + " received and saved in file: " + savedFile.getAbsolutePath());
			return new ResponseEntity<>(HttpStatus.OK);
		} else { 
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@PutMapping(path = "/content/{partId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	ResponseEntity<Void> putContent(@PathVariable Long partId, @RequestPart(name = "part") MultipartFile content) throws IOException {
		Optional<PartEntity> part = partRepository.findById(partId);
		if(part.isPresent()) {
			PartEntity partEntity = part.get();
			File partFile = new File(partEntity.getPathToPart());
			if(partFile.exists()) {
				FileUtils.forceDelete(partFile);
			}
			partEntity.setSize(0);
			partEntity.setPathToPart(null);
			partEntity.setSpaceId(null);
			partRepository.save(partEntity);
			File savedFile = partService.createFileForPart(partEntity, content.getBytes());
			logger.info("Content for partId: " + partId + " received and saved in file: " + savedFile.getAbsolutePath());
			return new ResponseEntity<>(HttpStatus.OK);
		} else { 
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping(path = "/content/{partId}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
	ResponseEntity<Resource> getContent(@PathVariable Long partId) throws FileNotFoundException {
		Optional<PartEntity> part = partRepository.findById(partId);
		if(part.isPresent()) {
			File file = new File(part.get().getPathToPart());
			InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
			return ResponseEntity.ok().contentLength(file.length())
					.contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

}
