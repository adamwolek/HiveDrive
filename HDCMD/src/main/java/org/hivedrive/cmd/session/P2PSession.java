package org.hivedrive.cmd.session;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.to.NodeSummary;
import org.hivedrive.cmd.to.NodeTO;
import org.hivedrive.cmd.to.PartTO;

public interface P2PSession {

	static final String SENDER_TYPE_HEADER_PARAM = "x-sender-type";
	static final String SENDER_ID_HEADER_PARAM = "x-sender-id";
	static final String SENDER_ADDRESS_HEADER_PARAM = "x-sender-address";
	static final String SIGN_HEADER_PARAM = "x-sign";
	
	byte[] getContent(PartTO part);
	
	List<PartTO> findPartsByRepository(String repository);
	
	PartTO downloadPart(PartInfo part);
	
	List<PartTO> getAllParts() throws URISyntaxException, IOException, InterruptedException;
	
	List<NodeTO> getAllNodes();
	
	boolean send(PartTO part);
	
	void sendContent(Long partId, File part);
	
	NodeTO getNode();
	
	boolean meetWithNode();
	
	boolean doesFileExistGet(String globalFileId);
	
	P2PSession fromClientToAddress(String address);

	P2PSession fromNodeToAddress(String address);

	boolean isAccepted(PartTO part);

	NodeSummary getSummary();

	boolean deletePart(PartTO part);

}
