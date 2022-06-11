package org.hivedrive.cmd.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hivedrive.cmd.model.NodeEntity;
import org.springframework.stereotype.Component;

@Component
public class NodeRepository {

	private List<NodeEntity> nodes = Collections.synchronizedList(new ArrayList<>());

	public List<NodeEntity> getAllNodes() {
		return new ArrayList<>(this.nodes);
	}

	public NodeEntity save(NodeEntity node) {
		NodeEntity existingNode = this.findByPublicKey(node.getPublicKey());
		if (existingNode == null) {
			nodes.add(node);
			return node;
		} else {
			existingNode.setAddress(node.getAddress());
			existingNode.setStatus(node.getStatus());
			existingNode.setFreeSpace(node.getFreeSpace());
			existingNode.setUsedSpace(node.getUsedSpace());
			return existingNode;
		}
	}

	public NodeEntity findByPublicKey(String publicKey) {
		for (NodeEntity node : nodes) {
			if (publicKey.equals(node.getPublicKey())) {
				return node;
			}
		}
		return null;
	}

}