package org.hivedrive.cmd.session;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.hivedrive.cmd.helper.StatusCode;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.service.common.AddressService;
import org.hivedrive.cmd.service.common.KeysService;
import org.hivedrive.cmd.service.common.SignatureService;
import org.hivedrive.cmd.status.PartStatus;
import org.hivedrive.cmd.to.NodeSummary;
import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.cmd.tool.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import com.google.common.collect.Iterables;

/**
 * Session beetween my node and another node
 *
 */
@Component
@Scope("prototype")
public class P2PSessionImpl implements P2PSession {

	private static final String ERROR = "Error: ";
	
	private Logger logger = LoggerFactory.getLogger(P2PSession.class);
	private NodeTO correspondingNode;
	
	@Autowired
	private KeysService userKeysService;
	
	@Autowired
	private SignatureService signatureService;
	
	@Autowired
	private AddressService addressService;

	private DefaultUriBuilderFactory uriBuilderFactory;
	private String senderType;
	private String senderAddress;
	private String address;

	private UriBuilder whoAreYouEndpoint() {
		return uriBuilderFactory.builder().path("/whoAreYou");
	}
	private UriBuilder nodeEndpoint() {
		return uriBuilderFactory.builder().path("/node");
	}
	private UriBuilder partEndpoint() {
		return uriBuilderFactory.builder().path("/part");
	}
	private UriBuilder allPartEndpoint() {
		return uriBuilderFactory.builder().path("/part/all");
	}
	private UriBuilder doesFileExistEndpoint(String fileHash) {
		return uriBuilderFactory.builder().path("/part/" + fileHash + "/exists");
	}
	private UriBuilder allNodeEndpoint() {
		return uriBuilderFactory.builder().path("/node/all");
	}
	private UriBuilder partContentEndpoint() {
		return uriBuilderFactory.builder().path("/part/content");
	} 
	private UriBuilder nodeSummaryEndpoint() {
		return uriBuilderFactory.builder().path("/space/summary");
	} 
	
	@Override
	public P2PSession fromClientToAddress(String address) {
		this.address = address;
		if(!address.startsWith("http")) {
			address = "http://" + address;
		}
		this.uriBuilderFactory = new DefaultUriBuilderFactory(address);
		this.senderType = "client";
		return this;
	}
	
	@Override
	public P2PSession fromNodeToAddress(String address) {
		this.address = address;
		this.uriBuilderFactory = new DefaultUriBuilderFactory("http://" + address);
		this.senderType = "node";
		return this;
	}

	@PostConstruct
	private void init() {
		this.senderAddress = addressService.getMyAddress();
	}
	
	@Override
	public NodeTO getNode() {
		return correspondingNode;
	}

	@Override
	public boolean meetWithNode() {
		logger.debug("Meet with node at address " + address);
		try {
			NodeTO receivedNode = get(whoAreYouEndpoint().build(), new TypeReference<NodeTO>() {
			});
			receivedNode.setAddress(this.address);
			this.correspondingNode = receivedNode;
			return true;
		} catch (Exception e) {
			logger.error(ERROR, e);
			return false;
		}
	}
	
