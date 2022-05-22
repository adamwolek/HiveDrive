package org.hivedrive.server.listeners;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import org.hivedrive.cmd.service.common.SignatureService;
import org.hivedrive.cmd.service.common.UserKeysService;
import org.hivedrive.cmd.session.P2PSession;
import org.hivedrive.server.repository.NodeRepository;
import org.hivedrive.server.service.N2NConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class OnStartListener implements ApplicationListener<ApplicationReadyEvent> {
	
	private Logger logger = LoggerFactory.getLogger(P2PSession.class);
	
	@Autowired
	private N2NConnectionService connectionService;
	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		try {
			connectionService.manualInit();
		} catch (Exception e) {
			logger.error("Error: ", e);
		}
	}
}
