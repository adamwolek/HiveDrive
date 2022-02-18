package org.hivedrive.cmd.session;

import java.io.File;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.hivedrive.cmd.helper.StatusCode;
import org.hivedrive.cmd.mapper.PartInfoToTOMapper;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.model.UserKeys;
import org.hivedrive.cmd.service.ConnectionService;
import org.hivedrive.cmd.service.SignatureService;
import org.hivedrive.cmd.service.UserKeysService;
import org.hivedrive.cmd.status.PartStatus;
import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.cmd.tool.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import com.github.mizosoft.methanol.MultipartBodyPublisher.Builder;
import com.google.common.collect.Iterables;

public class P2PSessionManager {

	private Logger logger = LoggerFactory.getLogger(P2PSessionManager.class);
	
	private P2PSession session;

	private NodeTO correspondingNode;

	private UserKeysService userKeysService;

	private SignatureService signatureService;

	public static String SENDER_ID_HEADER_PARAM = "x-sender-id";
	public static String SIGN_HEADER_PARAM = "x-sign";

	private DefaultUriBuilderFactory uriBuilderFactory;

	private UriBuilder whouAreYouEndpoint() {
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
	private UriBuilder allNodeEndpoint() {
		return uriBuilderFactory.builder().path("/node/all");
	}
	private UriBuilder partContentEndpoint() {
		return uriBuilderFactory.builder().path("/part/content");
	}

	public P2PSessionManager(NodeTO correspondingNode, UserKeysService userKeysService,
			SignatureService signatureService) {
		this(correspondingNode.getIpAddress(), userKeysService, signatureService);
	}

	public P2PSessionManager(String address, UserKeysService userKeysService,
			SignatureService signatureService) {
		this.uriBuilderFactory = new DefaultUriBuilderFactory("http://" + address);
		this.userKeysService = userKeysService;
		this.signatureService = signatureService;
	}

	public P2PSessionManager(UserKeysService userKeysService, SignatureService signatureService) {
		this.userKeysService = userKeysService;
		this.signatureService = signatureService;
	}

	public boolean registerToNode() throws URISyntaxException, IOException, InterruptedException {
		UserKeys keys = userKeysService.getKeys();
		NodeTO me = new NodeTO();
		me.setPublicKey(keys.getPublicAsymetricKeyAsString());
		return post(nodeEndpoint().build(), me);
	}

	public NodeTO getNode() {
		return correspondingNode;
	}

	public boolean meetWithNode() {
		try {
			this.correspondingNode = get(whouAreYouEndpoint().build(), new TypeReference<NodeTO>() {
			});
			return true;
		} catch (Exception e) {
			logger.error("Error: ", e);
			return false;
		}
	}

	public boolean send(PartTO part) {
		try {
			return post(partEndpoint().build(), part);
		} catch (URISyntaxException | IOException | InterruptedException e) {
			logger.error("Error: ", e);
			return false;
		}
	}

	public void sendContent(PartInfo part) {
		try {
			postMultipart(partContentEndpoint().build(), part.getPart());
		} catch (IOException | URISyntaxException | InterruptedException e) {
			logger.error("Error: ", e);
		}
	}

	private <T> T get(URI uri, TypeReference<T> typeReference)
			throws URISyntaxException, IOException, InterruptedException {
		String publicKeyOfNode = getPublicKeyOfNode();
		HttpRequest request = HttpRequest.newBuilder().uri(uri)
				.timeout(Duration.ofSeconds(10))
				.header(SENDER_ID_HEADER_PARAM, getSenderId())
				.GET()
				.build();
		HttpResponse<String> response = HttpClient.newBuilder().build().send(request,
				BodyHandlers.ofString());

		if (response.statusCode() == StatusCode.UNAUTHORIZED) {
			registerToNode();
			return get(uri, typeReference);
		}
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

	private void verifyResponseSignature(String publicKeyOfNode, HttpResponse<String> response)
			throws JsonMappingException, JsonProcessingException {
		if (correspondingNode != null && StringUtils.isNotBlank(response.body())) {
			if (publicKeyOfNode == null) {
				publicKeyOfNode = correspondingNode.getPublicKey();
			}
		}
	}

	private boolean post(URI uri, Object object)
			throws URISyntaxException, IOException, InterruptedException {
		String json = JSONUtils.mapper().writeValueAsString(object);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(uri)
				.timeout(timeoutForRequests())
				.header(SIGN_HEADER_PARAM, signOf(json))
				.header(SENDER_ID_HEADER_PARAM,
						userKeysService.getKeys().getPublicAsymetricKeyAsString())
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(json)).build();
		HttpResponse<String> response = HttpClient.newBuilder().build()
				.send(request, BodyHandlers.ofString());
		if (response.statusCode() == StatusCode.UNAUTHORIZED) {
			registerToNode();
			return post(uri, object);
		}
		return response.statusCode() == StatusCode.ACCEPTED;
	}

	private Duration timeoutForRequests() {
		return Duration.ofSeconds(500);
	}
	
	private byte[] getMultipart(URI uri) throws URISyntaxException, IOException, InterruptedException {
		String publicKeyOfNode = getPublicKeyOfNode();
		HttpRequest request = HttpRequest.newBuilder().uri(uri)
				.timeout(Duration.ofSeconds(10))
				.header(SENDER_ID_HEADER_PARAM, getSenderId())
				.GET()
				.build();
		HttpResponse<byte[]> response = HttpClient.newBuilder().build().send(request,
				BodyHandlers.ofByteArray());
		if (response.statusCode() == StatusCode.UNAUTHORIZED) {
			registerToNode();
			return getMultipart(uri);
		}
		return response.body();
	}
	
	
	private boolean postMultipart(URI uri, File file)
			throws IOException, URISyntaxException, InterruptedException {
		var publisher = MultipartBodyPublisher.newBuilder().filePart("part", file.toPath()).build();
		byte[] content = FileUtils.readFileToByteArray(file);
		var request = HttpRequest.newBuilder()
				.uri(uri)
				.timeout(timeoutForRequests())
				.header(SIGN_HEADER_PARAM, signOf(content))
				.header(SENDER_ID_HEADER_PARAM,
						userKeysService.getKeys().getPublicAsymetricKeyAsString())
				.header("Content-Type", "multipart/form-data; boundary=" + publisher.boundary())
				.POST(publisher)
				.build();
		var response = HttpClient.newBuilder().build()
				.send(request, BodyHandlers.ofString());
		return response.statusCode() == StatusCode.ACCEPTED;
	}


	
	private String signOf(byte[] content) {
		return signatureService.signByClient(content);
	}

	private String signOf(String text) {
		return signatureService.signByClient(text);
	}


	public List<NodeTO> getAllNodes() throws URISyntaxException, IOException, InterruptedException {
		TypeReference<List<NodeTO>> typeReference = new TypeReference<List<NodeTO>>() {
		};
		return get(allNodeEndpoint().build(), typeReference);
	}

	public List<PartTO> getAllParts() throws URISyntaxException, IOException, InterruptedException {
		TypeReference<List<PartTO>> typeReference = new TypeReference<List<PartTO>>() {
		};
		return get(allPartEndpoint().build(), typeReference);
	}

	public boolean partAccepted(PartInfo part) {
		try {
			Collection<PartTO> parts = get(
					partEndpoint()
					.queryParam("repository", part.getFileMetadata().getRepository())
					.queryParam("groupId", part.getFileMetadata().getFileId())
					.queryParam("orderInGroup", part.getFileMetadata().getPartIndex())
					.build(), 
					new TypeReference<Collection<PartTO>>() {
			});
			PartTO partOnNode = Iterables.getFirst(parts, null);
			return PartStatus.ACCEPTED == partOnNode.getStatus();
		} catch (URISyntaxException | IOException | InterruptedException e) {
			logger.error("Error: ", e);
		}
		return false;
	}
	
	
	public void setUriBuilderFactory(DefaultUriBuilderFactory uriBuilderFactory) {
		this.uriBuilderFactory = uriBuilderFactory;
	}
	
	public List<PartTO> findPartsByRepository(String repository) {
		try {
			TypeReference<List<PartTO>> typeReference = new TypeReference<List<PartTO>>() {
			};
			return get(partEndpoint()
					.queryParam("repository", repository)
					.build(), 
					typeReference);
		} catch (URISyntaxException | IOException | InterruptedException e) {
			logger.error("Error: ", e);
			return null;
		}
	}
	public byte[] getContent(PartTO part) {
		try {
			byte[] fileData = getMultipart(partContentEndpoint()
					.path("/" + part.getId()).build());
			return fileData;
		} catch (URISyntaxException | IOException | InterruptedException e) {
			logger.error("Error: ", e);
			return null;
		}
		
	}

	
}
