package org.hivedrive.cmd.session;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.hivedrive.cmd.exception.HttpResponseNotSignedProperly;
import org.hivedrive.cmd.helper.StatusCode;
import org.hivedrive.cmd.model.NodeEntity;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.model.UserKeys;
import org.hivedrive.cmd.service.SignatureService;
import org.hivedrive.cmd.service.UserKeysService;
import org.hivedrive.cmd.to.NodeTO;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class P2PSessionManager {
	
	private P2PSession session;
	
	private NodeEntity node;

	private String ip;

	private UserKeysService userKeysService;
	
	private SignatureService signatureService;
	
	private static String SIGN_HEADER_PARAM = "X-SIGN";
	
	public P2PSessionManager(String ip, UserKeysService userKeysService, 
			SignatureService signatureService) {
		this.ip = ip;
		this.userKeysService = userKeysService;
	}

	public void registerToNode() 
			throws URISyntaxException, IOException, InterruptedException {
		UserKeys keys = userKeysService.getKeys();
		NodeTO me = new NodeTO();
		me.setPublicKey(keys.getPublicAsymetricKeyAsString());
		post(nodeEndpoint(), me);
	}

	public NodeEntity getNode() {
		return node;
	}
	
	public boolean meetWithNode() {
		try {
			this.node = get(whoAreYouEndpoint(), null, NodeEntity.class);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void send(PartInfo part) {
		
	}
	

	private <T> T get(String url, String publicKeyOfNode, Class<T> clazz) 
			throws URISyntaxException, IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				  .uri(new URI(url))
				  .GET()
				  .build();
		HttpResponse<String> response = HttpClient
				  .newBuilder()
				  .build()
				  .send(request, BodyHandlers.ofString());
		
		if(response.statusCode() == StatusCode.UNAUTHORIZED) {
			registerToNode();
			return get(url, publicKeyOfNode, clazz);
		}
		verifyResponseSignature(publicKeyOfNode, response);
		
		String json = response.body();
		T object = new ObjectMapper().readValue(json, clazz);
		return object;
	}
	
	
	private void verifyResponseSignature(String publicKeyOfNode, HttpResponse<String> response) 
			throws JsonMappingException, JsonProcessingException {
		if(publicKeyOfNode == null) {
			NodeTO node = new ObjectMapper().readValue(response.body(), NodeTO.class);
			publicKeyOfNode = node.getPublicKey();
		}
		String signature = response.headers().firstValue(SIGN_HEADER_PARAM).get();
		boolean verified = signatureService.verifySign(signature, response.body(), publicKeyOfNode);
		if(!verified) {
			throw new HttpResponseNotSignedProperly();
		}
		
	}

	private void post(String url, Object object) 
			throws URISyntaxException, IOException, InterruptedException {
		String json = new ObjectMapper().writeValueAsString(object);
		HttpRequest request = HttpRequest.newBuilder()
				  .uri(new URI(url))
				  .header(SIGN_HEADER_PARAM, signOf(json))
				  .POST(BodyPublishers.ofString(json))
				  .build();
		HttpResponse<String> response = HttpClient
				  .newBuilder()
				  .build()
				  .send(request, BodyHandlers.ofString());
		if(response.statusCode() == StatusCode.UNAUTHORIZED) {
			registerToNode();
			post(url, object);
		}
	}
	
	private String signOf(String text) {
		return signatureService.signByClient(text);
	}


	private String whoAreYouEndpoint(){
		return ip + "/whoAreYou";
	}
	
	private String nodeEndpoint(){
		return ip + "/node";
	}
	
	private String partEndpoint(){
		return ip + "/part";
	}
	

}
