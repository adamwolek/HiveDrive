package org.hivedrive.server.repository;

import java.util.Collection;

import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.server.entity.PartEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PartRepository extends CrudRepository<PartEntity, Long> {

	@Override
	PartEntity save(PartEntity part);

	@Query("FROM PartEntity part "
			+ "WHERE part.node.publicKey = :ownerId "
			+ "AND part.repository = :repository "
			+ "AND part.groupId = :groupId "
			+ "AND part.orderInGroup = :orderInGroup")
	PartEntity findPart(
			@Param("ownerId") String ownerId, 
			@Param("repository") String repository, 
			@Param("groupId") String groupId, 
			@Param("orderInGroup") Integer orderInGroup);

	@Query("FROM PartEntity part "
			+ "WHERE part.node.publicKey = :ownerId "
			+ "AND part.repository = :repository ")
	Collection<PartEntity> findPart(
			@Param("ownerId") String ownerId, 
			@Param("repository") String repository);

}
