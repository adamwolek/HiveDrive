package org.hivedrive.cmd.space;

import org.hivedrive.cmd.service.ConnectionService;
import org.hivedrive.cmd.service.SignatureService;
import org.hivedrive.cmd.service.UserKeysService;
import org.hivedrive.cmd.session.P2PSession;
import org.hivedrive.cmd.to.NodeTO;
import org.springframework.stereotype.Service;

@Service
public class SpaceService {
	
	private ConnectionService connectionService;
	
	private UserKeysService userKeysService;
	
	private SignatureService signatureService;
	
	public SpaceService(ConnectionService connectionService, UserKeysService userKeysService, SignatureService signatureService) {
		this.connectionService = connectionService;
		this.userKeysService = userKeysService;
		this.signatureService = signatureService;		
	}

	public int defaultSpace() {
		String publicKey = userKeysService.getKeys().getPublicAsymetricKeyAsString();
		NodeTO myServer = connectionService.getMyServerIP(publicKey);
		P2PSession session = new P2PSession(myServer, userKeysService, signatureService);
		return session.getDefaultSpace();
	}

}
