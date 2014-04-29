package org.bitcoin.authenticator;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * This class handles the communication messages sent between the Authenticator and the wallet.
 */
public class Message {
	
	static DataInputStream in;
	static DataOutputStream out ;
	
	/**Constructor takes in active Connection object to the wallet*/
	public Message(Connection conn) throws IOException{
		in = conn.getInputStream();
		out = conn.getOutputStream();
	}
	
	/**
	 * Method to receive a transaction from the wallet. It decrypts the message and verifies the HMAC.
	 * Returns a string containing the child key index, public key from the wallet, and raw unsighed transaction.
	 */
	public String receiveTX(SecretKey sharedsecret) throws Exception {
		//Recieve the encrypted payload
	  	byte[] cipherBytes;
	  	int size = in.readInt();
		cipherBytes = new byte[size];
		in.read(cipherBytes);
		//Decrypt the payload
	  	Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
	    cipher.init(Cipher.DECRYPT_MODE, sharedsecret);
	    //Split the payload into it's parts.
	    String message = Utils.bytesToHex(cipher.doFinal(cipherBytes));
	    String payload = message.substring(0,message.length()-64);
	    String version = message.substring(0, 2);
	    String childkeyindex = message.substring(2,10);
	    String pubkey = message.substring(10,76);
	    String transaction = message.substring(76, message.length()-64);
	    String HMAC = message.substring(message.length()-64,message.length());
	    //Verify the HMAC
	    Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(sharedsecret);
	    byte[] testmsg = Utils.hexStringToByteArray(payload);
	    byte[] hash = Utils.hexStringToByteArray(HMAC);
		byte[] macbytes = mac.doFinal(testmsg);
		if (Arrays.equals(macbytes, hash)){
			//Return the payload as a string
			return (childkeyindex + pubkey + transaction);
		}
		else {
			System.out.println("Message authentication code is invalid");
			return ("error");
		}
	}
	
	/**
	 * Method to send the transaction signature back to the wallet.
	 * It calculates the HMAC of the signature, concatentates it, and encypts it with AES.
	 */
	public void sendSig (byte[] sig, SecretKey sharedsecret) throws IOException, InvalidKeyException, NoSuchAlgorithmException{
		//Calculate the HMAC
    	Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(sharedsecret);
		byte[] macbytes = mac.doFinal(sig);
		//Concatenate it with the signature
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write(sig);
		outputStream.write(macbytes);
		byte payload[] = outputStream.toByteArray();
  		//Encrypt the payload
  	    Cipher cipher = null;
			try {
				cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			}
          try {
				cipher.init(Cipher.ENCRYPT_MODE, sharedsecret);
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			}
          byte[] cipherBytes = null;
			try {
				cipherBytes = cipher.doFinal(payload);
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			}
			//Send the payload over to the wallet
	  		out.writeInt(cipherBytes.length);
			out.write(cipherBytes);
    }
	
}
