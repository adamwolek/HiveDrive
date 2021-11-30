package org.hivedrive.server.helpers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.hivedrive.server.to.NodeTO;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class NodeJsonHelper {

	private NodeJsonHelper() {
	}
	
	public static String toJson(NodeTO to) {
		Gson gson = new Gson();
		return gson.toJson(to);
	}
	
	public static NodeTO fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, NodeTO.class);
	}
	
	public static String toJson(List<NodeTO> tos) {
		Gson gson = new Gson();
		return gson.toJson(tos);
	}
	
	public static List<NodeTO> fromJsonToList(String json) {
		Type listType = new TypeToken<ArrayList<NodeTO>>(){}.getType();
		return new Gson().fromJson(json, listType);
	}
}
