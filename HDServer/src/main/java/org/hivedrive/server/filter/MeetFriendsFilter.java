package org.hivedrive.server.filter;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.hivedrive.cmd.session.P2PSession;
import org.hivedrive.server.entity.ClientEntity;
import org.hivedrive.server.entity.NodeEntity;
import org.hivedrive.server.exception.NodeIsNotRegisteredException;
import org.hivedrive.server.repository.ClientRepository;
import org.hivedrive.server.repository.NodeRepository;
import org.hivedrive.server.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
@Order(2)
public class MeetFriendsFilter implements Filter {

	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ClientRepository clientRepository;
	
	@Override
    public void doFilter(
      ServletRequest request, 
      ServletResponse response, 
      FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		if(!httpRequest.getRequestURL().toString().contains("h2") 
				&& httpRequest.getHeader(P2PSession.SENDER_ID_HEADER_PARAM) != null) {
			String senderId = httpRequest.getHeader(P2PSession.SENDER_ID_HEADER_PARAM);
			String senderType = httpRequest.getHeader(P2PSession.SENDER_TYPE_HEADER_PARAM);
			String addressOfSender = getAddressOfSender(httpRequest);
			if(senderType.equals("node")) {
				NodeEntity entity = new NodeEntity();
				entity.setPublicKey(senderId);
				entity.setAddress(addressOfSender);
				entity.setStatus(NodeEntity.NEW_STATUS);
				nodeService.saveOrUpdate(entity);
			} else if(senderType.equals("client")) {
				NodeEntity relatedNode = nodeService.findNode(senderId);
				if(relatedNode == null) {
					throw new NodeIsNotRegisteredException("Sender id: " + senderId);
				}
				if(relatedNode.getClients() == null) {
					relatedNode.setClients(new ArrayList<>());
				}
				relatedNode.getClients().stream()
				.filter(client -> client.getPublicKey().equals(senderId))
				.findAny()
				.ifPresentOrElse(existingClient -> {
					existingClient.setIpAddress(addressOfSender);
				}, () -> {
					ClientEntity newClient = new ClientEntity();
					newClient.setPublicKey(senderId);
					newClient.setIpAddress(addressOfSender);
					newClient.setStatus(ClientEntity.NEW_STATUS);
					clientRepository.save(newClient);
					
					relatedNode.getClients().add(newClient);
					nodeService.saveOrUpdate(relatedNode);
				});
			}
		} 
    	chain.doFilter(request, response);
    }

	private String getAddressOfSender(HttpServletRequest httpRequest) {
		String addressOfClient = httpRequest.getHeader(P2PSession.SENDER_ADDRESS_HEADER_PARAM);
		String ipAddresSentBySender = addressOfClient.split(":")[0];
		String portSentBySender = addressOfClient.split(":")[1];
		if(!httpRequest.getRemoteAddr().equals(ipAddresSentBySender)) {
			addressOfClient = httpRequest.getRemoteAddr() + ":" + portSentBySender;
		}
		return addressOfClient;
	}
	
}
