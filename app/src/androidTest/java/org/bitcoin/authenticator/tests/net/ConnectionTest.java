package org.bitcoin.authenticator.tests.net;

import junit.framework.TestCase;

import org.bitcoin.authenticator.tests.backup.net.Connection;
import org.junit.Test;

public class ConnectionTest extends TestCase {

	@Test
	public void testPort() {
		assertTrue(Connection.PORT == 8222);
	}

	@Test
	public void testDispose() {
//		try {
//			Socket socket = SocketFactory.getDefault().createSocket("example.org", Connection.PORT);
//			DataInputStream in = new DataInputStream(socket.getInputStream());
//			
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//			assertTrue(false);
//		} catch (IOException e) {
//			e.printStackTrace();
//			assertTrue(false);
//		}
	}
}
