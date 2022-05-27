package org.hivedrive.server.node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.mappers.NodeMapper;
import org.hivedrive.server.service.NodeService;
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
@DisplayName("NodeServiceTest")
class NodeServiceTest {

	   @Autowired NodeService service;
	   NodeMapper mapper;
	   NodeTO to;
	   NodeEntity entity;
	   String publicKey;
	    
	   @BeforeEach
	    public void setUp() throws Exception {
	        mapper= new NodeMapper();
	        
	        to = new NodeTO();
	        to.setStatus("test status");
	        to.setAddress("127.0.0.1:8080");
	        to.setPublicKey("qwertyuiopasdfgjkl");
	        
	        entity = new NodeEntity();
	        entity.setStatus("test status");
	        entity.setAddress("127.0.0.1:8080");
	        entity.setPublicKey("qwertyuiopasdfgjkl");
	        
	        publicKey = "qwertyuiopasdfgjkl";
	    }
	   
	    @Test
	    @DisplayName("Save or update method")
	    void saveOrUpdateTest() throws Exception {

	    	//when
	        NodeEntity result = service.saveOrUpdate(to);
	        
	        //then
	        assertEquals(to.getStatus(), result.getStatus());
	        assertEquals(to.getAddress(), result.getAddress());
	        assertEquals(to.getAddress(), result.getAddress());
	        assertEquals(to.getPublicKey(), result.getPublicKey());
	    }
	    
	    @Test
	    @DisplayName("Get node entity by public key method") 
	    void getNodeEntityByPublicKeyTest() throws Exception {
	    	//when
	    	NodeEntity result = service.getNodeEntityByPublicKey(publicKey);
	    	
	    	//then
	        assertEquals(publicKey, result.getPublicKey());
	    }
	    
	    @Test
	    @DisplayName("Get node entity by public key method - key doesn't exist") 
	    void getNodeEntityByPublicKeyDoesntExist() throws Exception {
	    	//given
	    	String publicKey2 = "asdfghjklzxcvbnm";
	    	
	    	//when
	    	NodeEntity result = service.getNodeEntityByPublicKey(publicKey2);
	    	
	    	//then
	    	assertNull(result);
	    }
	    
	    @Test
	    @DisplayName("Get node TO by public key method") 
	    void getNodeByPublicKey() throws Exception {
	    	//when
	    	NodeTO result = service.getNodeByPublicKey(publicKey);
	    	
	    	//then
	        assertEquals(publicKey, result.getPublicKey());
	    }
	    
	    @Test
	    @DisplayName("Get node TO by public key method - key doesn't exist") 
	    void getNodeByPublicKeyDoesntExist() throws Exception {
	    	//given
	    	String publicKey2 = "asdfghjklzxcvbnm";
	    	
	    	//when
	    	NodeTO result = service.getNodeByPublicKey(publicKey2);
	    	
	    	//then
	    	assertNull(result);
	    }
	    
	    @Test
	    @DisplayName("Get all nodes") 
	    void getAll() throws Exception {
	    	//when
	    	List<NodeTO> result = service.getAll();
	    	
	    	//then
	    	assertEquals(1, result.size());
	    	assertEquals(to.getStatus(), result.get(0).getStatus());
	        assertEquals(to.getAddress(), result.get(0).getAddress());
	        assertEquals(to.getPublicKey(), result.get(0).getPublicKey());
	    }
	    
	    @Test
	    @DisplayName("Delete node which doesn't exist") 
	    void delete() throws Exception {
	    	//given
	    	NodeTO to2 = new NodeTO();
	        to2.setStatus("test status 2");
	        to2.setAddress("127.0.0.1:8080");
	        to2.setPublicKey("asdfghjklzxcvbnm");
	    	
	    	//when
	    	service.delete(to2);
	    	
	    	//then
	    	List<NodeTO> all = service.getAll();
	    	assertEquals(1, all.size());
	    }
	    
	    @Test
	    @DisplayName("Delete node which exists") 
	    void deleteExisted() throws Exception {
	    	//when
	    	service.delete(to);
	    	
	    	//then
	    	List<NodeTO> all = service.getAll();
	    	assertEquals(0, all.size());
	    }
	    
}
