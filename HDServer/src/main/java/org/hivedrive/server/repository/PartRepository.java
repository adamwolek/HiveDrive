package org.hivedrive.server.repository;

import org.hivedrive.server.entity.PartEntity;
import org.springframework.data.repository.CrudRepository;

public interface PartRepository extends CrudRepository<PartEntity, Long> {

	@SuppressWarnings("unchecked")
	PartEntity save(PartEntity part);
}
