package org.hivedrive.cmd.service;

import java.io.IOException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
	
	
	@PostConstruct
	public void init() {
		metadata = downloadMetadata();
	}

	public void meetMoreNodes() {
		for (String activeNodeIp : metadata.getActiveNodes()) {
			
		}
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
		// TODO Auto-generated method stub
		
	}
	
	
	
}
