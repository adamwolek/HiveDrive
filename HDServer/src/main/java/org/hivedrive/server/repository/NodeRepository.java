package org.hivedrive.server.repository;

import java.util.List;

import org.hivedrive.server.entity.NodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NodeRepository extends JpaRepository<NodeEntity, Long> {

	
 @SuppressWarnings("unchecked")
 NodeEntity save(NodeEntity node);
 
 @Query("from NodeEntity where publicKey like :publicKey ")
 NodeEntity findByPublicKey(@Param("publicKey") String publicKey);

 List<NodeEntity> findAll();

 

}
