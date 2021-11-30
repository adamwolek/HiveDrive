package org.hivedrive.cmd.service;

import java.io.IOException;



import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.hivedrive.cmd.exception.ConnectToCentralMetadataServerException;
import org.hivedrive.cmd.exception.ReadDataFromMetadataServerException;
import org.hivedrive.cmd.helper.StatusCode;
import org.hivedrive.cmd.model.NodeEntity;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.session.P2PSessionManager;
import org.hivedrive.cmd.to.CentralServerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.math.PairedStats;

import one.util.streamex.StreamEx;

@Service
public class ConnectionService {
	
	private static class PartAndP2PManager{
		PartInfo part;
		P2PSessionManager manager;
		public PartAndP2PManager(PartInfo part, P2PSessionManager manager) {
			super();
			this.part = part;
			this.manager = manager;
		}
		
	}
	
	
	@Autowired
	private UserKeysService userKeysService;
	
	@Autowired
	private SignatureService signatureService;
	
	public static String URL_TO_CENTRAL_METADATA = "https://hivedrive.org/metadata.json";
	private CentralServerMetadata metadata;

	Logger logger = LoggerFactory.getLogger(ConnectionService.class);
	
	private Set<NodeEntity> knownNodes = new HashSet<>();
	
	private Executor executor = Executors.newFixedThreadPool(5);
	
	@PostConstruct
	public void init() throws URISyntaxException, IOException, InterruptedException {
		this.metadata = downloadMetadata();
		this.knownNodes = extractInitialKnownNodes(metadata);
		meetMoreNodes();
	}

	private Set<NodeEntity> extractInitialKnownNodes(CentralServerMetadata metadata) {
		return metadata.getActiveNodes().stream()
		.map(ip -> new P2PSessionManager(ip, userKeysService, signatureService))
		.filter(sessionManager -> sessionManager.meetWithNode())
		.map(sessionManager -> sessionManager.getNode())
		.collect(Collectors.toSet());
	}


	private void meetMoreNodes() {
		Set<NodeEntity> newNodes = metadata.getActiveNodes().stream()
		.map(ip -> new P2PSessionManager(ip, userKeysService, signatureService))
		.filter(sessionManager -> sessionManager.meetWithNode())
		.map(sessionManager -> sessionManager.getNode())
		.collect(Collectors.toSet());
		this.knownNodes.addAll(newNodes);
	}


	

	
	private CentralServerMetadata downloadMetadata() {
		try {
			String json = IOUtils.toString(new URL(URL_TO_CENTRAL_METADATA), "UTF-8");
			CentralServerMetadata metadata = new ObjectMapper().readValue(json, CentralServerMetadata.class);
			return metadata;
		} catch (JsonProcessingException e) {
			throw new ReadDataFromMetadataServerException(e);
		} catch (IOException e) {
			throw new ConnectToCentralMetadataServerException(e);
		}
	}

	public void sendParts(List<PartInfo> parts) {
		Queue<NodeEntity> nodes = getNodesSortedByBestGeneralRate(this.knownNodes);
		for (NodeEntity node : nodes) {
			for (PartInfo part : parts) {
				P2PSessionManager sessionManager = new P2PSessionManager(
						node.getIpAddress(), userKeysService, signatureService);
				sessionManager.send(part);
			}
		}
	}

	private Queue<NodeEntity> getNodesSortedByBestGeneralRate(Set<NodeEntity> knownNodes) {
		return knownNodes.stream()
		.sorted((n1, n2) -> {
			
			return 1;
		}).collect(Collectors.toCollection(LinkedList::new));
	}



	
	
	
}
