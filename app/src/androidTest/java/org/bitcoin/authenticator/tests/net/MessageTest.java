package org.bitcoin.authenticator.tests.net;

import com.googlecode.catchexception.CatchException;

import junit.framework.TestCase;

import static org.junit.Assert.*;

import org.bitcoin.authenticator.net.Connection;
import org.bitcoin.authenticator.net.Message;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.Socket;
import java.sql.DriverManager;
import java.util.Arrays;

public class MessageTest extends TestCase {

	@Test
	public void testConstructorWithIPs() {
        try {
            new Message(null);
        }
        catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().equals("No ips were provided"));
        }

        try {

            new Message(new String[] {});
        }
        catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().equals("No ips were provided"));
        }
	}

    @Test
    public void testSendRequestID() {
        String[] ips = new String[] { "127.0.0.1" };

        String reqID = "i am the req id";
        String walletID = "i am the wallet id";
        byte[] payload = null;
        {
            JSONObject jo = new JSONObject();
            jo.put("requestID", reqID);
            jo.put("pairingID", walletID);
            payload = jo.toString().getBytes();
        }

        Socket s = Mockito.mock(Socket.class);

        Message msg = Mockito.mock(Message.class);
        Mockito.when(msg.getIPs()).thenReturn(ips);

        Connection conn = Mockito.mock(Connection.class);
        try {
            Mockito.when(conn.writeContinuous(ips, payload)).thenReturn(null);
        }
        catch(Connection.CannotConnectToWalletException e) { assertTrue(false); }
        Mockito.when(Connection.getInstance()).thenReturn(conn);

        ArgumentCaptor<byte[]> argPayload = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<String[]> argIps = ArgumentCaptor.forClass(String[].class);
        try {
            Mockito.verify(conn, Mockito.atMost(1)).writeContinuous(argIps.capture(), argPayload.capture());

            assertTrue(Arrays.equals(argPayload.getValue(), payload));
            assertTrue(Arrays.equals(argIps.getValue(), ips));
        }
        catch(Connection.CannotConnectToWalletException e) { assertTrue(false); }


    }

}
