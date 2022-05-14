package org.hivedrive.server.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.hivedrive.server.controller.SenderInfo;
import org.hivedrive.server.service.SignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
@Order(1)
public class AuthenticationFilter implements Filter {

	public static String SENDER_ID_HEADER_PARAM = "x-sender-id";
	public static String SIGN_HEADER_PARAM = "x-sign";
	
	@Autowired
	private SenderInfo senderInfo;
	
	@Autowired
	private SignatureService signatureService;
	
	
    @Override
    public void doFilter(
      ServletRequest request, 
      ServletResponse response, 
      FilterChain chain) throws IOException, ServletException {
    	ContentCachingRequestWrapper httpRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
    	senderInfo.setSenderPublicKey(httpRequest.getHeader(SENDER_ID_HEADER_PARAM));
    	senderInfo.setSign(httpRequest.getHeader(SIGN_HEADER_PARAM));
    	senderInfo.setBody(new String(httpRequest.getContentAsByteArray()));
    	boolean verified = signatureService.verifySign(senderInfo.getSign(), senderInfo.getBody(), senderInfo.getSenderPublicKey());
    	senderInfo.setAuthenticated(verified);
    	chain.doFilter(httpRequest, response);
    }


}