package org.bitcoin.authenticator.net;

import java.io.ByteArrayOutputStream;
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

import org.bitcoin.authenticator.TxData;
import org.bitcoin.authenticator.Utils;
import org.bitcoin.authenticator.net.exceptions.CouldNotGetTransactionException;
import org.bitcoin.authenticator.net.exceptions.CouldNotSendEncryptedException;
import org.bitcoin.authenticator.net.exceptions.CouldNotSendRequestIDException;
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
		setIPs(ips);
	}
	
	@SuppressWarnings("unchecked")
	public Socket sendRequestID(String requestID, String walletID) throws CouldNotSendRequestIDException {
		try {
			JSONObject jo = new JSONObject();
			jo.put("requestID", requestID);
			jo.put("pairingID", walletID); // the walletID in the authenticator is the pairing id in the wallet
			byte[] payload = jo.toString().getBytes();
			return Connection.getInstance().writeContinuous(getIPs(), payload);
		}
		catch(Exception e) {
            e.printStackTrace();
			throw new CouldNotSendRequestIDException("Couldn't send request ID to wallet");
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
			String response = CannotProcessRequestPayload.isCannotBeProcessedPayload(testpayload);
			if(response != null)
				throw new CouldNotGetTransactionException(response);
			
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
				throw new CouldNotGetTransactionException("Couldn't get transaction from wallet");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new CouldNotGetTransactionException(e.getMessage());
		}
		
	}

    /**
     * Method to send the transaction signature back to the wallet.
     * It calculates the HMAC of the signature, concatenates it, and encrypts it with AES.
     *
     * @param sig
     * @param sharedsecret
     * @param s
     * @throws CouldNotSendEncryptedException
     */
	public void sendEncrypted (byte[] sig, SecretKey sharedsecret, Socket s) throws CouldNotSendEncryptedException{
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
			Connection.getInstance().writeAndClose(s, cipherBytes);
		}
		catch(Exception e) {
			throw new CouldNotSendEncryptedException("Couldn't send encrypted payload");
		}
		
    }

    public void setIPs(String[] ips) {
        this.ips = ips;
    }

    public String[] getIPs() {
        return this.ips;
    }
}
