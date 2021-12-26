package org.hivedrive.server.part;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.entity.PartEntity;
import org.hivedrive.server.mappers.PartMapper;
import org.hivedrive.server.repository.NodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@AutoConfigureTestDatabase(replace = Replace.ANY)
@DisplayName("PartMapperTest")
class PartMapperTest {

	 @Autowired
	 PartMapper mapper;
	 @Autowired
	 NodeRepository repository;
	 PartTO to;
	 PartEntity entity;
	 PartTO to2;
	 PartEntity entity2;
	 List<PartTO> tos;
	 List<PartEntity> entities;
	 
	 @BeforeEach
    public void setUp() throws Exception {
        //given
		LocalDateTime date = LocalDateTime.now();
		
        to = new PartTO();
        to.setStatus("test status");
        to.setCreateDate(date);
        to.setGlobalId("qwert");
        to.setGroupId("yuiop");
        to.setOrderInGroup(1);
        to.setOwnerId("qwertyuiopasdfgjkl");
        to.setRepository("asdf");
        
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setStatus("test status");
        nodeEntity.setIpAddress("127.0.0.1");
        nodeEntity.setPublicKey("qwertyuiopasdfgjkl");
        
        entity = new PartEntity();
        entity.setPathToPart(new File("C:/User/Test/Documents/test.docx"));
        entity.setStatus("test status");
        entity.setCreateDate(date);
        entity.setGlobalId("qwert");
        entity.setGroupId("yuiop");
        entity.setOrderInGroup(1);
        entity.setNode(nodeEntity);
        entity.setRepository("asdf");
        
        to2 = new PartTO();
        to2.setStatus("test status 2");
        to2.setCreateDate(date);
        to2.setGlobalId("trewq");
        to2.setGroupId("poiuy");
        to2.setOrderInGroup(1);
        to2.setOwnerId("qwertyuiopasdfgjkl");
        to2.setRepository("asdf");
        
        entity2 = new PartEntity();
        entity2.setPathToPart(new File("C:/User/Test/Documents/test.docx"));
        entity2.setStatus("test status 2");
        entity2.setCreateDate(date);
        entity2.setGlobalId("trewq");
        entity2.setGroupId("poiuy");
        entity2.setOrderInGroup(1);
        entity2.setNode(nodeEntity);
        entity2.setRepository("asdf");
        
        tos = new ArrayList<> ();
        tos.add(to);
        tos.add(to2);
        entities = new ArrayList<>();
        entities.add(entity);
        entities.add(entity2);
        
        repository.save(nodeEntity);
        
    }
	 
	@Test
    @DisplayName("Map from TO to entity")
    void mapTOTest() throws Exception {

    	//when
        PartEntity result = mapper.map(to);
        
        //then
        assertEquals(to.getStatus(), result.getStatus());
        assertEquals(to.getCreateDate(), result.getCreateDate());
        assertEquals(to.getGlobalId(), result.getGlobalId());
        assertEquals(to.getGroupId(), result.getGroupId());
        assertEquals(to.getOrderInGroup(), result.getOrderInGroup());
        assertEquals(to.getOwnerId(), result.getNode().getPublicKey());
        assertEquals(to.getRepository(), result.getRepository());
    }
	 
	@Test
    @DisplayName("Map from entities to TOs")
    void mapEntitiesTest() throws Exception {

    	//when
        List<PartTO> result = mapper.mapToTOs(entities);
        
        //then
        assertEquals(2, result.size());
        
        assertEquals(to.getStatus(), result.get(0).getStatus());
        assertEquals(to.getCreateDate(), result.get(0).getCreateDate());
        assertEquals(to.getGlobalId(), result.get(0).getGlobalId());
        assertEquals(to.getGroupId(), result.get(0).getGroupId());
        assertEquals(to.getOrderInGroup(), result.get(0).getOrderInGroup());
        assertEquals(to.getOwnerId(), result.get(0).getOwnerId());
        assertEquals(to.getRepository(), result.get(0).getRepository());
        
        assertEquals(to2.getStatus(), result.get(1).getStatus());
        assertEquals(to2.getCreateDate(), result.get(1).getCreateDate());
        assertEquals(to2.getGlobalId(), result.get(1).getGlobalId());
        assertEquals(to2.getGroupId(), result.get(1).getGroupId());
        assertEquals(to2.getOrderInGroup(), result.get(1).getOrderInGroup());
        assertEquals(to2.getOwnerId(), result.get(1).getOwnerId());
        assertEquals(to2.getRepository(), result.get(1).getRepository());
    }

	
	@Test
    @DisplayName("Map from TOs to entities")
    void mapTOsTest() throws Exception {

    	//when
        List<PartEntity> result = mapper.mapToEntities(tos);
        
        //then
        assertEquals(2, result.size());
        
        assertEquals(entity.getStatus(), result.get(0).getStatus());
        assertEquals(entity.getCreateDate(), result.get(0).getCreateDate());
        assertEquals(entity.getGlobalId(), result.get(0).getGlobalId());
        assertEquals(entity.getGroupId(), result.get(0).getGroupId());
        assertEquals(entity.getOrderInGroup(), result.get(0).getOrderInGroup());
        assertEquals(entity.getNode().getIpAddress(), result.get(0).getNode().getIpAddress());       
        assertEquals(entity.getNode().getPublicKey(), result.get(0).getNode().getPublicKey());
        assertEquals(entity.getNode().getStatus(), result.get(0).getNode().getStatus());
        assertEquals(entity.getRepository(), result.get(0).getRepository());
        
        assertEquals(entity2.getStatus(), result.get(1).getStatus());
        assertEquals(entity2.getCreateDate(), result.get(1).getCreateDate());
        assertEquals(entity2.getGlobalId(), result.get(1).getGlobalId());
        assertEquals(entity2.getGroupId(), result.get(1).getGroupId());
        assertEquals(entity2.getOrderInGroup(), result.get(1).getOrderInGroup());
        assertEquals(entity2.getNode().getIpAddress(), result.get(1).getNode().getIpAddress());       
        assertEquals(entity2.getNode().getPublicKey(), result.get(1).getNode().getPublicKey());
        assertEquals(entity2.getNode().getStatus(), result.get(1).getNode().getStatus());
        assertEquals(entity2.getRepository(), result.get(1).getRepository());
    }
	
	@Test
    @DisplayName("Map from entity to TO")
    void mapEntityTest() throws Exception {

    	//when
        PartTO result = mapper.map(entity);
        
        //then
        assertEquals(entity.getStatus(), result.getStatus());
        assertEquals(entity.getCreateDate(), result.getCreateDate());
        assertEquals(entity.getGlobalId(), result.getGlobalId());
        assertEquals(entity.getGroupId(), result.getGroupId());
        assertEquals(entity.getOrderInGroup(), result.getOrderInGroup());
        assertEquals(entity.getNode().getPublicKey(), result.getOwnerId());
        assertEquals(entity.getRepository(), result.getRepository());
    }

}
