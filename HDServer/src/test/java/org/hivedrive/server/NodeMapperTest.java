package org.hivedrive.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.mappers.NodeMapper;
import org.hivedrive.server.to.NodeTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("NodeMapperTest")
class NodeMapperTest {

	  @Autowired
	   NodeMapper mapper;
	   NodeTO to;
	   NodeEntity entity;
	   String json;
	    
	   @BeforeEach
	    public void setUp() throws Exception {
	        mapper= new NodeMapper();
	        
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
	    @DisplayName("TO to json")
	    void toJsonTest() throws Exception {
	    	
	    	//when
	        String result = mapper.toJson(to);
	        
	        //then
	        assertEquals(result, json);
	    }
	    
	    @Test
	    @DisplayName("TO from json")
	    void fromJsonTest() throws Exception {
	    	
	    	//when
	        NodeTO result = mapper.fromJson(json);
	        
	        //then
	        assertEquals(to.getStatus(), result.getStatus());
	        assertEquals(to.getIpAddress(), result.getIpAddress());
	        assertEquals(to.getPublicKey(), result.getPublicKey());
	    }
	    
}
