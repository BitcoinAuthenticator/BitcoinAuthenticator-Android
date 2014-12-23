package org.bitcoin.authenticator.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;


import android.util.Log;

/**	
 * This class creates a new TCP socket connection to the wallet as well as the data stream input/output objects.
 */
public class Connection {
	
	public static final int PORT = 8222;
	
	private static Connection instance;
	public static Connection getInstance() {
		if(instance == null)
			instance = new Connection();
		return instance;
	}
	
	/** Closes the connection */
	private void dispose(Socket socket, DataInputStream in, DataOutputStream out) throws IOException {
		if(socket != null)
            socket.close();

		if(in != null)
            in.close();

		if(out != null)
            out.close();
	}

    /**
     * Will try and connect to the authenticator with the passed ips.
     * A connection will be considered valid if a valid pong message is received.
     * Will return a socket <b>without</b> timeout
     *
     * @param ips
     * @return
     * @throws CannotConnectToWalletException
     */
	public Socket generateSockeToAuthenticator(Socket s, String[] ips) throws CannotConnectToWalletException {
		for(String ip:ips)
			try {
				Log.i("asdf", "Trying to connect to: " + ip);
				
				InetAddress walletAddr = InetAddress.getByName(ip);
                if(s == null)
				    s = new Socket();
				s.connect(new InetSocketAddress(walletAddr, PORT), 1000);
				s.setSoTimeout(0);
				
				// verify we are connected to an authenticator
                if(!PongPayload.isValidPongPayload(this.readContinuous(s))) {
                    try { s.close(); }
                    catch(IOException e) { e.printStackTrace(); }
                    s = null;

                    throw new CannotConnectToWalletException("Returned pong message is not valid");
                }
				
				Log.i("asdf", "Connected to: " + ip);
				
				return s;		
			}
			catch(Exception e) { e.printStackTrace(); }
		
		if(s == null)
			throw new CannotConnectToWalletException("Could Not Connect to wallet");
		return s;
	}

	/*
	 * API
	 * 
	 */
	
	/**
	 * <b>WARNING</b><br>
	 * This method doesn't close the connection, make sure you close it.
	 * 
	 * @param ip
	 * @param payload
	 * @return Socket
	 * @throws CannotConnectToWalletException
	 */
	public Socket writeContinuous(String ip, byte[] payload) throws CannotConnectToWalletException {
		return writeContinuous(new String[]{ ip }, payload);
	}
	public Socket writeContinuous(String[] ips, byte[] payload) throws CannotConnectToWalletException {
		Socket s = null;
        s = generateSockeToAuthenticator(s, ips);
        return  writeContinuous(s, payload);
	}

    public Socket writeContinuous(Socket s, byte[] payload) throws CannotConnectToWalletException {
        try {
            write(s, payload);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CannotConnectToWalletException("Couldn't connect to wallet");
        }

        return s;
    }
	
	public void writeAndClose(String ip, byte[] payload) throws CannotConnectToWalletException {
		writeAndClose(new String[]{ ip }, payload);
	}
	public void writeAndClose(String[] ips, byte[] payload) throws CannotConnectToWalletException {
        try {
            Socket s = writeContinuous(ips, payload);
            this.dispose(s, null, null);
        }
        catch(Exception e) {
            throw new CannotConnectToWalletException(e.getMessage());
        }
	}
	public void writeAndClose(Socket s, byte[] payload) throws CannotConnectToWalletException {
		try {
			write(s, payload);
			
			this.dispose(s, null, null);
		} catch (IOException e) { 
		
			throw new CannotConnectToWalletException("Cannot write to wallet");
		}
		
	}
	
	private void write(Socket s, byte[] payload) throws IOException {
		DataOutputStream out = null;
		out = new DataOutputStream(s.getOutputStream());
		out.writeInt(payload.length);
		out.write(payload);
	}
	
	public byte[] readContinuous(Socket s) throws CannotReadFromWalletException {
		try {
			byte[] ret = null;
			read(s, ret);
						
			return ret;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new CannotReadFromWalletException("Couldn't read from wallet");
		}
	}
	
	public byte[] readAndClose(Socket s) throws CannotReadFromWalletException {
		try {
			return readContinuous(s);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new CannotReadFromWalletException("Couldn't read from wallet");
		}
	}
	
	private byte[] read(Socket s, byte[] readBytes) throws IOException {
		int previousTimeout = s.getSoTimeout();
		try {
			s.setSoTimeout(3000);
			
			DataInputStream in = new DataInputStream(s.getInputStream());
			int size = in.readInt();
            readBytes = new byte[size];
			in.read(readBytes);

			return readBytes;
		}
		catch (IOException e) {
			throw new IOException(e.toString());
		}
		finally {
			s.setSoTimeout(previousTimeout);
		}
	}
	
	public static class CannotConnectToWalletException extends Exception {
		public CannotConnectToWalletException(String str) {
			super (str);
		}
	}
	
	public static class CannotReadFromWalletException extends Exception {
		public CannotReadFromWalletException(String str) {
			super (str);
		}
	}
}
