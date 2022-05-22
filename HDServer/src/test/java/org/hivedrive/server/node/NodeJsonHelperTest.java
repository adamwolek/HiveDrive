package org.hivedrive.server.node;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.helpers.NodeJsonHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = Replace.ANY)
@DisplayName("NodeJsonHelperTest")
class NodeJsonHelperTest {
	
	NodeTO to;
	NodeEntity entity;
	String json;
	   
	@BeforeEach
    public void setUp() throws Exception {
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
    @DisplayName("TO to json")
    void toJsonTest() throws Exception {
    	
    	//when
        String result = NodeJsonHelper.toJson(to);
        
        //then
        assertEquals(result, json);
    }
    
    @Test
    @DisplayName("TO from json")
    void fromJsonTest() throws Exception {
    	
    	//when
        NodeTO result = NodeJsonHelper.fromJson(json);
        
        //then
        assertEquals(to.getStatus(), result.getStatus());
        assertEquals(to.getIpAddress(), result.getIpAddress());
        assertEquals(to.getPublicKey(), result.getPublicKey());
    }
    
    @Test
    @DisplayName("List TOs to json")
    void listToJsonTest() throws Exception {
    	//given
    	NodeTO to2 = new NodeTO();
    	to2.setStatus("test status 2");
	    to2.setIpAddress("127.0.0.1");
	    to2.setPublicKey("asdfghjklzxcvbnm");
	    
	    List<NodeTO> tos = new ArrayList<>();
	    tos.add(to);
	    tos.add(to2);
	    
	    String jsonList = "[{\"publicKey\":\"qwertyuiopasdfgjkl\",\"status\":\"test status\",\"ipAddress\":\"127.0.0.1\"},{\"publicKey\":\"asdfghjklzxcvbnm\",\"status\":\"test status 2\",\"ipAddress\":\"127.0.0.1\"}]";
    	
    	//when
        String result = NodeJsonHelper.toJson(tos);
        
        //then
        assertEquals(jsonList, result);
    }
    
    @Test
    @DisplayName("List TOs from json")
    void listFromJsonTest() throws Exception {
    	//given
    	NodeTO to2 = new NodeTO();
    	to2.setStatus("test status 2");
	    to2.setIpAddress("127.0.0.1");
	    to2.setPublicKey("asdfghjklzxcvbnm");
	    
	    List<NodeTO> tos = new ArrayList<>();
	    tos.add(to);
	    tos.add(to2);
	    
	    String jsonList = "[{\"publicKey\":\"qwertyuiopasdfgjkl\",\"status\":\"test status\",\"ipAddress\":\"127.0.0.1\"},{\"publicKey\":\"asdfghjklzxcvbnm\",\"status\":\"test status 2\",\"ipAddress\":\"127.0.0.1\"}]";
    	
    	//when
        List<NodeTO> result = NodeJsonHelper.fromJsonToList(jsonList);
        
        //then
        assertEquals(2, result.size());
        assertEquals(to.getStatus(), result.get(0).getStatus());
        assertEquals(to.getAccessibleIP(), result.get(0).getAccessibleIP());
        assertEquals(to.getPublicKey(), result.get(0).getPublicKey());
        assertEquals(to2.getStatus(), result.get(1).getStatus());
        assertEquals(to2.getIpAddress(), result.get(1).getIpAddress());
        assertEquals(to2.getPublicKey(), result.get(1).getPublicKey());
    }

}
