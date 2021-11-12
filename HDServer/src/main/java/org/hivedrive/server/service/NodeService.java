package org.hivedrive.server.service;

import org.hivedrive.server.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NodeService {

	@Autowired
	private NodeRepository repository;
	
	
}
