import java.net.ServerSocket;
import java.net.Socket;

/**
 * Small class to open a new port. The pairing protocol opens a connection to the wallet. But if it's alread 
 * paired you can use this to reconnect. 
 */
public class OpenPort {
	
	public static Socket socket;
	
	public static void main(String[] args) throws Exception {
		final int port = 1234;
		UpNp plugnplay = new UpNp();
	    plugnplay.main(null);
	    System.out.println("Listening for Alice on port "+port+"...");
	    ServerSocket ss = new ServerSocket (port);
	    socket = ss.accept();
	    System.out.println("Connected to Alice.");
	    
	    //return to main
	    Main.inputCommand();
	}

}