	@Override
	public boolean doesFileExistGet(String hashFile) {
		TypeReference<Boolean> typeReference = new TypeReference<Boolean>() {
		};
		try {
			return get(doesFileExistEndpoint(hashFile).build(), typeReference);
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean send(PartTO part) {
		try {
			return post(partEndpoint().build(), part);
		} catch (URISyntaxException | IOException | InterruptedException e) {
			logger.error(ERROR, e);
			return false;
		}
	}

	@Override
	public void sendContent(Long partId, File part) {
		try {
			
			postMultipart(partContentEndpoint()
					.path("/" + partId).build(), 
					part);
		} catch (IOException | URISyntaxException | InterruptedException e) {
			logger.error(ERROR, e);
		}
	}



	@Override
	public List<NodeTO> getAllNodes() {
		logger.debug("Get all known nodes from node at address " + address);
		try {
			TypeReference<List<NodeTO>> typeReference = new TypeReference<List<NodeTO>>() {
			};
			return get(allNodeEndpoint().build(), typeReference);
		} catch (Exception e) {
			logger.error(ERROR, e);
			return null;
		}
	}

	@Override
	public List<PartTO> getAllParts() throws URISyntaxException, IOException, InterruptedException {
		try {
			TypeReference<List<PartTO>> typeReference = new TypeReference<List<PartTO>>() {
			};
			return get(allPartEndpoint().build(), typeReference);
		} catch (Exception e) {
			logger.error(ERROR, e);
			return null;
		}
	}

	@Override
	public PartTO downloadPart(PartInfo part) {
		try {
			Collection<PartTO> parts = get(
					partEndpoint()
					.queryParam("repository", part.getFileMetadata().getRepository())
					.queryParam("groupId", part.getFileMetadata().getFileId())
					.queryParam("orderInGroup", part.getFileMetadata().getPartIndex())
					.build(), 
					new TypeReference<Collection<PartTO>>() {
			});
			return Iterables.getFirst(parts, null);
		} catch (Exception e) {
			logger.error(ERROR, e);
			return null;
		}
	}
	
	@Override
	public boolean isAccepted(PartTO part) {
		return part != null && PartStatus.ACCEPTED == part.getStatus();
	}
	
	@Override
	public List<PartTO> findPartsByRepository(String repository) {
		try {
			TypeReference<List<PartTO>> typeReference = new TypeReference<List<PartTO>>() {
			};
			return get(partEndpoint()
					.queryParam("repository", repository)
					.build(), 
					typeReference);
		} catch (URISyntaxException | IOException | InterruptedException e) {
			logger.error(ERROR, e);
			return null;
		}
	}
	
	@Override
	public byte[] getContent(PartTO part) {
		try {
			byte[] fileData = getMultipart(partContentEndpoint()
					.path("/" + part.getId()).build());
			return fileData;
		} catch (URISyntaxException | IOException | InterruptedException e) {
			logger.error(ERROR, e);
			return null;
		}
		
	}
	
	@Override
	public boolean deletePart(PartTO part) {
		try {
			return delete(partEndpoint().path("/" + part.getId()).build());
		} catch (URISyntaxException | IOException | InterruptedException e) {
			logger.error(ERROR, e);
			return false;
		}
		
	}
	
	@Override
	public NodeSummary getSummary() {
		try {
			TypeReference<NodeSummary> typeReference = new TypeReference<NodeSummary>() {
			};
			return get(nodeSummaryEndpoint().build(), typeReference);
		} catch (Exception e) {
			logger.error(ERROR, e);
			return null;
		}
	}

	private <T> T get(URI uri, TypeReference<T> typeReference)
			throws URISyntaxException, IOException, InterruptedException {
		
		logger.debug("Sending get request: " + uri);
		String publicKeyOfNode = getPublicKeyOfNode();
		HttpRequest request = HttpRequest.newBuilder().uri(uri)
				.timeout(Duration.ofSeconds(50))
				.header(SENDER_TYPE_HEADER_PARAM, this.senderType)
				.header(SENDER_ID_HEADER_PARAM, getSenderId())
				.header(SENDER_ADDRESS_HEADER_PARAM, this.senderAddress)
				.GET()
				.build();
		
		HttpResponse<String> response = HttpClient.newBuilder().build().send(request,
				BodyHandlers.ofString());
		
		verifyResponseSignature(publicKeyOfNode, response);
		
		String json = response.body();
		T object = JSONUtils.mapper().readValue(json, typeReference);
		return object;
	}
	
	private String getPublicKeyOfNode() {
		if (userKeysService.getKeys() != null) {
			return userKeysService.getKeys().getPublicAsymetricKeyAsString();
		}
		return null;
	}
	
	private String getSenderId() {
		if (userKeysService.getKeys() != null) {
			return userKeysService.getKeys().getPublicAsymetricKeyAsString();
		}
		return "";
	}
	
	private void verifyResponseSignature(String publicKeyOfNode, HttpResponse<String> response) {
		if (correspondingNode != null && StringUtils.isNotBlank(response.body()) && publicKeyOfNode == null) {
			publicKeyOfNode = correspondingNode.getPublicKey();
		}
	}
	
	private boolean post(URI uri, Object object)
			throws URISyntaxException, IOException, InterruptedException {
		logger.debug("Sending post request: " + uri);
		String json = JSONUtils.mapper().writeValueAsString(object);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(uri)
				.timeout(timeoutForRequests())
				.header(SENDER_TYPE_HEADER_PARAM, this.senderType)
				.header(SIGN_HEADER_PARAM, signOf(json))
				.header(SENDER_ID_HEADER_PARAM,
						userKeysService.getKeys().getPublicAsymetricKeyAsString())
				.header(SENDER_ADDRESS_HEADER_PARAM, this.senderAddress)
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(json)).build();
		HttpResponse<String> response = HttpClient.newBuilder().build()
				.send(request, BodyHandlers.ofString());
		return response.statusCode() == StatusCode.ACCEPTED;
	}
	
	private boolean delete(URI uri)
			throws URISyntaxException, IOException, InterruptedException {
		logger.debug("Sending delete request: " + uri);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(uri)
				.timeout(timeoutForRequests())
				.header(SENDER_TYPE_HEADER_PARAM, this.senderType)
				.header(SENDER_ID_HEADER_PARAM,
						userKeysService.getKeys().getPublicAsymetricKeyAsString())
				.header(SENDER_ADDRESS_HEADER_PARAM, this.senderAddress)
				.header("Content-Type", "application/json")
				.DELETE().build();
		HttpResponse<String> response = HttpClient.newBuilder().build()
				.send(request, BodyHandlers.ofString());
		return response.statusCode() == StatusCode.ACCEPTED;
	}
	
	private Duration timeoutForRequests() {
		return Duration.ofSeconds(500);
	}
	
	private byte[] getMultipart(URI uri) throws URISyntaxException, IOException, InterruptedException {
		logger.debug("Sending get file request: " + uri);
		String publicKeyOfNode = getPublicKeyOfNode();
		HttpRequest request = HttpRequest.newBuilder().uri(uri)
				.timeout(Duration.ofSeconds(10))
				.header(SENDER_TYPE_HEADER_PARAM, this.senderType)
				.header(SENDER_ID_HEADER_PARAM, getSenderId())
				.header(SENDER_TYPE_HEADER_PARAM, this.senderType)
				.header(SENDER_ADDRESS_HEADER_PARAM, this.senderAddress)
				.GET()
				.build();
		HttpResponse<byte[]> response = HttpClient.newBuilder().build().send(request,
				BodyHandlers.ofByteArray());
		return response.body();
	}
	
	
	private boolean postMultipart(URI uri, File file)
			throws IOException, URISyntaxException, InterruptedException {
		logger.debug("Sending post multipart request: " + uri);
		var publisher = MultipartBodyPublisher.newBuilder().filePart("part", file.toPath()).build();
		byte[] content = FileUtils.readFileToByteArray(file);
		var request = HttpRequest.newBuilder()
				.uri(uri)
				.timeout(timeoutForRequests())
				.header(SENDER_TYPE_HEADER_PARAM, this.senderType)
				.header(SIGN_HEADER_PARAM, signOf(content))
				.header(SENDER_ID_HEADER_PARAM,
						userKeysService.getKeys().getPublicAsymetricKeyAsString())
				.header(SENDER_ADDRESS_HEADER_PARAM, this.senderAddress)
				.header("Content-Type", "multipart/form-data; boundary=" + publisher.boundary())
				.POST(publisher)
				.build();
		var response = HttpClient.newBuilder().build()
				.send(request, BodyHandlers.ofString());
		return response.statusCode() == StatusCode.ACCEPTED;
	}
	
	
	
	private String signOf(byte[] content) {
		return signatureService.signUsingDefaultKeys(content);
	}
	
	private String signOf(String text) {
		return signatureService.signStringUsingDefaultKeys(text);
	}
}
