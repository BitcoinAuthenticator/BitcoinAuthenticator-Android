package org.bitcoin.authenticator.tests.net;

import junit.framework.TestCase;

import org.bitcoin.authenticator.net.Connection;
import org.json.simple.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import javax.net.ServerSocketFactory;

public class ConnectionTest extends TestCase {

	@Test
	public void testPort() {
		assertTrue(Connection.PORT == 8222);
	}

    @Test
    public void testWriteAndClose() {
        byte[] payload = "I am the payload".getBytes();
        try {
            Socket s = Mockito.mock(Socket.class);
            DataOutputStream dos = Mockito.mock(DataOutputStream.class);
            Mockito.when(s.getOutputStream()).thenReturn(dos);

            Connection.getInstance().writeAndClose(s, payload);

            // assert socket.close() was called
            Mockito.verify(s, Mockito.atLeastOnce()).close();
        }
        catch(IOException e) {
            assertTrue(false);
            e.printStackTrace();
        }
        catch(Connection.CannotConnectToWalletException e) {
            assertTrue(false);
            e.printStackTrace();
        }
    }

    @Test
    public void testwriteContinuous() {
        byte[] payload = "I am the payload".getBytes();
        try {
            Socket s = Mockito.mock(Socket.class);
            DataOutputStream dos = Mockito.mock(DataOutputStream.class);
            Mockito.when(s.getOutputStream()).thenReturn(dos);

            Connection.getInstance().writeContinuous(s, payload);

            // assert socket.close() was not called
            Mockito.verify(s, Mockito.never()).close();
        }
        catch(IOException e) {
            assertTrue(false);
            e.printStackTrace();
        }
        catch(Connection.CannotConnectToWalletException e) {
            assertTrue(false);
            e.printStackTrace();
        }
    }

    @Test
    @Ignore
    public void testGenerateSocketToAuthenticator() {
//        byte[] payload = null;
//        {
//            JSONObject jo = new JSONObject();
//            jo.put("WELCOME_BACK_AUTHENTICATOR","");
//            // payload is {"WELCOME_BACK_AUTHENTICATOR":""}
//            payload = jo.toJSONString().getBytes();
//        }
//        String[] ips = new String[] { "127.0.0.1", "127.0.0.2" };
//        Socket s = Mockito.mock(Socket.class);
//        try {
//            Mockito.when(s.getSoTimeout()).thenReturn(1000);
//
//            InputStream in = Mockito.mock(InputStream.class);
//            Mockito.when(in.read()).thenReturn((int)payload[0])
//                                    .thenReturn((int)payload[1])
//                                    .thenReturn((int)payload[2])
//                                    .thenReturn((int)payload[3])
//                                    .thenReturn((int)payload[4])
//                                    .thenReturn((int)payload[5])
//                                    .thenReturn((int)payload[6])
//                                    .thenReturn((int)payload[7])
//                                    .thenReturn((int)payload[8])
//                                    .thenReturn((int)payload[9])
//                                    .thenReturn((int)payload[10])
//                                    .thenReturn((int)payload[11])
//                                    .thenReturn((int)payload[12])
//                                    .thenReturn((int)payload[13])
//                                    .thenReturn((int)payload[14])
//                                    .thenReturn((int)payload[15])
//                                    .thenReturn((int)payload[16])
//                                    .thenReturn((int)payload[17])
//                                    .thenReturn((int)payload[18])
//                                    .thenReturn((int)payload[19])
//                                    .thenReturn((int)payload[20])
//                                    .thenReturn((int)payload[21])
//                                    .thenReturn((int)payload[22])
//                                    .thenReturn((int)payload[23])
//                                    .thenReturn((int)payload[24])
//                                    .thenReturn((int)payload[25])
//                                    .thenReturn((int)payload[26])
//                                    .thenReturn((int)payload[27])
//                                    .thenReturn((int)payload[28])
//                                    .thenReturn((int)payload[29])
//                                    .thenReturn((int)payload[30])
//                                    .thenReturn((int)payload[31])
//                                    .thenReturn((int)payload[32]);
//
//            Mockito.when(s.getInputStream()).thenReturn(in);
//        } catch (SocketException e) {
//            e.printStackTrace();
//            assertTrue(false);
//        } catch (IOException e) {
//            e.printStackTrace();
//            assertTrue(false);
//        }
//
//        /*
//            Valid ips
//         */
//        try {
//            Connection.getInstance().generateSockeToAuthenticator(s, ips);
//            Mockito.verify(s, Mockito.atLeastOnce()).connect(new InetSocketAddress("127.0.0.1", Connection.PORT), 1000);
//        } catch (Connection.CannotConnectToWalletException e) {
//            e.printStackTrace();
//            assertTrue(false);
//        } catch (IOException e) {
//            e.printStackTrace();
//            assertTrue(false);
//        }
    }
}
