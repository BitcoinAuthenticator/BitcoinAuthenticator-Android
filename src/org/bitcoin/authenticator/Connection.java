package org.bitcoin.authenticator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**	
 * This class creates a new TCP socket connection to the wallet as well as the data stream input/output objects.
 */
public class Connection {
	
	public static final int PORT = 1234;
    public Socket SOCKET;
    public DataInputStream IN;
    public DataOutputStream OUT;
    
    /**	Takes in the wallet IP address as a string and connects to it.*/
	public Connection(String IP) throws IOException{
		InetAddress walletAddr = InetAddress.getByName(IP);
		SOCKET = new Socket();//(walletAddr, PORT);
		SOCKET.connect(new InetSocketAddress(walletAddr, PORT), 300);
		IN = new DataInputStream(SOCKET.getInputStream());
		OUT = new DataOutputStream(SOCKET.getOutputStream());
	}
	
	/**	Returns the open socket*/
	public Socket getSocket(){
		return SOCKET;
	}
	
	/** Closes the connection */
	public void close() throws IOException{
		SOCKET.close();
		IN.close();
		OUT.close();
	}
	
	
	/**	Returns the DataInputStream object for the open connection*/
	public DataInputStream getInputStream() throws IOException {
		return IN;
	}
	
	/**	Returns the DataOutputStream object for the open connection*/
	public DataOutputStream getOutputStream() throws IOException {
		return OUT;
	}
	
}
