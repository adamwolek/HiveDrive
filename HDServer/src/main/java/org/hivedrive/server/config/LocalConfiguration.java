package org.hivedrive.server.config;

import java.util.List;

public class LocalConfiguration {

	private List<SpaceConfig> spaces;
	private List<String> centralServers;
	private boolean testMode;
	
	
	
	public boolean isTestMode() {
		return testMode;
	}

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	public List<String> getCentralServers() {
		return centralServers;
	}

	public void setCentralServers(List<String> centralServers) {
		this.centralServers = centralServers;
	}

	public List<SpaceConfig> getSpaces() {
		return spaces;
	}

	public void setSpaces(List<SpaceConfig> spaces) {
		this.spaces = spaces;
	}
	
	
}
