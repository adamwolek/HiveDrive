package org.hivedrive.server.repository;

import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.server.entity.PartEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface PartRepository extends CrudRepository<PartEntity, Long> {

	@Override
	PartEntity save(PartEntity part);

	@Query("FROM PartEntity part "
			+ "WHERE part.ownerId = :ownerId "
			+ "AND part.repository = :repository "
			+ "AND part.groupId = :groupId "
			+ "AND part.orderInGroup = :orderInGroup")
	PartTO findPart(String ownerId, String repository, String groupId, Integer orderInGroup);

}
