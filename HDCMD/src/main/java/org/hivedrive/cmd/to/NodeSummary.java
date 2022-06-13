package org.hivedrive.cmd.to;

import java.util.List;

public class NodeSummary {

	private String id;
	private String address;
	private List<SpaceTO> spaces;

	
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<SpaceTO> getSpaces() {
		return spaces;
	}

	public void setSpaces(List<SpaceTO> spaces) {
		this.spaces = spaces;
	}

	

	
}
