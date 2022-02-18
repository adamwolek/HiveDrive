package org.hivedrive.cmd.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.hivedrive.cmd.config.ConfigurationService;
import org.hivedrive.cmd.exception.ConnectToCentralMetadataServerException;
import org.hivedrive.cmd.exception.ReadDataFromMetadataServerException;
import org.hivedrive.cmd.mapper.PartInfoToTOMapper;
import org.hivedrive.cmd.model.FileMetadata;
import org.hivedrive.cmd.model.NodeEntity;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.repository.NodeRepository;
import org.hivedrive.cmd.session.P2PSessionManager;
import org.hivedrive.cmd.to.CentralServerMetadata;
import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.cmd.tool.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

@Lazy
@Service
public class ConnectionService {

	private Logger logger = LoggerFactory.getLogger(ConnectionService.class);
	
	private ConfigurationService config;
	private UserKeysService userKeysService;
	private SignatureService signatureService;
	private Environment env;
	private NodeRepository nodeRepository;

	private RepositoryConfigService repositoryConfigService;

	private SymetricEncryptionService encryptionService;
	
	@Autowired
	public ConnectionService(ConfigurationService config, UserKeysService userKeysService,
			SignatureService signatureService, Environment env, NodeRepository nodeRepository, 
			RepositoryConfigService repositoryConfigService, SymetricEncryptionService encryptionService) {
		super();
		this.config = config;
		this.userKeysService = userKeysService;
		this.signatureService = signatureService;
		this.env = env;
		this.nodeRepository = nodeRepository;
		this.repositoryConfigService = repositoryConfigService;
		this.encryptionService = encryptionService;
	}



	@PostConstruct
	public void init() throws URISyntaxException, IOException, InterruptedException {
		userKeysService.addPropertyChangeListener(event -> {
			try {
				this.manualInit();
			} catch (URISyntaxException | IOException | InterruptedException e) {
				e.printStackTrace();
			}
		});
	}

	public void manualInit() throws URISyntaxException, IOException, InterruptedException {
		CentralServerMetadata metadata = downloadMetadata();
		saveInitialKnownNodes(metadata);
		meetMoreNodes();
	}

	private void saveInitialKnownNodes(CentralServerMetadata metadata) {
		metadata.getActiveNodes().stream()
			.map(address -> new P2PSessionManager(address, userKeysService, signatureService))
			.filter(P2PSessionManager::meetWithNode).map(P2PSessionManager::getNode)
			.map(this::mapToNewEntity).forEach(nodeRepository::save);
	}

	private void meetMoreNodes() {
		nodeRepository.getAllNodes().stream().map(this::mapEntityToTO)
			.map(node -> new P2PSessionManager(node, userKeysService, signatureService))
			.filter(P2PSessionManager::meetWithNode).map(P2PSessionManager::getNode)
			.map(this::mapToNewEntity).forEach(nodeRepository::save);
	}

