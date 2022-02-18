package org.hivedrive.cmd.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
	
	public URL urlToCentralMetadata;
	public int bestNumberOfCopies = 6;
	
	void init() throws MalformedURLException {
		this.urlToCentralMetadata = new URL("https://hivedrive.org/metadata.json");
	}

	
	
	public URL getUrlToCentralMetadata() {
		return urlToCentralMetadata;
	}



	public void setUrlToCentralMetadata(URL urlToCentralMetadata) {
		this.urlToCentralMetadata = urlToCentralMetadata;
	}



	public int getBestNumberOfCopies() {
		return bestNumberOfCopies;
	}

	public void setBestNumberOfCopies(int bestNumberOfCopies) {
		this.bestNumberOfCopies = bestNumberOfCopies;
	}
	
	

}
