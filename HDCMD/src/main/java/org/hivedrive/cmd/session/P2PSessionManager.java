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
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class P2PSessionManager {
	
	private P2PSession session;
	
	private NodeEntity node;

	private UserKeysService userKeysService;
	
	private SignatureService signatureService;
	
	public static String SIGN_HEADER_PARAM = "X-SIGN";

	private URL whouAreYouEndpoint;
	private URL nodeEndpoint;
	private URL partEndpoint;
	
	public P2PSessionManager(String address, UserKeysService userKeysService, 
			SignatureService signatureService)  {
		try {
			this.whouAreYouEndpoint = new URL("http://" + address + "/whoAreYou");
			this.nodeEndpoint = new URL("http://" + address + "/node");
			this.partEndpoint = new URL("http://" + address + "/part");
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

	public NodeEntity getNode() {
		return node;
	}
	
	public boolean meetWithNode() {
		try {
			this.node = get(whouAreYouEndpoint, null, NodeEntity.class);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean send(PartInfo part) {
		
		return true;
	}
	

	private <T> T get(URL url, String publicKeyOfNode, Class<T> clazz) 
			throws URISyntaxException, IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				  .uri(url.toURI())
				  .timeout(Duration.ofSeconds(10))
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
		if(StringUtils.isNotBlank(response.body())) {
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
		
	}

	private boolean post(URL url, Object object) 
			throws URISyntaxException, IOException, InterruptedException {
		String json = new ObjectMapper().writeValueAsString(object);
		HttpRequest request = HttpRequest.newBuilder()
				  .uri(url.toURI())
				  .timeout(Duration.ofSeconds(10))
				  .header(SIGN_HEADER_PARAM, signOf(json))
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


	

}
