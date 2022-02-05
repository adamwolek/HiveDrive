package org.hivedrive.cmd.tool;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.model.UserKeys;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JSONUtils {


	public static ObjectMapper mapper() {
		ObjectMapper mapper  = new ObjectMapper();
		mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
		                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
		                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
		                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
		                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
		return mapper;
	}
	
	public static ObjectWriter createWrtier() {
		return mapper().writerWithDefaultPrettyPrinter();
	}
	
	public static void write(File file, Object object) {
		try {
			String json = createWrtier()
					.writeValueAsString(object);
			FileUtils.writeStringToFile(file, json, "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
	
	public static <T> T read(File file, Class<T> clazz) {
		try {
			String json = FileUtils.readFileToString(file, "UTF-8");
			T object = JSONUtils.mapper().readValue(json, clazz);
			return object;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
}
