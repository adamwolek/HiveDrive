package org.hivedrive.server.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hivedrive.cmd.to.SpaceUsageTO;
import org.hivedrive.server.repository.PartRepository;
import org.hivedrive.server.service.SpaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/space")
public class SpaceController {

	private Logger logger = LoggerFactory.getLogger(SpaceController.class);
	
	@Autowired
	private SpaceService spaceService;
	
	@Autowired
	private PartRepository partRepository;

	@Autowired
	public SpaceController(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	@GetMapping("/default")
	public ResponseEntity<Integer> get() {
		
		Integer defaultSpace = spaceService.getDefaultSpace();
		if (defaultSpace != null) {
			return new ResponseEntity<>(defaultSpace, HttpStatus.ACCEPTED);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@GetMapping("/spaceUsage")
	public ResponseEntity<Map<String, Long>> spaceUsage() {
		Map<String, Long> usage = partRepository.getUsageOfSpaces().stream()
		.collect(Collectors.toMap(pair -> (String)pair[0], pair -> (Long)pair[1]));
		return new ResponseEntity<>(usage, HttpStatus.ACCEPTED);
	}

}
