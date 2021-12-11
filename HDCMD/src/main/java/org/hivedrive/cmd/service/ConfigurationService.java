package org.hivedrive.cmd.service;

import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
	
	public int bestNumberOfCopies = 6;

	public int getBestNumberOfCopies() {
		return bestNumberOfCopies;
	}

	public void setBestNumberOfCopies(int bestNumberOfCopies) {
		this.bestNumberOfCopies = bestNumberOfCopies;
	}
	
	

}
