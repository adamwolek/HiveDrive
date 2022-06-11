package org.hivedrive.server.node;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.server.controller.NodeController;
import org.hivedrive.server.helpers.NodeJsonHelper;
import org.hivedrive.server.mappers.NodeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@AutoConfigureTestDatabase(replace = Replace.ANY)
@DisplayName("NodeControllerTest")
@ActiveProfiles("test")
class NodeControllerTest {

	   @Autowired
	   private WebApplicationContext webApplicationContext;
	   private MockMvc mockMvc;
	   @Autowired NodeController controller;
	   NodeMapper mapper;
	   NodeTO to;
	   NodeTO to2;
	   List<NodeTO> tos;
	   String tosJson;
	    
	    @BeforeEach
	    public void setUp() throws Exception {
	    	mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	        mapper= new NodeMapper();
	        
	        to = new NodeTO();
	        to.setStatus("test status");
	        to.setAddress("127.0.0.1:8080");
	        to.setPublicKey("qwertyuiopasdfgjkl");
	        
	        to2 = new NodeTO();
	        to2.setStatus("test status 2");
	        to2.setAddress("127.0.0.1:8080");
	        to2.setPublicKey("asdfghjklzxcvbnm");
	        
	        tos = new ArrayList<>();
	        tos.add(to);
	        
	        tosJson  = "[{\"publicKey\":\"qwertyuiopasdfgjkl\",\"status\":\"test status\",\"address\":\"127.0.0.1:8080\"}]";
	    }

	    @Test
	    @DisplayName("post")
	    void postTest() throws Exception {
	        mockMvc.perform(post("/node")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(NodeJsonHelper.toJson(to)))
	            .andExpect(status().isCreated());
	    }
	    
	    @Test
	    @DisplayName("put")
	    void putTest() throws Exception {
	        mockMvc.perform(put("/node")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(NodeJsonHelper.toJson(to)))
	            .andExpect(status().isOk());
	    }
	    
	    @Test
	    @DisplayName("get existed")
	    void getExistedTest() throws Exception {
	    	//given
	    	String json = "{\"publicKey\":\"qwertyuiopasdfgjkl\",\"status\":\"test status\",\"address\":\"127.0.0.1:8080\"}";
	    	
	    	//when
	    	mockMvc.perform(
    		        get("/node/{publicKey}", "qwertyuiopasdfgjkl"))
	    	//then
    		        .andExpect(status().isOk())
    		        .andExpect(content().string(json)); 
		}
	    
	    @Test
	    @DisplayName("get not existed")
	    void getNotExistedTest() throws Exception {
	    	//when
	    	mockMvc.perform(
    		        get("/node/{publicKey}", "qwertyuiopasdfgjkl"))
	    	//then
    		        .andExpect(status().isNotFound());
	    }
	    
}
