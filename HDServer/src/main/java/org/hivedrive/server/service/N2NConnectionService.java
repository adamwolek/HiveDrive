package org.hivedrive.server.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hivedrive.cmd.exception.ConnectToCentralMetadataServerException;
import org.hivedrive.cmd.exception.ReadDataFromMetadataServerException;
import org.hivedrive.cmd.service.C2NConnectionService;
import org.hivedrive.cmd.service.common.SignatureService;
import org.hivedrive.cmd.service.common.UserKeysService;
import org.hivedrive.cmd.session.P2PSession;
import org.hivedrive.cmd.to.CentralServerMetadata;
import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.cmd.tool.JSONUtils;
import org.hivedrive.server.config.LocalConfiguration;
import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.repository.NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Iterables;

@Service
public class N2NConnectionService {
	
	private Logger logger = LoggerFactory.getLogger(N2NConnectionService.class);
	
	@Autowired
	private LocalConfiguration localConfiguration;
	
	@Autowired
	private UserKeysService userKeysService;
	
	@Autowired
	private SignatureService signatureService;
	
	@Autowired
	private NodeRepository nodeRepository;
	
	public void manualInit() throws URISyntaxException, IOException, InterruptedException {
		List<CentralServerMetadata> centralServersMetadata = localConfiguration.getCentralServers().stream()
		.map(this::createUrl)
		.map(this::downloadMetadata)
		.collect(Collectors.toList());
		saveInitialKnownNodes(centralServersMetadata);
		meetMoreNodes();
	}
	
	private CentralServerMetadata downloadMetadata(URL centralServerAddress) {
		try {
			String json = IOUtils.toString(centralServerAddress, "UTF-8");
			CentralServerMetadata metadata = JSONUtils.mapper().readValue(json,
					CentralServerMetadata.class);
			return metadata;
		} catch (JsonProcessingException e) {
			throw new ReadDataFromMetadataServerException(e);
		} catch (IOException e) {
			throw new ConnectToCentralMetadataServerException(e);
		}
	}
	
	private void saveInitialKnownNodes(List<CentralServerMetadata> centralServersMetadata) {
		centralServersMetadata.stream()
		.map(CentralServerMetadata::getActiveNodes)
		.flatMap(Collection::stream)
		.distinct()
		.map(address -> {
			logger.info("Register to node at address " + address);
			return address;
		})
		.map(address -> new P2PSession(address, userKeysService, signatureService))
		.filter(P2PSession::meetWithNode)
		.map(P2PSession::getNode)
		.map(this::mapToNewEntity)
		.forEach(nodeRepository::save);
	}
	
	private void meetMoreNodes() {
		nodeRepository.findAll().stream().map(this::mapEntityToTO)
			.map(node -> new P2PSession(node, userKeysService, signatureService))
			.filter(P2PSession::meetWithNode)
			.map(P2PSession::getAllNodes)
			.flatMap(Collection::stream)
			.map(this::mapToNewEntity)
			.forEach(nodeRepository::save);
	}
	
	private NodeTO mapEntityToTO(NodeEntity entity) {
		NodeTO nodeTO = new NodeTO();
		nodeTO.setPublicKey(entity.getPublicKey());
		nodeTO.setIpAddress(entity.getIpAddress());
		nodeTO.setLocalIpAddress(entity.getLocalIpAddress());
		nodeTO.setStatus(entity.getStatus());
		nodeTO.setFreeSpace(entity.getFreeSpace());
		nodeTO.setUsedSpace(entity.getUsedSpace());
		return nodeTO;
	}
	
	private NodeEntity mapToNewEntity(NodeTO nodeTO) {
		NodeEntity entity = new NodeEntity();
		entity.setPublicKey(nodeTO.getPublicKey());
		entity.setIpAddress(nodeTO.getAccessibleIP());
		entity.setStatus(nodeTO.getStatus());
		entity.setFreeSpace(nodeTO.getFreeSpace());
		entity.setUsedSpace(nodeTO.getUsedSpace());
		return entity;
	}

	private URL createUrl(String address) {
		try {
			return new URL(address);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	

}
