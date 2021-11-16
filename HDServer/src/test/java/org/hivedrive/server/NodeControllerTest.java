package org.hivedrive.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hivedrive.server.controller.NodeController;
import org.hivedrive.server.mappers.NodeMapper;
import org.hivedrive.server.to.NodeTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@DisplayName("NodeControllerTest")
class NodeControllerTest {

	   @Autowired
	   private WebApplicationContext webApplicationContext;
	   private MockMvc mockMvc;
	   @Autowired NodeController controller;
	   NodeMapper mapper;
	   NodeTO to;
	    
	    @BeforeEach
	    public void setUp() throws Exception {
	    	mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	        mapper= new NodeMapper();
	        
	        to = new NodeTO();
	        to.setStatus("test status");
	        to.setIpAddress("127.0.0.1");
	        to.setPublicKey("qwertyuiopasdfgjkl");
	    }

	    @Test
	    @DisplayName("post")
	    void postTest() throws Exception {
	        mockMvc.perform(post("/node")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(mapper.toJson(to)))
	            .andExpect(status().isCreated());
	    }
}
