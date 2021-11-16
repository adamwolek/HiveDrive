package org.hivedrive.server.service;

import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.mappers.NodeMapper;
import org.hivedrive.server.repository.NodeRepository;
import org.hivedrive.server.to.NodeTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NodeService {

	@Autowired
	private NodeRepository repository;
	
	@Autowired
	private NodeMapper mapper;
	
	public NodeEntity post(NodeTO to) {
		NodeEntity entity = mapper.map(to);
		return repository.save(entity);
	}
	
}
