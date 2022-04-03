package org.hivedrive.server.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

	@Value("${network.ip-address}")
	private String localIpAddress;
	
	private static int port = 8080;
	
	public String getGlobalAddress() throws IOException {
		URL checker = new URL("http://checkip.amazonaws.com");
		BufferedReader in = new BufferedReader(new InputStreamReader(checker.openStream()));
		return in.readLine() + ":" + port;
	}
	
	public String getLocalAddress() throws UnknownHostException {
		//Doesnt work:
		//InetAddress.getLocalHost().getHostAddress()
		
		String localAddress = localIpAddress + ":"  + port;
		return localAddress;
	}
	
}
