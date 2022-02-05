package org.hivedrive.cmd.service;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hivedrive.cmd.model.NodeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatusService {

	
	private ConnectionService connectionService;
	
	@Autowired
	public StatusService(ConnectionService connectionService) {
		this.connectionService = connectionService;
	}

	public String getNodeStatistics(String nodeAddress) {
		String message = "";
		try {
			List<NodeEntity> allNodes = connectionService.getAllKnonwNodes(nodeAddress);
			message += "Node knows " + allNodes.size() + " another nodes";
		} catch (Exception e) {
			e.printStackTrace();
			message += "Error: " + ExceptionUtils.getMessage(e);
		}
		return message;
	}
	
}
