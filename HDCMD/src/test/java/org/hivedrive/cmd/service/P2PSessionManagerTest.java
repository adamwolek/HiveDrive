package org.hivedrive.cmd.service;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.assertj.core.util.Arrays;
import org.hivedrive.cmd.config.TestConfig;
import org.hivedrive.cmd.model.UserKeys;
import org.hivedrive.cmd.session.P2PSessionManager;
import org.hivedrive.cmd.to.CentralServerMetadata;
import org.hivedrive.cmd.to.NodeTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class P2PSessionManagerTest {
	
	@Autowired
	UserKeysService userKeysService;
	
	@Autowired
	SignatureService signatureService;
	
	@Autowired
	ConnectionService connectionService;
	
	@Test
	void testCentralNode() throws URISyntaxException, IOException, InterruptedException {
		MockWebServer centralServer = new MockWebServer();
		MockWebServer node1 = new MockWebServer();
		MockWebServer node2 = new MockWebServer();
		ConnectionService.urlToCentralMetadata = centralServer.url("metadata").uri();
		CentralServerMetadata metadata = new CentralServerMetadata();
		metadata.setActiveNodes(Lists.newArrayList(node1.getHostName(), node2.getHostName()));
		MockResponse centralResponse = new MockResponse()
		        .setBody(new ObjectMapper().writeValueAsString(metadata));
		centralServer.enqueue(centralResponse);
		
		connectionService.init();
		
		RecordedRequest node1Request = node1.takeRequest();
		RecordedRequest node2Request = node2.takeRequest();
		
		String body1 = node1Request.getBody().toString();
		String body2 = node2Request.getBody().toString();
		
		System.out.println("");
		
	}
	
	@Test
	void whoAreYouTest() throws JsonProcessingException {
		MockWebServer mockServer = new MockWebServer();
		URI uri = mockServer.url("/whoAreYou").uri();
		
		NodeTO nodeTo = new NodeTO();
		nodeTo.setPublicKey(userKeysService.getKeys().getPublicAsymetricKeyAsString());
		nodeTo.setIpAddress(uri.getHost());
		
		String body = new ObjectMapper().writeValueAsString(nodeTo);
		MockResponse mockedResponse = new MockResponse()
				.addHeader(P2PSessionManager.SIGN_HEADER_PARAM, 
						signatureService.signByClient(body))
		        .setBody(body)
		        .addHeader("Content-Type", "application/json");
		mockServer.enqueue(mockedResponse);
		
		P2PSessionManager p2pSessionManager = new P2PSessionManager(
				userKeysService, signatureService);
		p2pSessionManager.setWhouAreYouEndpoint(uri);
		boolean met = p2pSessionManager.meetWithNode();
		
		assertTrue(met);
	}
	
	@Test
	void postNodeTest() throws URISyntaxException, IOException, InterruptedException {
		MockWebServer mockServer = new MockWebServer();
		URI uri = mockServer.url("/node").uri();
		
		MockResponse mockedResponse = new MockResponse()
				.setStatus("HTTP/1.1 202");
		mockServer.enqueue(mockedResponse);
		
		NodeTO me = new NodeTO();
		me.setPublicKey(userKeysService.getKeys().getPublicAsymetricKeyAsString());
		P2PSessionManager p2pSessionManager = new P2PSessionManager(
				userKeysService, signatureService);
		p2pSessionManager.setNodeEndpoint(uri);
		boolean registered = p2pSessionManager.registerToNode();
		
		assertTrue(registered);
	}
	
	@Test
	void getNodeTest() {
		
	}
	
	@Test
	void postPartTest() {

	}
	
	@Test
	void getPartTest() {

	}
	
}
