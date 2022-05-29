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
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@AutoConfigureTestDatabase(replace = Replace.ANY)
@DisplayName("NodeMapperTest")
@ActiveProfiles("test")
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
	        to.setAddress("127.0.0.1:8080");
	        to.setPublicKey("qwertyuiopasdfgjkl");
	        
	        entity = new NodeEntity();
	        entity.setStatus("test status");
	        entity.setAddress("127.0.0.1:8080");
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
	        assertEquals(to.getAddress(), result.getAddress());
	        assertEquals(to.getPublicKey(), result.getPublicKey());
	    }
	    
	    @Test
	    @DisplayName("Map from TOs to entities")
	    void mapFromTOsToEntitiesTest() throws Exception {
	    	//given
	    	NodeTO to2= new NodeTO();
	        to2.setStatus("test status 2");
	        to2.setAddress("127.0.0.1:8080");
	        to2.setPublicKey("asdfghjklzxcvbnm");
	        
	        NodeEntity entity2 = new NodeEntity();
	        entity2.setStatus("test status 2");
	        entity2.setAddress("127.0.0.1:8080");
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
	        assertEquals(to.getAddress(), result.get(0).getAddress());
	        assertEquals(to.getPublicKey(), result.get(0).getPublicKey());
	        assertEquals(to2.getStatus(), result.get(1).getStatus());
	        assertEquals(to2.getAddress(), result.get(1).getAddress());
	        assertEquals(to2.getPublicKey(), result.get(1).getPublicKey());
	    }
	    
	    @Test
	    @DisplayName("Map from entity do TO")
	    void mapFromEntityToTO() throws Exception {
	    	
	    	//when
	        NodeTO result = mapper.map(entity);
	        
	        //then
	        assertEquals(entity.getStatus(), result.getStatus());
	        assertEquals(entity.getAddress(), result.getAddress());
	        assertEquals(entity.getPublicKey(), result.getPublicKey());
	    }
	    
	    @Test
	    @DisplayName("Map from entities to TOS")
	    void mapFromEntitiesToTOSTest() throws Exception {
	    	//given
	    	NodeTO to2= new NodeTO();
	        to2.setStatus("test status 2");
	        to2.setAddress("127.0.0.1:8080");
	        to2.setPublicKey("asdfghjklzxcvbnm");
	        
	        NodeEntity entity2 = new NodeEntity();
	        entity2.setStatus("test status 2");
	        entity2.setAddress("127.0.0.1:8080");
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
	        assertEquals(entity.getAddress(), result.get(0).getAddress());
	        assertEquals(entity.getPublicKey(), result.get(0).getPublicKey());
	        assertEquals(entity2.getStatus(), result.get(1).getStatus());
	        assertEquals(entity2.getAddress(), result.get(1).getAddress());
	        assertEquals(entity2.getPublicKey(), result.get(1).getPublicKey());
	    }
	    
}
