package org.hivedrive.server.mappers;

import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.entity.PartEntity;
import org.hivedrive.server.service.NodeService;
import org.hivedrive.server.to.PartTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PartMapper {

	@Autowired
	NodeService nodeService;
	
	PartEntity map(PartTO to) {
		PartEntity entity = new PartEntity();
		entity.setCreateDate(to.getCreateDate());
		entity.setGlobalId(to.getGlobalId());
		entity.setGroupId(to.getGroupId());
		entity.setOrderInGroup(to.getOrderInGroup());
		entity.setRepository(to.getRepository());
		NodeEntity node = nodeService.getNodeEntityByPublicKey(to.getOwnerId());
		entity.setNode(node);
		return entity;
	}
	
	PartEntity map(PartEntity to) {
		PartEntity entity = new PartEntity();
		entity.setCreateDate(to.getCreateDate());
		entity.setGlobalId(to.getGlobalId());
		entity.setGroupId(to.getGroupId());
		entity.setOrderInGroup(to.getOrderInGroup());
		entity.setRepository(to.getRepository());
		NodeEntity node = nodeService.getNodeEntityByPublicKey(to.getOwnerId());
		entity.setNode(node);
		return entity;
	}
}
