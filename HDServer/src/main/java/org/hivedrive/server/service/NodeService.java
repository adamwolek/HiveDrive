package org.hivedrive.server.service;

import java.util.List;

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
	
	public NodeEntity saveOrUpdate(NodeTO to) {
		NodeEntity entity = mapper.map(to);
		return repository.save(entity);
	}

	public boolean isAbleToAdd(NodeTO to) {
		//TODO: implementacja
		return true;
	}
	
	public boolean isAbleToUpdate(NodeTO to) {
		//TODO: implementacja
		return true;
	}
	
	public NodeTO getNodeByPublicKey(String publicKey) {
		NodeEntity entity = getNodeEntityByPublicKey(publicKey);
		if (entity != null) {
			return mapper.map(entity);
		}
		return null;
	}

	public NodeEntity getNodeEntityByPublicKey(String publicKey) {
		return repository.findByPublicKey(publicKey);
	}
	
	public List<NodeTO> getAll() {
		List<NodeEntity> entities = (List<NodeEntity>) repository.findAll();
		return mapper.mapEntities(entities);
	}
	
	public void delete(NodeTO to) {
		NodeEntity entity = mapper.map(to);
		if (entity != null) {
			repository.delete(entity);	
		}
	}
	
}
