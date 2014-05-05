<<<<<<< HEAD:testwallet/src/Main.java
import java.io.File;
import java.nio.ByteBuffer;
=======
package wallet;

<<<<<<< HEAD
>>>>>>> reordering and dipacher:testwallet/src/wallet/Main.java
=======
import java.io.DataInputStream;
import java.io.DataOutputStream;
>>>>>>> fixed gcm sending and multiple notification problem
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import dispacher.Device;
import dispacher.Dispacher;
import dispacher.MessageBuilder;
import dispacher.MessageType;

import GCM.GCMSender;

/**
 * This the main class for the test wallet. You can execute commands from the command line. 
 * At the moment you can't do multi-in/multi-out transactions.
 */
public class Main {

	/**Launches the application*/
	public static void main(String[] args) throws Exception {
		Date dNow = new Date(0);
		SimpleDateFormat ft =  new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
		System.out.println("Testwallet v. 0.01 (" + ft.format(dNow) + ")");
		//Gets the balance of the wallet if a wallet file exists
		String filePath = new java.io.File( "." ).getCanonicalPath() + "/wallet.json";
		File f = new File(filePath);
		long balance = 0;
		if(f.exists() && !f.isDirectory()) {
			WalletOperation wallet = new WalletOperation();
			WalletFile file2 = new WalletFile();
			if (file2.getKeyNum()!=0){
			ArrayList<String> addrs2 = file2.getAddresses();
			for (int i=0; i<addrs2.size(); i++){
				addrs2.get(i);
			}
			balance = wallet.getBalance(addrs2);
			}
			System.out.println("Wallet loaded successfully. Balance = " + balance + " Satoshi (confirmed)");
		}
		else {
			System.out.println("No wallet file exists. Pair a new wallet.");
		}
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
			  ArrayList<String> cmd = new ArrayList<String>();
			  cmd.add("pairwallet");  
			  cmd.add("send");  
			  cmd.add("newaddress");   
			  cmd.add("mktx");  
<<<<<<< HEAD
			  cmd.add("openport");  
			  cmd.add("listaddresses");
			  cmd.add("getbalance");
=======
			  cmd.add("openport");   
<<<<<<< HEAD:testwallet/src/Main.java
			  cmd.add("sendtest");
>>>>>>> added gcm send test
=======
			  cmd.add("testgcm");
>>>>>>> reordering and dipacher:testwallet/src/wallet/Main.java
			  cmd.add("help");  
			  //Switch for executing the commands
			  WalletOperation op = new WalletOperation();
			  switch (cmd.indexOf(command)){
			  // pairwallet
			  case 0:
				  String walletType = s.substring(s.indexOf("(")+1, s.indexOf(")"));
				  PairingProtocol pair = new PairingProtocol();
				  pair.run(walletType);
				  break;
			  // send
			  case 1:
				  op.sendTX();
				  break;
			  // newaddress
			  case 2:
				  String addr = op.genAddress();
				  System.out.println(addr);
				  break;
			  // mktx
			  case 3:
				  ArrayList<String> to = new ArrayList<String>();
				  ArrayList<String> amount = new ArrayList<String>();
				  String output = "";
				  boolean stop = false;
				  while (!stop) {
					  System.out.print("Output Address: ");
					  to.add(in.nextLine());
					  System.out.print("Amount: ");
					  amount.add(in.nextLine());
					  System.out.print("Add another output? (y/n): ");
					  String response = in.nextLine().toLowerCase();
					  if (response.equals("n")){stop = true;}
				  }
				  op.mktx(amount, to);  
				  break;
			  // openport
			  case 4:
				  OpenPort.main(null);
				  break;
			  // testgcm
			  case 5:
<<<<<<< HEAD
<<<<<<< HEAD
				  WalletFile file = new WalletFile();
				  ArrayList<String> addrs = file.getAddresses();
				  for (int i=0; i<addrs.size(); i++){
					  System.out.println(addrs.get(i));
				  }				  
				  break;
			  case 6:
				  WalletFile file2 = new WalletFile();
				  ArrayList<String> addrs2 = file2.getAddresses();
				  for (int i=0; i<addrs2.size(); i++){
					  addrs2.get(i);
				  }
				  long balance = op.getBalance(addrs2);
				  System.out.println(balance + " Satoshi (confirmed)");
				  break;
			  case 7:
				  System.out.println("Usage:");
				  System.out.println("  command(parameter)");
				  System.out.println("");
				  System.out.println("Commands:");
				  System.out.println("  pairwallet(logo)           Displays QR code to scan with the Authenticator. Supported logos include: bitcoincore, electrum, armory, blockchain, multibit, hive, and darkwallet.");
				  System.out.println("  openport()                 If the connection between devices is lost. Run this first, then open the Authenticator to reestablish connection.");
				  System.out.println("  newaddress()               Generates a new multisig address using a key from the wallet and a public key derived from the Authenticator Master Public Key.");            
				  System.out.println("  listaddresses()            Lists all the addresses in the wallet.");
				  System.out.println("  getbalance()               Returns the balance of the wallet in satoshi.");
				  System.out.println("  mktx()                     Builds a new unsigned raw transaction. Inputs are add in cronological order until inputs >= outputs. A fee of .1 mbtc is applied.");
				  System.out.println("  send()                     Sends the raw transaction over to the authenticator for signing.");
=======
				  GCMSender sender = new GCMSender();
=======
				  Dispacher disp = new Dispacher();
				  Device d = new Device(PairingProtocol.chaincode,
						  PairingProtocol.mPubKey,
						  "APA91bEwbGCjr1T-bNkiB1tdERfzOGChTHnjFHA9JizMJNs7D6dinCHUv86MWUOW-IchY3o7nVZwM2s3VCpDmYyWvlVAQ1lo1jnmnbP550uczJSPrYGmBxwlFvfa1poWIxReiIS5Nsm-fPAv2iiaKgT5eacr470RBJE3sxYkFtSi1svVfiJTGig".getBytes(),
						  "-1".getBytes(),
						  PairingProtocol.sharedsecret);
				  disp.dispachMessage(MessageType.signTx, "{data:hello}".getBytes(), d);
				  
				  /*GCMSender sender = new GCMSender();
>>>>>>> fixed gcm sending and multiple notification problem
				  ArrayList<String> devicesList = new ArrayList<String>();
				  devicesList.add("APA91bGr1kYu7L6oKUfyCEhg0ofuGoFYdRbqj1QHBFAMVI_eFkYSp2NU3u01MfQ92jhBUVY4qhCYKO-xERCq3t52yKih671fEkNPHS_YIVfrvuj9PcD8_ETAoKdhHAnWpNZkofbFjOzdD0uMamTOQ0_xIoRymcm8DjeZ5zi6sfXryJ-bykS4nd0");
				  MessageBuilder msg = new MessageBuilder(MessageType.test);
				  sender.sender(devicesList,msg);*/
				  break;
			  // help
			  case 6:
				  System.out.println("Help menu to be implemented later");
>>>>>>> added gcm send test
				  break;
			  }
		   }
	}
	
}
