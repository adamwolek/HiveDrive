package org.hivedrive.cmd.session;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.hivedrive.cmd.exception.HttpResponseNotSignedProperly;
import org.hivedrive.cmd.helper.StatusCode;
import org.hivedrive.cmd.model.NodeEntity;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.model.UserKeys;
import org.hivedrive.cmd.service.SignatureService;
import org.hivedrive.cmd.service.UserKeysService;
import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.cmd.tool.JSONUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class P2PSessionManager {
	
	private P2PSession session;
	
	private NodeTO correspondingNode;

	private UserKeysService userKeysService;
	
	private SignatureService signatureService;
	
	public static String SENDER_ID_HEADER_PARAM = "x-sender-id";
	public static String SIGN_HEADER_PARAM = "x-sign";

	private URL whouAreYouEndpoint;
	private URL nodeEndpoint;
	private URL partEndpoint;
	private URL allPartEndpoint;
	private URL allNodeEndpoint;
	
	public P2PSessionManager(NodeTO correspondingNode, UserKeysService userKeysService, 
			SignatureService signatureService)  {
		this(correspondingNode.getIpAddress(), userKeysService, signatureService);
	}
	
	public P2PSessionManager(String address, UserKeysService userKeysService, 
			SignatureService signatureService)  {
		try {
			this.whouAreYouEndpoint = new URL("http://" + address + "/whoAreYou");
			this.nodeEndpoint = new URL("http://" + address + "/node");
			this.allNodeEndpoint = new URL("http://" + address + "/node/all");
			this.partEndpoint = new URL("http://" + address + "/part");
			this.allPartEndpoint = new URL("http://" + address + "/part/all");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		this.userKeysService = userKeysService;
		this.signatureService = signatureService;
	}
	
	public P2PSessionManager(UserKeysService userKeysService, 
			SignatureService signatureService)  {
		this.userKeysService = userKeysService;
		this.signatureService = signatureService;
	}

	public boolean registerToNode() 
			throws URISyntaxException, IOException, InterruptedException {
		UserKeys keys = userKeysService.getKeys();
		NodeTO me = new NodeTO();
		me.setPublicKey(keys.getPublicAsymetricKeyAsString());
		return post(nodeEndpoint, me);
	}

	public NodeTO getNode() {
		return correspondingNode;
	}
	
	public boolean meetWithNode() {
		try {
			this.correspondingNode = get(whouAreYouEndpoint, null, new TypeReference<NodeTO>() { });
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean send(PartInfo part) {
		try {
			return post(partEndpoint, part);
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	private <T> T get(URL url, String publicKeyOfNode, TypeReference<T> typeReference) 
			throws URISyntaxException, IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				  .uri(url.toURI())
				  .timeout(Duration.ofSeconds(10))
				  .header(SENDER_ID_HEADER_PARAM, userKeysService.getKeys().getPublicAsymetricKeyAsString())
				  .GET()
				  .build();
		HttpResponse<String> response = HttpClient
				  .newBuilder()
				  .build()
				  .send(request, BodyHandlers.ofString());
		
		if(response.statusCode() == StatusCode.UNAUTHORIZED) {
			registerToNode();
			return get(url, publicKeyOfNode, typeReference);
		}
		verifyResponseSignature(publicKeyOfNode, response);
		
		String json = response.body();
		T object = JSONUtils.mapper().readValue(json, typeReference);
		return object;
	}
	
	
	private void verifyResponseSignature(String publicKeyOfNode, HttpResponse<String> response) 
			throws JsonMappingException, JsonProcessingException {
		if(correspondingNode != null && StringUtils.isNotBlank(response.body())) {
			if(publicKeyOfNode == null) {
				publicKeyOfNode = correspondingNode.getPublicKey();
			}
		}
	}

	private boolean post(URL url, Object object) 
			throws URISyntaxException, IOException, InterruptedException {
		String json = JSONUtils.mapper().writeValueAsString(object);
		HttpRequest request = HttpRequest.newBuilder()
				  .uri(url.toURI())
				  .timeout(Duration.ofSeconds(10))
				  .header(SIGN_HEADER_PARAM, signOf(json))
				  .header(SENDER_ID_HEADER_PARAM, userKeysService.getKeys().getPublicAsymetricKeyAsString())
				  .header("Content-Type", "application/json")
				  .POST(BodyPublishers.ofString(json))
				  .build();
		HttpResponse<String> response = HttpClient
				  .newBuilder()
				  .build()
				  .send(request, BodyHandlers.ofString());
		if(response.statusCode() == StatusCode.UNAUTHORIZED) {
			registerToNode();
			return post(url, object);
		}
		return response.statusCode() == StatusCode.ACCEPTED;
	}
	
	private String signOf(String text) {
		return signatureService.signByClient(text);
	}

	public URL getWhouAreYouEndpoint() {
		return whouAreYouEndpoint;
	}

	public void setWhouAreYouEndpoint(URL whouAreYouEndpoint) {
		this.whouAreYouEndpoint = whouAreYouEndpoint;
	}

	public URL getNodeEndpoint() {
		return nodeEndpoint;
	}

	public void setNodeEndpoint(URL nodeEndpoint) {
		this.nodeEndpoint = nodeEndpoint;
	}

	public URL getPartEndpoint() {
		return partEndpoint;
	}

	public void setPartEndpoint(URL partEndpoint) {
		this.partEndpoint = partEndpoint;
	}

	public List<NodeTO> getAllNodes() throws URISyntaxException, IOException, InterruptedException {
		TypeReference<List<NodeTO>> typeReference = new TypeReference<List<NodeTO>>() { };
		return get(allNodeEndpoint, null, typeReference);
	}

	public List<PartTO> getAllParts() throws URISyntaxException, IOException, InterruptedException {
		TypeReference<List<PartTO>> typeReference = new TypeReference<List<PartTO>>() { };
		return get(allPartEndpoint, null, typeReference);
	}


	

}
