package org.hivedrive.cmd.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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
import org.hivedrive.cmd.service.common.SignatureService;
import org.hivedrive.cmd.service.common.UserKeysService;
import org.hivedrive.cmd.session.P2PSession;
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
import org.springframework.util.DigestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

@Lazy
@Service
public class C2NConnectionService {

	private Logger logger = LoggerFactory.getLogger(C2NConnectionService.class);
	
	private ConfigurationService config;
	private UserKeysService userKeysService;
	private SignatureService signatureService;
	private Environment env; // TODO: delete?
	private NodeRepository nodeRepository;

	private RepositoryConfigService repositoryConfigService;

	private SymetricEncryptionService encryptionService;

	@Autowired
	public C2NConnectionService(ConfigurationService config, UserKeysService userKeysService,
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
		userKeysService.onKeysLoaded(() -> {
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
			.map(address -> P2PSession.fromClient(address, userKeysService, signatureService))
			.filter(P2PSession::meetWithNode)
			.map(P2PSession::getNode)
			.map(this::mapToNewEntity)
			.forEach(nodeRepository::save);
	}

	public NodeTO getMyServerIP(String myPublicKey) {
		List<NodeEntity> myNode = searchForMyNode(myPublicKey);
		while (myNode.isEmpty()) {
			meetMoreNodes();
			myNode = searchForMyNode(myPublicKey);
		}
		return mapEntityToTO(myNode.get(0));
	}

	private List<NodeEntity> searchForMyNode(String myPublicKey) {
		return nodeRepository.getAllNodes().stream().filter(node -> node.getPublicKey().equals(myPublicKey))
				.collect(Collectors.toList());
	}

	private void meetMoreNodes() {
		nodeRepository.getAllNodes().stream().map(this::mapEntityToTO)
			.map(node -> P2PSession.fromClient(node, userKeysService, signatureService))
			.filter(P2PSession::meetWithNode)
			.map(P2PSession::getAllNodes)
			.flatMap(Collection::stream)
			.map(this::mapToNewEntity)
			.forEach(nodeRepository::save);
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
		try {
			String json = IOUtils.toString(config.getUrlToCentralMetadata(), "UTF-8");
			CentralServerMetadata metadata = JSONUtils.mapper().readValue(json, CentralServerMetadata.class);
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
				P2PSession sessionManager = newSession(node.getIpAddress());
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

	private Long waitForAcceptance(PartInfo part, P2PSession sessionManager) {
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

	public List<NodeEntity> getAllKnownNodes(String ipAddress)
			throws URISyntaxException, IOException, InterruptedException {
		P2PSession session = newSession(ipAddress);
		return session.getAllNodes().stream()
				.map(this::mapToNewEntity)
				.map(nodeRepository::save)
				.collect(Collectors.toList());
	}

	public List<PartTO> getAllPartsStoredOnNode(String ipAddress)
			throws URISyntaxException, IOException, InterruptedException {
		P2PSession session = newSession(ipAddress);
		return session.getAllParts();
	}

	private P2PSession newSession(String ipAddress) {
		return P2PSession.fromClient(ipAddress, userKeysService, signatureService);
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
	
	public boolean isFilePushedAlready(File file) {
		
		List<NodeEntity> allNodes = nodeRepository.getAllNodes();
		for (NodeEntity node : allNodes) {
			NodeTO nodeTO = mapEntityToTO(node);
			P2PSession p2pSession = P2PSession.fromClient(nodeTO, userKeysService, signatureService);
			if (p2pSession.meetWithNode()) {
				String fileHash = null;
				try {
					fileHash = DigestUtils.md5DigestAsHex(FileUtils.readFileToByteArray(file));
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (fileHash != null && p2pSession.doesFileExistGet(fileHash)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<PartInfo> downloadParts(File workDirectory) {
		List<PartTO> parts = nodeRepository.getAllNodes().stream().map(this::mapEntityToTO)
				.map(node -> P2PSession.fromClient(node, userKeysService, signatureService))
				.filter(P2PSession::meetWithNode)
				.flatMap(session -> {
					String repository = repositoryConfigService.getConfig().getRepositoryName();
					return session.findPartsByRepository(repository).stream();
				}).collect(Collectors.toList());
		ListMultimap<String, PartTO> groupedParts = Multimaps.index(parts, PartTO::getGlobalId);
		return groupedParts.keys().stream().map(partGlobalId -> randomElement(groupedParts.get(partGlobalId)))
				.map(selectedPart -> {
					return partInfo(workDirectory, selectedPart);
				}).collect(Collectors.toList());
	}

	private PartInfo partInfo(File workDirectory, PartTO selectedPart) {
		try {
			P2PSession session = newSession(selectedPart.getNodeWhichContainsPart().getLocalIpAddress());
			File directoryForParts = new File(workDirectory, "/parts");
			File part = new File(directoryForParts,
					"part-" + selectedPart.getGroupId() + "-" + selectedPart.getOrderInGroup());
			byte[] data = session.getContent(selectedPart);
			FileUtils.writeByteArrayToFile(part, data);
			PartInfo partInfo = new PartInfo();
			partInfo.setPart(part);
			partInfo.setEncryptedFileMetadata(selectedPart.getEncryptedFileMetadata());
			partInfo.setFileMetadata(
					FileMetadata.parseJSON(encryptionService.decrypt(partInfo.getEncryptedFileMetadata())));
			return partInfo;
		} catch (Exception e) {
			logger.error("Error: ", e);
			return null;
		}
	}

	private PartTO randomElement(List<PartTO> elements) {
		return elements.get(new Random().nextInt(elements.size()));
	}

}
