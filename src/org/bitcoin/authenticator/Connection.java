package org.bitcoin.authenticator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.bitcoin.authenticator.net.PongPayload;

import android.util.Log;

/**	
 * This class creates a new TCP socket connection to the wallet as well as the data stream input/output objects.
 */
public class Connection {
	
	public static final int PORT = 1234;
	
	private static Connection instance;
	public static Connection getInstance() {
		if(instance == null)
			instance = new Connection();
		return instance;
	}
	
	/** Closes the connection */
	private void dispose(Socket socket, DataInputStream in, DataOutputStream out) {
		if(socket != null)
		try { socket.close(); } catch(IOException e){ };
		
		if(in != null)
		try { in.close(); } catch(IOException e){ };
		
		if(out != null)
		try { out.close(); } catch(IOException e){ };
	}
	
	/**
	 * Will check if a connection is possible, if so will return a socket <b>without</b> timeout
	 * 
	 * @param ip
	 * @return
	 * @throws CannotConnectToWalletException
	 */
	public Socket generateSocket(String[] ips) throws CannotConnectToWalletException {
		Socket s = null;
		for(String ip:ips)
			try {
				Log.i("asdf", "Trying to connect to: " + ip);
				
				InetAddress walletAddr = InetAddress.getByName(ip);
				s = new Socket();
				s.connect(new InetSocketAddress(walletAddr, PORT), 1000);
				s.setSoTimeout(0);
				
				// verify we are connected to an authenticator
				try {
					if(!PongPayload.isValidPongPayload(this.readContinuous(s))) {
						s.close();
						throw new CannotConnectToWalletException("");
					}
				} catch (Exception e) {
					throw new Exception("");
				}
				
				Log.i("asdf", "Connected to: " + ip);
				
				return s;		
			}
			catch(Exception e) { }
		
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
		Socket s = generateSocket(ips);
		
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
		Socket s = writeContinuous(ips, payload);
		this.dispose(s, null, null);
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
		
//		if(out != null)
//			try {
//				out.close();
//			} catch (IOException e) { e.printStackTrace(); }
	}
	
	public byte[] readContinuous(Socket s) throws CannotReadFromWalletException {
		try {
			byte[] ret;
			ret = read(s);
						
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
		finally {
//			try {
//				s.close();
//			} catch (IOException e) { }
		}
	}
	
	private byte[] read(Socket s) throws IOException {
		int previousTimeout = s.getSoTimeout();
		try {
			s.setSoTimeout(3000);
			
			DataInputStream in = new DataInputStream(s.getInputStream());
			int size = in.readInt();
			byte[] payload = new byte[size];
			in.read(payload);
			
//			if(in != null)
//				try {
//					in.close();
//				} catch (IOException e) { e.printStackTrace(); }
			
			return payload;
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
