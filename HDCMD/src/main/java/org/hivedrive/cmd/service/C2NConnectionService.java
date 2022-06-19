package org.hivedrive.cmd.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
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
import org.hivedrive.cmd.helper.RepoOperationsHelper;
import org.hivedrive.cmd.mapper.PartInfoToTOMapper;
import org.hivedrive.cmd.model.FileMetadata;
import org.hivedrive.cmd.model.NodeEntity;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.repository.NodeRepository;
import org.hivedrive.cmd.service.common.UserKeysService;
import org.hivedrive.cmd.session.P2PSession;
import org.hivedrive.cmd.to.CentralServerMetadata;
import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.cmd.tool.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

@Lazy
@Service
public class C2NConnectionService {

	private Logger logger = LoggerFactory.getLogger(C2NConnectionService.class);
	
	@Autowired
	private ApplicationContext appContext;
	
	@Autowired
	private ConfigurationService config;
	
	@Autowired
	private UserKeysService userKeysService;
	
	@Autowired
	private NodeRepository nodeRepository;
	
	@Autowired
	private SymetricEncryptionService encryptionService;
	
	@Autowired
	private RepoOperationsHelper repoOperationsHelper;

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
			.map(address -> appContext.getBean(P2PSession.class).fromClientToAddress(address))
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
			.map(node -> appContext.getBean(P2PSession.class).fromClientToAddress(node.getAddress()))
			.filter(P2PSession::meetWithNode)
			.map(P2PSession::getAllNodes)
			.flatMap(Collection::stream)
			.map(this::mapToNewEntity)
			.forEach(nodeRepository::save);
	}

	private NodeEntity mapToNewEntity(NodeTO nodeTO) {
		NodeEntity entity = new NodeEntity();
		entity.setPublicKey(nodeTO.getPublicKey());
		entity.setAddress(nodeTO.getAddress());
		entity.setStatus(nodeTO.getStatus());
		entity.setFreeSpace(nodeTO.getFreeSpace());
		entity.setUsedSpace(nodeTO.getUsedSpace());
		return entity;
	}

	private NodeTO mapEntityToTO(NodeEntity entity) {
		NodeTO nodeTO = new NodeTO();
		nodeTO.setPublicKey(entity.getPublicKey());
		nodeTO.setAddress(entity.getAddress());
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

	public int sendPart(PartInfo part) {
		Queue<NodeEntity> nodes = getBestNodes(part);
		int copiesOfPart = 0;
		while (!nodes.isEmpty() && copiesOfPart < config.getBestNumberOfCopies()) {
			NodeEntity node = nodes.poll();
			P2PSession sessionManager = newSession(node.getAddress());
			PartTO partTO = PartInfoToTOMapper.create().map(part);
			Long partId = sessionManager.send(partTO);
			if(partId != null) {
				waitForAcceptance(partId, sessionManager);
				sessionManager.sendContent(partId, part.getPart());
				copiesOfPart++;
			}
		}
		return copiesOfPart;
	}
	
	private void waitForAcceptance(Long partRemoteId, P2PSession sessionManager) {
		int triesCount = 0;
		PartTO downloadedPart = null;
		do {
			delay();
			downloadedPart = sessionManager.downloadPart(partRemoteId);
		} while (!sessionManager.isAccepted(downloadedPart) & triesCount < 5);
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
		return appContext.getBean(P2PSession.class).fromClientToAddress(ipAddress);
	}

	private Queue<NodeEntity> getBestNodes(PartInfo part) {
		LinkedList<NodeEntity> allNodes = new LinkedList<>(nodeRepository.getAllNodes());
		Collections.shuffle(allNodes);
//		allNodes.sort(getComparatorByGeneralRate());
		return allNodes;
	}

	private Comparator<? super NodeEntity> getComparatorByGeneralRate() {
		return (n1, n2) -> {
			Long space1 = n1.getFreeSpace();
			Long space2 = n2.getFreeSpace();
			return ObjectUtils.compare(space1, space2);
		};
	}
	
	public boolean isFilePushedAlready(File file) {
		return nodeRepository.getAllNodes().stream()
		.map(this::mapEntityToTO)
		.map(NodeTO::getAddress)
		.map(address -> appContext.getBean(P2PSession.class)
					.fromClientToAddress(address))
		.filter(P2PSession::meetWithNode)
		.anyMatch(p2pSession -> p2pSession.doesFileExistGet(repoOperationsHelper.fileId(file)));
	}

	public List<PartTO> getAllPartsForRepository(String repository) {
		List<PartTO> duplicatedParts = nodeRepository.getAllNodes().stream()
		.map(this::mapEntityToTO)
		.map(node -> appContext.getBean(P2PSession.class)
		.fromClientToAddress(node.getAddress()))
		.filter(P2PSession::meetWithNode)
		.flatMap(session -> session.findPartsByRepository(repository).stream())
		.collect(Collectors.toList());
		ListMultimap<String, PartTO> groupedParts = Multimaps
				.index(duplicatedParts, part -> part.getFileId() + part.getOrderInGroup());
		return groupedParts.keySet().stream()
				.map(partGlobalId -> randomElement(groupedParts.get(partGlobalId)))
				.collect(Collectors.toList());
	}
	
	public PartInfo downloadPart(PartTO part, File directoryForParts) {
		P2PSession session = newSession(part.getNodeWhichContainsPart().getAddress());
		File contentFile = new File(directoryForParts,
				"part-" + part.getFileId() + "-" + part.getOrderInGroup());
		byte[] data = session.getContent(part);
		try {
			FileUtils.writeByteArrayToFile(contentFile, data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		PartInfo partInfo = new PartInfo();
		partInfo.setFileId(part.getFileId());
		partInfo.setPart(contentFile);
		partInfo.setEncryptedFileMetadata(part.getEncryptedFileMetadata());
		partInfo.setFileMetadata(
				FileMetadata.parseJSON(encryptionService.decrypt(partInfo.getEncryptedFileMetadata())));
		return partInfo;
	}
	
	private PartTO randomElement(List<PartTO> elements) {
		return elements.get(new Random().nextInt(elements.size()));
	}

	public boolean deletePartWithContent(PartTO part) {
		P2PSession session = newSession(part.getNodeWhichContainsPart().getAddress());
		return session.deletePart(part);
	}

}
