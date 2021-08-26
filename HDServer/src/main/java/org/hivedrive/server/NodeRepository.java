package org.hivedrive.server;

import java.util.List;

import org.hivedrive.server.entity.Node;
import org.springframework.data.repository.CrudRepository;

public interface NodeRepository extends CrudRepository<Node, Long> {

	
 Node save(Node node);

}
