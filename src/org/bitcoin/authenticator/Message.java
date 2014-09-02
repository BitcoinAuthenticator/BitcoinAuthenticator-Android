package org.bitcoin.authenticator;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bitcoin.authenticator.net.CannotProcessRequestPayload;
import org.json.simple.JSONObject;

/**
 * This class handles the communication messages sent between the Authenticator and the wallet.
 */
public class Message {
	
	private String[] ips;
	
	/**Constructor takes in active Connection object to the wallet*/
	public Message(String[] ips){
		if (ips == null || ips.length == 0)
			throw new IllegalArgumentException("No ips were provided");
		this.ips = ips;
	}
	
	@SuppressWarnings("unchecked")
	public Socket sentRequestID(String requestID) throws CouldNotSendRequestIDException{
		try {
			JSONObject jo = new JSONObject();
			jo.put("requestID", requestID);
			byte[] payload = jo.toString().getBytes();
			return Connection.getInstance().writeContinuous(ips, payload);
		}
		catch(Exception e) {
			throw new CouldNotSendRequestIDException("Couldn't send request ID to wallet");
		}
	}
	
	static public class CouldNotSendRequestIDException extends Exception {
		public CouldNotSendRequestIDException(String str) {
			super(str);
		}
	}
	
	/**
	 * Method to receive a transaction from the wallet. It decrypts the message and verifies the HMAC.
	 * Returns a TxData object containing the number of inputs, child key indexes, public keys from the wallet, and 
	 * raw unsigned transaction.
	 */
	public TxData receiveTX(SecretKey sharedsecret, Socket s) throws CouldNotGetTransactionException {
		try {
			//Receive the encrypted payload
		  	byte[] cipherBytes;
			cipherBytes = Connection.getInstance().readContinuous(s);
			//Decrypt the payload
		  	Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
		    cipher.init(Cipher.DECRYPT_MODE, sharedsecret);
		    //Split the payload into it's parts.
		    String payload = Utils.bytesToHex(cipher.doFinal(cipherBytes));
			byte[] testpayload = Utils.hexStringToByteArray(payload.substring(0,payload.length()-64));
			byte[] hash = Utils.hexStringToByteArray(payload.substring(payload.length()-64,payload.length()));
			
			// in case wallet couldn't process request
			if(CannotProcessRequestPayload.isCannotBeProcessedPayload(testpayload))
				return null;
			
		    TxData data = new TxData(testpayload);
		    //Verify the HMAC
		    Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(sharedsecret);
			byte[] macbytes = mac.doFinal(testpayload);
			if (Arrays.equals(macbytes, hash)){
				//Return the payload
				return data;
			}
			else {
				System.out.println("Message authentication code is invalid");
				return null;
			}
		}
		catch (Exception e) {
			throw new CouldNotGetTransactionException("Couldn't get transaction from wallet");
		}
		
	}
	
	static public class CouldNotGetTransactionException extends Exception {
		public CouldNotGetTransactionException(String str) {
			super(str);
		}
	}

	/**
	 * Method to send the transaction signature back to the wallet.
	 * It calculates the HMAC of the signature, concatenates it, and encrypts it with AES.
	 * 
	 * @param sig
	 * @param sharedsecret
	 * @param ip
	 * @throws CouldNotSendEncryptedException
	 */
	public void sendEncrypted (byte[] sig, SecretKey sharedsecret, String[] ips) throws CouldNotSendEncryptedException{
		try {
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
			try {cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");} 
			catch (NoSuchAlgorithmException e) {e.printStackTrace();} 
			catch (NoSuchPaddingException e) {e.printStackTrace();}
	        try {cipher.init(Cipher.ENCRYPT_MODE, sharedsecret);} 
	        catch (InvalidKeyException e) {e.printStackTrace();}
	        byte[] cipherBytes = null;
			try {cipherBytes = cipher.doFinal(payload);} 
			catch (IllegalBlockSizeException e) {e.printStackTrace();} 
			catch (BadPaddingException e) {e.printStackTrace();}
			//Send the payload over to the wallet
			Connection.getInstance().writeAndClose(ips, cipherBytes);
		}
		catch(Exception e) {
			throw new CouldNotSendEncryptedException("Couldn't send encrypted payload");
		}
		
    }
	
	static public class CouldNotSendEncryptedException extends Exception {
		public CouldNotSendEncryptedException(String str) {
			super(str);
		}
	}
	
}
