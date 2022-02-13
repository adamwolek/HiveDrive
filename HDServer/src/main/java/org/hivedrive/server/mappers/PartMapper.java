package org.hivedrive.server.mappers;

import java.util.List;
import java.util.stream.Collectors;

import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.entity.PartEntity;
import org.hivedrive.server.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PartMapper {

	@Autowired
	NodeService nodeService;

	public PartEntity map(PartTO to) {
		PartEntity entity = new PartEntity();
		entity.setStatus(to.getStatus());
		entity.setCreateDate(to.getCreateDate());
		entity.setGlobalId(to.getGlobalId());
		entity.setGroupId(to.getGroupId());
		entity.setOrderInGroup(to.getOrderInGroup());
		entity.setRepository(to.getRepository());
		entity.setGlobalId(to.getGlobalId());
		NodeEntity node = nodeService.getNodeEntityByPublicKey(to.getOwnerId());
		entity.setNode(node);
		return entity;
	}

	public List<PartEntity> mapToEntities(List<PartTO> tos) {
		return tos.stream().map(this::map).collect(Collectors.toList());
	}

	public PartTO map(PartEntity entity) {
		PartTO to = new PartTO();
		to.setStatus(entity.getStatus());
		to.setCreateDate(entity.getCreateDate());
		to.setGlobalId(entity.getGlobalId());
		to.setGroupId(entity.getGroupId());
		to.setOrderInGroup(entity.getOrderInGroup());
		to.setRepository(entity.getRepository());
		to.setGlobalId(entity.getGlobalId());
		to.setOwnerId(entity.getNode().getPublicKey());
		return to;
	}

	public List<PartTO> mapToTOs(List<PartEntity> tos) {
		return tos.stream().map(this::map).collect(Collectors.toList());
	}

}
