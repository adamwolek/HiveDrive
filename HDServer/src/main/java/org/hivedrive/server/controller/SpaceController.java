package org.hivedrive.server.controller;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hivedrive.cmd.service.common.UserKeysService;
import org.hivedrive.cmd.session.P2PSession;
import org.hivedrive.cmd.to.NodeSummary;
import org.hivedrive.cmd.to.SpaceTO;
import org.hivedrive.cmd.to.SpaceUsageTO;
import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.repository.NodeRepository;
import org.hivedrive.server.repository.PartRepository;
import org.hivedrive.server.service.NodeService;
import org.hivedrive.server.service.ServerConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
	private PartRepository partRepository;

	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ServerConfigService serverConfigService;
	
	@Autowired
	private UserKeysService userKeysService;
	
	@Autowired
	private ApplicationContext appContext;
	
	@GetMapping("/spaceUsage")
	public ResponseEntity<Map<String, Long>> spaceUsage() {
		Map<String, Long> usage = partRepository.getUsageOfSpaces().stream()
		.collect(Collectors.toMap(pair -> (String)pair[0], pair -> (Long)pair[1]));
		return new ResponseEntity<>(usage, HttpStatus.ACCEPTED);
	}
	
	@GetMapping("/summary")
	public ResponseEntity<NodeSummary> summary() {
		return new ResponseEntity<>(mySummary(), HttpStatus.ACCEPTED);
	}
	
	private NodeSummary mySummary() {
		NodeSummary node = new NodeSummary();
		node.setId(userKeysService.getKeys().getPublicAsymetricKeyAsString());
		node.setSpaces(partRepository.getUsageOfSpaces().stream()
		.map(row -> {
			SpaceTO space = new SpaceTO();
			space.setName((String)row[0]);
			space.setUsed((Long)row[1]);
			space.setTotal(getTotalSpace(space.getName()));
			space.setNumberOfParts(partRepository.countPartsBySpace(space.getName()));
			return space;
		}).collect(Collectors.toList()));
		return node;
	}
	
	private Long getTotalSpace(String name) {
		return serverConfigService.getSpacesForSave().stream()
		.filter(spaceFromConfig -> spaceFromConfig.getDirectory().getAbsolutePath().equals(name))
		.map(spaceFromConfig -> spaceFromConfig.getSize().toBytes())
		.findAny().get();
	}

	@GetMapping("/summary/all")
	public ResponseEntity<List<NodeSummary>> summaryForAllNodes() {
		List<NodeSummary> allNodes = new ArrayList<>();
		allNodes.add(this.mySummary());
		nodeService.findAllWithoutMe().stream()
		.map(node -> appContext.getBean(P2PSession.class).fromNodeToAddress(node.getAddress()))
		.map(session -> session.getSummary());
		
		return new ResponseEntity<>(allNodes, HttpStatus.ACCEPTED);
	}

}
