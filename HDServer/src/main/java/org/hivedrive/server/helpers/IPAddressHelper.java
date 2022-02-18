package org.hivedrive.server.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;

public class IPAddressHelper {

	private static int port = 8080;
	
	public static String getGlobalAddress() throws IOException {
		URL checker = new URL("http://checkip.amazonaws.com");
		BufferedReader in = new BufferedReader(new InputStreamReader(checker.openStream()));
		return in.readLine() + port;
	}
	
	public static String getLocalAddress() throws UnknownHostException {
		String localAddress = InetAddress.getLocalHost().getHostAddress() + ":"  + port;
		return localAddress;
	}
	
}
