package org.hivedrive.server.repository;

import org.hivedrive.server.entity.NodeEntity;
import org.springframework.data.repository.CrudRepository;

public interface NodeRepository extends CrudRepository<NodeEntity, Long> {

	
 NodeEntity save(NodeEntity node);
 NodeEntity findByPublicKey(String publicKey);

}
