package org.hivedrive.server.repository;

import java.util.List;

import org.hivedrive.server.entity.NodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeRepository extends JpaRepository<NodeEntity, Long> {

	
 @SuppressWarnings("unchecked")
 NodeEntity save(NodeEntity node);
 
 NodeEntity findByPublicKey(String publicKey);

 List<NodeEntity> findAll();

 

}
