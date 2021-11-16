package org.hivedrive.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.mappers.NodeMapper;
import org.hivedrive.server.service.NodeService;
import org.hivedrive.server.to.NodeTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("NodeServiceTest")
class NodeServiceTest {

	   @Autowired NodeService service;
	   NodeMapper mapper;
	   NodeTO to;
	   NodeEntity entity;
	    
	   @BeforeEach
	    public void setUp() throws Exception {
	        mapper= new NodeMapper();
	        
	        to = new NodeTO();
	        to.setStatus("test status");
	        to.setIpAddress("127.0.0.1");
	        to.setPublicKey("qwertyuiopasdfgjkl");
	    }

	    @Test
	    @DisplayName("Post method")
	    void postTest() throws Exception {

	    	//when
	        NodeEntity result = service.post(to);
	        
	        //then
	        assertNotNull(result.getId());
	        assertEquals(to.getStatus(), result.getStatus());
	        assertEquals(to.getIpAddress(), result.getIpAddress());
	        assertEquals(to.getPublicKey(), result.getPublicKey());
	    }
}
