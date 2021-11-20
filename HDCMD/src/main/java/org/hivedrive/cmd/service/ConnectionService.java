package org.hivedrive.cmd.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hivedrive.cmd.exception.ConnectToCentralMetadataServerException;
import org.hivedrive.cmd.exception.ReadDataFromMetadataServerException;
import org.hivedrive.cmd.model.Node;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.to.CentralServerMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ConnectionService {
	
	public static String URL_TO_CENTRAL_METADATA = "https://hivedrive.org/metadata.json";
	private CentralServerMetadata metadata;
	
	private List<Node> knownNodes = new ArrayList<>();
	
	private Executor executor = Executors.newFixedThreadPool(5);
	
	@PostConstruct
	public void init() throws URISyntaxException, IOException, InterruptedException {
//		this.metadata = downloadMetadata();
//		this.knownNodes = extractInitialKnownNodes(metadata);
//		meetMoreNodes();
		
		meetNode("");
	}

	private List<Node> extractInitialKnownNodes(CentralServerMetadata metadata) {
		return metadata.getActiveNodes().stream()
		.map(ip -> registerNodeByIp(ip))
		.collect(Collectors.toList());
	}

	private Node registerNodeByIp(String ip) {
		// TODO Auto-generated method stub
		return null;
	}

	private void meetMoreNodes() throws URISyntaxException, IOException, InterruptedException {
		for (String activeNodeIp : metadata.getActiveNodes()) {
			meetNode(activeNodeIp);
		}
	}

	private void meetNode(String ip) throws URISyntaxException, IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
//					  .uri(new URI(activeNodeIp + "/"))
				  .uri(new URI("https://httpbin.org/get"))
				  .GET()
				  .build();
		HttpResponse<String> response = HttpClient
				  .newBuilder()
				  .build()
				  .send(request, BodyHandlers.ofString());
		String body = response.body();
		System.out.println(body);
	}
	
	private CentralServerMetadata downloadMetadata() {
		try {
			String json = IOUtils.toString(new URL(URL_TO_CENTRAL_METADATA), "UTF-8");
			ObjectMapper objectMapper = new ObjectMapper();
			CentralServerMetadata metadata = objectMapper.readValue(json, CentralServerMetadata.class);
			return metadata;
		} catch (JsonProcessingException e) {
			throw new ReadDataFromMetadataServerException(e);
		} catch (IOException e) {
			throw new ConnectToCentralMetadataServerException(e);
		}
	}


	public void send(PartInfo part) {
		
		
	}
	
	
	
}
