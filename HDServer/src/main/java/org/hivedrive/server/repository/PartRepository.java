package org.hivedrive.server.repository;

import java.util.Collection;
import java.util.List;

import org.hivedrive.server.entity.PartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PartRepository extends JpaRepository<PartEntity, Long> {

	@Override
	PartEntity save(PartEntity part);

	@Query("FROM PartEntity part "
			+ "WHERE part.node.publicKey = :ownerId "
			+ "AND part.repository = :repository "
			+ "AND part.groupId = :groupId "
			+ "AND part.orderInGroup = :orderInGroup ")
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
	

	@Query("SELECT count(part.id) FROM PartEntity part "
			+ "WHERE part.fileHash LIKE :fileHash")
	Long howManyPartsExists(
			@Param("fileHash") String fileHash);

	
	@Query("SELECT SUM(part.size) FROM PartEntity part "
			+ "WHERE part.spaceId LIKE :parentPath")
	Long sizeForPath(@Param("parentPath") String parentPath);
	
	@Query("SELECT SUM(part.size) FROM PartEntity part ")
	Long sizeOfAllParts();
	
	@Query("SELECT part.spaceId, SUM(part.size) FROM PartEntity part GROUP BY part.spaceId ")
	List<Object[]> getUsageOfSpaces();

	@Query("SELECT COUNT(part.size) FROM PartEntity part WHERE part.spaceId = :spaceId ")
	Long countPartsBySpace(@Param("spaceId") String spaceId);

}
