package org.hivedrive.server.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.hivedrive.server.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
@Order(2)
public class MeetFriendsFilter implements Filter {

	public static final String SENDER_TYPE_HEADER_PARAM = "x-sender-type";
	
	@Override
    public void doFilter(
      ServletRequest request, 
      ServletResponse response, 
      FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String addressOfClient = httpRequest.getRemoteAddr();
		String senderType = httpRequest.getHeader(SENDER_TYPE_HEADER_PARAM);
		if(senderType.equals("node")) {
			
		}
    	chain.doFilter(request, response);
    }
	
}
