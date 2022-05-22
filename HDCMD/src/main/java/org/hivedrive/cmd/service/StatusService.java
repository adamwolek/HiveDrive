package org.hivedrive.cmd.service;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hivedrive.cmd.model.NodeEntity;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.to.PartTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatusService {

	
	private C2NConnectionService connectionService;
	
	@Autowired
	public StatusService(C2NConnectionService connectionService) {
		this.connectionService = connectionService;
	}

	public String getNodeStatistics(String nodeAddress) {
		String message = "";
		try {
			List<NodeEntity> allNodes = connectionService.getAllKnownNodes(nodeAddress);
			message += "Node knows " + allNodes.size() + " another nodes";
			message += "\n";
			
			List<PartTO> allParts = connectionService.getAllPartsStoredOnNode(nodeAddress);
			message += "Node stores " + allParts.size() + " parts";
			message += "\n";
		} catch (Exception e) {
			e.printStackTrace();
			message += "Error: " + ExceptionUtils.getMessage(e);
		}
		return message;
	}
	
}
