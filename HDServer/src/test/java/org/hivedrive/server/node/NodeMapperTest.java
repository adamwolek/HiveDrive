package org.hivedrive.server.node;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.mappers.NodeMapper;
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
@DisplayName("NodeMapperTest")
class NodeMapperTest {

	  @Autowired
	   NodeMapper mapper;
	   NodeTO to;
	   NodeEntity entity;
	   String json;
	    
	   @BeforeEach
	    public void setUp() throws Exception {
	        mapper = new NodeMapper();
	        
	        //given
	        to = new NodeTO();
	        to.setStatus("test status");
	        to.setIpAddress("127.0.0.1");
	        to.setPublicKey("qwertyuiopasdfgjkl");
	        
	        entity = new NodeEntity();
	        entity.setStatus("test status");
	        entity.setIpAddress("127.0.0.1");
	        entity.setPublicKey("qwertyuiopasdfgjkl");
	        
	        json = "{\"publicKey\":\"qwertyuiopasdfgjkl\",\"status\":\"test status\",\"ipAddress\":\"127.0.0.1\"}";
	    }

	    @Test
	    @DisplayName("Map from TO to entity")
	    void mapFromToEntityTest() throws Exception {

	    	//when
	        NodeEntity result = mapper.map(to);
	        
	        //then
	        assertEquals(to.getStatus(), result.getStatus());
	        assertEquals(to.getIpAddress(), result.getIpAddress());
	        assertEquals(to.getPublicKey(), result.getPublicKey());
	    }
	    
	    @Test
	    @DisplayName("Map from TOs to entities")
	    void mapFromTOsToEntitiesTest() throws Exception {
	    	//given
	    	NodeTO to2= new NodeTO();
	        to2.setStatus("test status 2");
	        to2.setIpAddress("127.0.0.1");
	        to2.setPublicKey("asdfghjklzxcvbnm");
	        
	        NodeEntity entity2 = new NodeEntity();
	        entity2.setStatus("test status 2");
	        entity2.setIpAddress("127.0.0.1");
	        entity2.setPublicKey("asdfghjklzxcvbnm");
	        
	    	List<NodeTO> tos = new ArrayList<>();
	    	tos.add(to);
	    	tos.add(to2);
	    	List<NodeEntity> entities = new ArrayList<>();
	    	entities.add(entity);
	    	entities.add(entity2);
	    	
	    	//when
	        List<NodeEntity> result = mapper.mapTOs(tos);
	        
	        //then
	        assertEquals(2, result.size());
	        assertEquals(to.getStatus(), result.get(0).getStatus());
	        assertEquals(to.getIpAddress(), result.get(0).getIpAddress());
	        assertEquals(to.getPublicKey(), result.get(0).getPublicKey());
	        assertEquals(to2.getStatus(), result.get(1).getStatus());
	        assertEquals(to2.getIpAddress(), result.get(1).getIpAddress());
	        assertEquals(to2.getPublicKey(), result.get(1).getPublicKey());
	    }
	    
	    @Test
	    @DisplayName("Map from entity do TO")
	    void mapFromEntityToTO() throws Exception {
	    	
	    	//when
	        NodeTO result = mapper.map(entity);
	        
	        //then
	        assertEquals(entity.getStatus(), result.getStatus());
	        assertEquals(entity.getIpAddress(), result.getIpAddress());
	        assertEquals(entity.getPublicKey(), result.getPublicKey());
	    }
	    
	    @Test
	    @DisplayName("Map from entities to TOS")
	    void mapFromEntitiesToTOSTest() throws Exception {
	    	//given
	    	NodeTO to2= new NodeTO();
	        to2.setStatus("test status 2");
	        to2.setIpAddress("127.0.0.1");
	        to2.setPublicKey("asdfghjklzxcvbnm");
	        
	        NodeEntity entity2 = new NodeEntity();
	        entity2.setStatus("test status 2");
	        entity2.setIpAddress("127.0.0.1");
	        entity2.setPublicKey("asdfghjklzxcvbnm");
	        
	    	List<NodeTO> tos = new ArrayList<>();
	    	tos.add(to);
	    	tos.add(to2);
	    	List<NodeEntity> entities = new ArrayList<>();
	    	entities.add(entity);
	    	entities.add(entity2);
	    	
	    	//when
	        List<NodeTO> result = mapper.mapEntities(entities);
	        
	        //then
	        assertEquals(2, result.size());
	        assertEquals(entity.getStatus(), result.get(0).getStatus());
	        assertEquals(entity.getIpAddress(), result.get(0).getIpAddress());
	        assertEquals(entity.getPublicKey(), result.get(0).getPublicKey());
	        assertEquals(entity2.getStatus(), result.get(1).getStatus());
	        assertEquals(entity2.getIpAddress(), result.get(1).getIpAddress());
	        assertEquals(entity2.getPublicKey(), result.get(1).getPublicKey());
	    }
	    
}
