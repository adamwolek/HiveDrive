package org.hivedrive.server.mappers;

import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.to.NodeTO;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

@Service
public class NodeMapper {

	public NodeEntity map(NodeTO to) {
		NodeEntity entity = new NodeEntity();
		entity.setStatus(to.getStatus());
		entity.setPublicKey(to.getPublicKey());
		entity.setIpAddress(to.getIpAddress());
		return entity;
	}
	
	public NodeTO map(NodeEntity entity) {
		NodeTO to = new NodeTO();
		to.setStatus(entity.getStatus());
		to.setPublicKey(entity.getPublicKey());
		to.setIpAddress(entity.getIpAddress());
		return to;
	}
	
	public String toJson(NodeTO to) {
		Gson gson = new Gson();
		return gson.toJson(to);
	}
	
	public NodeTO fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, NodeTO.class);
	}
}