	private NodeEntity mapToNewEntity(NodeTO nodeTO) {
		NodeEntity entity = new NodeEntity();
		entity.setPublicKey(nodeTO.getPublicKey());
		entity.setIpAddress(nodeTO.getAccessibleIP());
		entity.setLocalIpAddress(nodeTO.getLocalIpAddress());
		entity.setStatus(nodeTO.getStatus());
		entity.setFreeSpace(nodeTO.getFreeSpace());
		entity.setUsedSpace(nodeTO.getUsedSpace());
		return entity;
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

	private CentralServerMetadata downloadMetadata() throws URISyntaxException {
		if (true) {
			CentralServerMetadata metadata = new CentralServerMetadata();
			metadata.setActiveNodes(Arrays.asList("localhost:8080"));
			return metadata;
		}

		try {
			String json = IOUtils.toString(config.getUrlToCentralMetadata(), "UTF-8");
			CentralServerMetadata metadata = JSONUtils.mapper().readValue(json,
					CentralServerMetadata.class);
			return metadata;
		} catch (JsonProcessingException e) {
			throw new ReadDataFromMetadataServerException(e);
		} catch (IOException e) {
			throw new ConnectToCentralMetadataServerException(e);
		}
	}

	public void sendParts(List<PartInfo> parts) {
		int partIndex = 1;
		for (PartInfo part : parts) {
			Queue<NodeEntity> nodes = getBestNodes(part);
			int copiesOfPart = 0;
			while (!nodes.isEmpty() && copiesOfPart < config.getBestNumberOfCopies()) {
				NodeEntity node = nodes.poll();
				P2PSessionManager sessionManager = newSession(node.getIpAddress());
				PartTO partTO = PartInfoToTOMapper.create().map(part);
				boolean requestSent = sessionManager.send(partTO);
				if (requestSent) {
					Long partId = waitForAcceptance(part, sessionManager);
					sessionManager.sendContent(partId, part.getPart());
					copiesOfPart++;
				}
			}
			logger.info("Part " + partIndex + " sent");
			partIndex++;
		}
	}

	private Long waitForAcceptance(PartInfo part, P2PSessionManager sessionManager) {
		int triesCount = 0;
		PartTO downloadedPart = null;
		do {
			delay();
			downloadedPart = sessionManager.downloadPart(part);
		} while (!sessionManager.isAccepted(downloadedPart) & triesCount < 5);
		return downloadedPart.getId();
	}

	private void delay() {
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public List<NodeEntity> getAllKnonwNodes(String ipAddress)
			throws URISyntaxException, IOException, InterruptedException {
		P2PSessionManager session = newSession(ipAddress);
		return session.getAllNodes().stream().map(this::mapToNewEntity).map(nodeRepository::save)
				.collect(Collectors.toList());
	}


	public List<PartTO> getAllPartsStoredOnNode(String ipAddress)
			throws URISyntaxException, IOException, InterruptedException {
		P2PSessionManager session = newSession(ipAddress);
		return session.getAllParts();
	}

	private P2PSessionManager newSession(String ipAddress) {
		return new P2PSessionManager(ipAddress, userKeysService, signatureService);
	}

	private Queue<NodeEntity> getBestNodes(PartInfo part) {
		LinkedList<NodeEntity> allNodes = new LinkedList<>(nodeRepository.getAllNodes());
		allNodes.sort(getComparatorByGeneralRate());
		return allNodes;
	}

	private Comparator<? super NodeEntity> getComparatorByGeneralRate() {
		return (n1, n2) -> {
			Long space1 = n1.getFreeSpace();
			Long space2 = n1.getFreeSpace();
			return ObjectUtils.compare(space1, space2);
		};
	}

	public List<PartInfo> downloadParts(File workDirectory) {
		List<PartTO> parts = nodeRepository.getAllNodes().stream()
		.map(this::mapEntityToTO)
		.map(node -> new P2PSessionManager(node, userKeysService, signatureService))
		.filter(P2PSessionManager::meetWithNode)
		.flatMap(session -> {
			String repository = repositoryConfigService.getConfig().getRepositoryName();
			return session.findPartsByRepository(repository).stream();
		})
		.collect(Collectors.toList());
		ListMultimap<String, PartTO> groupedParts = Multimaps.index(parts, PartTO::getGlobalId);
		return groupedParts.keys().stream()
		.map(partGlobalId -> randomElement(groupedParts.get(partGlobalId)))
		.map(selectedPart -> {
			try {
				P2PSessionManager session = newSession(
						selectedPart.getNodeWhichContainsPart().getLocalIpAddress());
				File directoryForParts = new File(workDirectory, "/parts");
				File part = new File(directoryForParts, "part-" + selectedPart.getGroupId() 
				+ "-" + selectedPart.getOrderInGroup());
				byte[] data = session.getContent(selectedPart);
				FileUtils.writeByteArrayToFile(part, data);
				PartInfo partInfo = new PartInfo();
				partInfo.setPart(part);
				partInfo.setEncryptedFileMetadata(selectedPart.getEncryptedFileMetadata());
				partInfo.setFileMetadata(FileMetadata.parseJSON(
						encryptionService.decrypt(partInfo.getEncryptedFileMetadata())));
				return partInfo;
			} catch (Exception e) {
				logger.error("Error: ", e);
				return null;
			}
		}).collect(Collectors.toList());
	}


	private PartTO randomElement(List<PartTO> elements) {
		return elements.get(new Random().nextInt(elements.size()));
	}


}
