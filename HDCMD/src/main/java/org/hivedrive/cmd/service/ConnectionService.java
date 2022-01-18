package org.hivedrive.cmd.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.hivedrive.cmd.config.ConfigurationService;
import org.hivedrive.cmd.exception.ConnectToCentralMetadataServerException;
import org.hivedrive.cmd.exception.ReadDataFromMetadataServerException;
import org.hivedrive.cmd.model.NodeEntity;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.session.P2PSessionManager;
import org.hivedrive.cmd.to.CentralServerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Lazy
@Service
public class ConnectionService {

	private static class PartAndP2PManager {
		PartInfo part;
		P2PSessionManager manager;

		public PartAndP2PManager(PartInfo part, P2PSessionManager manager) {
			super();
			this.part = part;
			this.manager = manager;
		}
	}

	@Autowired
	private ConfigurationService config;

	@Autowired
	private UserKeysService userKeysService;

	@Autowired
	private SignatureService signatureService;

	@Autowired
	Environment env;

	private CentralServerMetadata metadata;

	Logger logger = LoggerFactory.getLogger(ConnectionService.class);

	private Set<NodeEntity> knownNodes = new HashSet<>();

	private Executor executor = Executors.newFixedThreadPool(5);

	@PostConstruct
	public void init() throws URISyntaxException, IOException, InterruptedException {
		if (!Arrays.asList(env.getActiveProfiles()).contains("unitTests")) {
			this.manualInit();
		}
	}

	public void manualInit() throws URISyntaxException, IOException, InterruptedException {
		this.metadata = downloadMetadata();
		this.knownNodes = extractInitialKnownNodes(metadata);
		meetMoreNodes();
	}

	private Set<NodeEntity> extractInitialKnownNodes(CentralServerMetadata metadata) {
		return metadata.getActiveNodes().stream()
				.map(address -> new P2PSessionManager(address, userKeysService, signatureService))
				.filter(sessionManager -> sessionManager.meetWithNode())
				.map(sessionManager -> sessionManager.getNode()).collect(Collectors.toSet());
	}

	private void meetMoreNodes() {
		Set<NodeEntity> newNodes = metadata.getActiveNodes().stream()
				.map(address -> new P2PSessionManager(address, userKeysService, signatureService))
				.filter(sessionManager -> sessionManager.meetWithNode())
				.map(sessionManager -> sessionManager.getNode()).collect(Collectors.toSet());
		this.knownNodes.addAll(newNodes);
	}

	private CentralServerMetadata downloadMetadata() throws URISyntaxException {
		try {
			String json = IOUtils.toString(config.getUrlToCentralMetadata(), "UTF-8");
			CentralServerMetadata metadata = new ObjectMapper().readValue(json,
					CentralServerMetadata.class);
			return metadata;
		} catch (JsonProcessingException e) {
			throw new ReadDataFromMetadataServerException(e);
		} catch (IOException e) {
			throw new ConnectToCentralMetadataServerException(e);
		}
	}

	public void sendParts(List<PartInfo> parts) {
		for (PartInfo part : parts) {
			Queue<NodeEntity> nodes = getBestNodes(part);
			int copiesOfPart = 0;
			while (copiesOfPart >= config.getBestNumberOfCopies()) {
				NodeEntity node = nodes.poll();
				P2PSessionManager sessionManager = newSession(node);
				boolean success = sessionManager.send(part);
				if (success) {
					copiesOfPart++;
				}
			}
		}
	}

	private P2PSessionManager newSession(NodeEntity node) {
		return new P2PSessionManager(node.getIpAddress(), userKeysService, signatureService);
	}

	private Queue<NodeEntity> getBestNodes(PartInfo part) {
		LinkedList<NodeEntity> nodes = new LinkedList<>();
		nodes.addAll(getNodesWhoAlreadyHaveThisPart(part));
		nodes.addAll(getNodesSortedByBestGeneralRate(knownNodes));
		return null;
	}

	private LinkedList<NodeEntity> getNodesWhoAlreadyHaveThisPart(PartInfo part) {
//		part.getOwnerPublicKey()
		return null;
	}

	private Queue<NodeEntity> getNodesSortedByBestGeneralRate(Set<NodeEntity> knownNodes) {
		return knownNodes.stream().sorted((n1, n2) -> {
			return n1.getFreeSpace().compareTo(n2.getFreeSpace());
		}).collect(Collectors.toCollection(LinkedList::new));
	}

}
