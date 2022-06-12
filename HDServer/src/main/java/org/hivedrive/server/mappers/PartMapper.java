package org.hivedrive.server.mappers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.hivedrive.cmd.service.common.AddressService;
import org.hivedrive.cmd.service.common.UserKeysService;
import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.entity.PartEntity;
import org.hivedrive.server.service.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PartMapper {

	private Logger logger = LoggerFactory.getLogger(PartMapper.class);
	
	@Autowired
	NodeService nodeService;
	
	@Autowired
	UserKeysService userKeysService;
	
	@Autowired
	AddressService addressHelper;

	public PartEntity map(PartTO to) {
		PartEntity entity = new PartEntity();
		entity.setId(to.getId());
		entity.setStatus(to.getStatus());
		entity.setCreateDate(to.getCreateDate());
		entity.setGroupId(to.getGroupId());
		entity.setOrderInGroup(to.getOrderInGroup());
		entity.setRepository(to.getRepository());
		entity.setFileId(to.getFileId());
		entity.setEncryptedFileMetadata(to.getEncryptedFileMetadata());
		NodeEntity node = nodeService.getNodeEntityByPublicKey(to.getOwnerId());
		entity.setNode(node);
		return entity;
	}

	public List<PartEntity> mapToEntities(Collection<PartTO> tos) {
		return tos.stream().map(this::map).collect(Collectors.toList());
	}

	public PartTO map(PartEntity entity) {
		PartTO to = new PartTO();
		to.setId(entity.getId());
		to.setStatus(entity.getStatus());
		to.setCreateDate(entity.getCreateDate());
		to.setFileId(entity.getFileId());
		to.setGroupId(entity.getGroupId());
		to.setOrderInGroup(entity.getOrderInGroup());
		to.setRepository(entity.getRepository());
		to.setOwnerId(entity.getNode().getPublicKey());
		to.setNodeWhichContainsPart(nodeService.getMe());
		to.setEncryptedFileMetadata(entity.getEncryptedFileMetadata());
		return to;
	}

	public List<PartTO> mapToTOs(Collection<PartEntity> tos) {
		return tos.stream().map(this::map).collect(Collectors.toList());
	}

}
