package org.hivedrive.cmd.service.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;

import org.hivedrive.cmd.to.NodeTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

	@Value("${server.port:0}")
	private String port;
	
//	private static int port = 8080;
	
	public String getMyAddress() {
		return getLocalAddress();
	}
	
	private String getGlobalAddress() {
		try {
			URL checker = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(checker.openStream()));
			return in.readLine() + ":" + port;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String getLocalAddress() {
		try(final DatagramSocket socket = new DatagramSocket()){
		  socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
		  String localAddress = socket.getLocalAddress().getHostAddress();
		  return localAddress + ":" + port;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
