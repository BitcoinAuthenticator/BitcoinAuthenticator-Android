import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This the main class for the test wallet. You can execute commands from the command line. 
 * At the moment you can't do multi-in/multi-out transactions.
 */
public class Main {
	public static int childkeyindex;

	/**Launches the application*/
	public static void main(String[] args) throws Exception {
		childkeyindex = 0;
		Date dNow = new Date(0);
		SimpleDateFormat ft =  new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
		System.out.println("Testwallet v. 0.01 (" + ft.format(dNow) + ")");
		System.out.println("Type 'help()' for a list of commands");
		inputCommand();
	}

	/**Lets user enter commands and executes them*/
	@SuppressWarnings("static-access")
	static void inputCommand() throws Exception {
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		  while (true){
			  String s = "";
			  System.out.print(">>> ");
			  s = in.nextLine().toLowerCase();
			  String u = s;
			  String command = s.substring(0,s.indexOf("("));
			  //List of supported commands
			  ArrayList<String> v = new ArrayList<String>();
			  v.add("pairwallet");  
			  v.add("send");  
			  v.add("newaddress");   
			  v.add("mktx");  
			  v.add("openport");    
			  v.add("help");  
			  //Switch for executing the commands
			  WalletOperation op = new WalletOperation();
			  switch (v.indexOf(command)){
			  case 0:
				  String walletType = s.substring(s.indexOf("(")+1, s.indexOf(")"));
				  PairingProtocol pair = new PairingProtocol();
				  pair.main(walletType);
				  break;
			  case 1:
				  op.sendTX();
				  break;
			  case 2:
				  op.childPubKey();
				  break;
			  case 3:
				  System.out.print("From: ");
				  String from = in.nextLine();
				  System.out.print("To: ");
				  String to = in.nextLine();
				  System.out.print("Amount: ");
				  String amount = in.nextLine();
				  op.mktx(amount, from, to);  
				  break;
			  case 4:
				  OpenPort.main(null);
				  break;
			  case 5:
				  System.out.println("Help menu to be implemented later");
				  break;
			  }
		   }
	}
	
}
