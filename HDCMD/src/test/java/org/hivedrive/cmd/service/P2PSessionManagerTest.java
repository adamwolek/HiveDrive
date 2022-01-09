package org.hivedrive.cmd.service;

import static org.junit.Assert.assertNotNull;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.assertj.core.util.Arrays;
import org.checkerframework.checker.nullness.qual.AssertNonNullIfNonNull;
import org.hivedrive.cmd.config.ConfigurationService;
import org.hivedrive.cmd.config.TestConfig;
import org.hivedrive.cmd.model.UserKeys;
import org.hivedrive.cmd.session.P2PSessionManager;
import org.hivedrive.cmd.to.CentralServerMetadata;
import org.hivedrive.cmd.to.NodeTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
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

@ActiveProfiles("unitTests")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class P2PSessionManagerTest {
	
	@Autowired
	ConfigurationService configurationService;
	
	@Autowired
	UserKeysService userKeysService;
	
	@Autowired
	SignatureService signatureService;
	
	@Autowired
	ConnectionService connectionService;
	
	@Test
	void testCentralNode() throws URISyntaxException, IOException, InterruptedException {
		try (MockWebServer centralServer = new MockWebServer(); 
				MockWebServer node1 = new MockWebServer();
				MockWebServer node2 = new MockWebServer()) {
			
			configurationService.setUrlToCentralMetadata(centralServer.url("metadata").uri().toURL());
			
			preapareCentralServer(centralServer, node1, node2);
			preapareNode(node1);
			preapareNode(node2);
			
			connectionService.manualInit();
			
			assertNotNull(node1.takeRequest());
			assertNotNull(node2.takeRequest());
		}
		
		
	}

	private void preapareNode(MockWebServer node) throws JsonProcessingException {
		NodeTO whoIsNode = new NodeTO();
		whoIsNode.setPublicKey(userKeysService.getKeys().getPublicAsymetricKeyAsString());
		whoIsNode.setIpAddress("localhost");
		
		String body = new ObjectMapper().writeValueAsString(whoIsNode);
		MockResponse whoAreYouResponse = new MockResponse()
				.addHeader(P2PSessionManager.SIGN_HEADER_PARAM, 
						signatureService.signByClient(body))
				.setBody(body)
				.addHeader("Content-Type", "application/json");
		node.enqueue(whoAreYouResponse);
		
		MockResponse acceptedResponse = new MockResponse()
				.setStatus("HTTP/1.1 202");
		node.enqueue(acceptedResponse);
	}
	
	private void preapareCentralServer(MockWebServer centralServer, MockWebServer node1, MockWebServer node2)
			throws JsonProcessingException, MalformedURLException {
		CentralServerMetadata metadata = new CentralServerMetadata();
		metadata.setActiveNodes(Lists.newArrayList(
				node1.getHostName() + ":" + node1.getPort(), 
				node2.getHostName() + ":" + node2.getPort()));
		MockResponse centralResponse = new MockResponse()
				.setBody(new ObjectMapper().writeValueAsString(metadata));
		centralServer.enqueue(centralResponse);
	}
	
	@Test
	void whoAreYouTest() throws IOException {
		try(MockWebServer mockServer = new MockWebServer();) {
			URL url = mockServer.url("/whoAreYou").url();
			
			NodeTO nodeTo = new NodeTO();
			nodeTo.setPublicKey(userKeysService.getKeys().getPublicAsymetricKeyAsString());
			nodeTo.setIpAddress(url.getHost());
			
			String body = new ObjectMapper().writeValueAsString(nodeTo);
			MockResponse mockedResponse = new MockResponse()
					.addHeader(P2PSessionManager.SIGN_HEADER_PARAM, 
							signatureService.signByClient(body))
					.setBody(body)
					.addHeader("Content-Type", "application/json");
			mockServer.enqueue(mockedResponse);
			
			P2PSessionManager p2pSessionManager = new P2PSessionManager(
					userKeysService, signatureService);
			p2pSessionManager.setWhouAreYouEndpoint(url);
			boolean met = p2pSessionManager.meetWithNode();
			
			assertTrue(met);
		} 
	}
	
	@Test
	void postNodeTest() throws URISyntaxException, IOException, InterruptedException {
		try(MockWebServer mockServer = new MockWebServer();) {
			URL url = mockServer.url("/node").url();
			
			MockResponse mockedResponse = new MockResponse()
					.setStatus("HTTP/1.1 202");
			mockServer.enqueue(mockedResponse);
			
			NodeTO me = new NodeTO();
			me.setPublicKey(userKeysService.getKeys().getPublicAsymetricKeyAsString());
			P2PSessionManager p2pSessionManager = new P2PSessionManager(
					userKeysService, signatureService);
			p2pSessionManager.setNodeEndpoint(url);
			boolean registered = p2pSessionManager.registerToNode();
			
			assertTrue(registered);
		}
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
