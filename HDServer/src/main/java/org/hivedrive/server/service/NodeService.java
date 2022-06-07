package org.hivedrive.server.service;

import java.util.List;
import java.util.stream.Collectors;

import org.hivedrive.cmd.service.common.AddressService;
import org.hivedrive.cmd.service.common.UserKeysService;
import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.mappers.NodeMapper;
import org.hivedrive.server.mappers.PartMapper;
import org.hivedrive.server.repository.NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NodeService {

	private Logger logger = LoggerFactory.getLogger(NodeService.class);
	
	@Autowired
	private UserKeysService userKeysService;
	
	@Autowired
	private NodeRepository repository;
	
	@Autowired
	private NodeMapper mapper;
	
	@Autowired
	private AddressService addressService;
	
	public NodeEntity saveOrUpdate(NodeTO to) {
		return this.saveOrUpdate(mapper.map(to));
	}

	public NodeEntity saveOrUpdate(NodeEntity newEntity) {
		NodeEntity existingNode = repository.findByPublicKey(newEntity.getPublicKey());
		if(existingNode != null) {
			existingNode.setAddress(newEntity.getAddress());
			existingNode.setStatus(newEntity.getStatus());
			return repository.save(existingNode);
		} else {
			return repository.save(newEntity);
		}
	}
	
	public NodeEntity findNode(String publicKey) {
		NodeTO me = getMe();
		if(me.getPublicKey().equals(publicKey)) {
			return mapper.map(me);
		}
		return repository.findByPublicKey(publicKey);
	}
	
	public List<NodeEntity> findAll() {
		return repository.findAll();
	}

	public List<NodeEntity> findAllWithoutMe() {
		String myId = userKeysService.getKeys().getPublicAsymetricKeyAsString();
		return repository.findAll().stream()
				.filter(node -> !node.getPublicKey().equals(myId))
				.collect(Collectors.toList());
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
	
	public NodeTO getMe() {
		NodeTO me = new NodeTO();
		try {
			me.setAddress(addressService.getMyAddress());
		} catch (Exception e) {
			logger.error("Error: ", e);
		}
		me.setPublicKey(userKeysService.getKeys().getPublicAsymetricKeyAsString());
		return me;
	}
	
}
