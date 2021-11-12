package org.hivedrive.server.mappers;

import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.to.NodeTO;
import org.springframework.stereotype.Service;

@Service
public class NodeMapper {

	public NodeEntity map(NodeTO to) {
		NodeEntity entity = new NodeEntity();
		entity.setNickname(to.getNickname());
		entity.setPublicKey(to.getPublicKey());
		return entity;
	}
	
	public NodeTO map(NodeEntity entity) {
		NodeTO to = new NodeTO();
		to.setNickname(entity.getNickname());
		to.setPublicKey(entity.getPublicKey());
		return to;
	}
	
}
