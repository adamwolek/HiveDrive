package org.hivedrive.server.mappers;

import java.util.ArrayList;
import java.util.List;

import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.to.NodeTO;
import org.springframework.stereotype.Service;

@Service
public class NodeMapper {

	public NodeEntity map(NodeTO to) {
		NodeEntity entity = new NodeEntity();
		entity.setStatus(to.getStatus());
		entity.setPublicKey(to.getPublicKey());
		entity.setIpAddress(to.getIpAddress());
		return entity;
	}
	
	public List<NodeEntity> mapTOs(List<NodeTO> toes) {
		List<NodeEntity> entities = new ArrayList<>();
		for (NodeTO to : toes) {
			entities.add(map(to));
		}
		return entities;
	}
	
	public NodeTO map(NodeEntity entity) {
		NodeTO to = new NodeTO();
		to.setStatus(entity.getStatus());
		to.setPublicKey(entity.getPublicKey());
		to.setIpAddress(entity.getIpAddress());
		return to;
	}
	
	public List<NodeTO> mapEntities(List<NodeEntity> entities) {
		List<NodeTO> toes = new ArrayList<>();
		for (NodeEntity entity : entities) {
			toes.add(map(entity));
		}
		return toes;
	}
	
}
