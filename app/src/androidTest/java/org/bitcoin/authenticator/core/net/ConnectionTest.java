package org.bitcoin.authenticator.core.net;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
    public void testReadAndClose() {
        try {
            Socket s = Mockito.mock(Socket.class);
            Mockito.when(s.getSoTimeout()).thenReturn(300);

            InputStream is = IOUtils.toInputStream("some test data for my input stream");
            Mockito.when(s.getInputStream()).thenReturn(is);

            Connection.getInstance().readAndClose(s);
            // assert socket.close() was called
            Mockito.verify(s, Mockito.atLeastOnce()).close();
        }
        catch(Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    @Ignore
    public void testReadContinuous() {
        try {
            Socket s = Mockito.mock(Socket.class);
            Mockito.when(s.getSoTimeout()).thenReturn(300);

            InputStream is = new ByteArrayInputStream("some test data for my input stream".getBytes());



            Mockito.when(s.getInputStream()).thenReturn(is);

            Connection.getInstance().readContinuous(s);
            Mockito.verify(s, Mockito.never()).close();
        }
        catch(Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testGenerateSockeToAuthenticator() {
        Connection conn = Mockito.spy(new Connection());
        Socket s = Mockito.mock(Socket.class);

        String validPongPayload = null;
        {
            JSONObject obj = new JSONObject();
            obj.put("WELCOME_BACK_AUTHENTICATOR","");
            validPongPayload = obj.toString();
        }
        String notValidPongPayload = "not pong payload";

        // test valid
        try {
            Mockito.doReturn(validPongPayload.getBytes()).when(conn).readContinuous(Mockito.any(Socket.class));
            Socket retSocket = conn.generateSockeToAuthenticator(s, new String[] { "" });
            assertTrue(retSocket != null);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }

        // test not reachable IPs
        boolean didThrow = false;
        try {
            Mockito.doReturn(notValidPongPayload.getBytes()).when(conn).readContinuous(Mockito.any(Socket.class));
            Socket retSocket = conn.generateSockeToAuthenticator(s, new String[] { "127.0.0.1" });
        } catch (Exception e) {
            assertTrue(e instanceof Connection.CannotConnectToWalletException);
            didThrow = true;
        }
        assertTrue(didThrow);

    }
}
