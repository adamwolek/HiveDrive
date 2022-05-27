package org.hivedrive.server.repository;

import org.hivedrive.server.entity.ClientEntity;
import org.hivedrive.server.entity.NodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long>  {

}
