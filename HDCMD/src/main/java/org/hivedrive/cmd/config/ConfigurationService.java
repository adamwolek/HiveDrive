package org.hivedrive.cmd.config;

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
	
	private URL urlToCentralMetadata;
	private int bestNumberOfCopies = 6;
	
	@PostConstruct
	void init() throws MalformedURLException {
//		this.urlToCentralMetadata = new URL("https://hivedrive.org/metadata.json");
		this.urlToCentralMetadata = new URL("http://192.168.0.122/metadata.json");
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
