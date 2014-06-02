package org.bitcoin.authenticator;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

<<<<<<< HEAD
import android.content.SharedPreferences;
=======
import org.json.simple.JSONObject;
>>>>>>> multi_gcm

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
	
	@SuppressWarnings("unchecked")
	public void sentRequestID(String requestID) throws IOException{
		JSONObject jo = new JSONObject();
		jo.put("requestID", requestID);
		byte[] payload = jo.toString().getBytes();
		out.writeInt(payload.length);
		out.write(payload);
	}
	
	/**
	 * Method to receive a transaction from the wallet. It decrypts the message and verifies the HMAC.
	 * Returns a TxData object containing the number of inputs, child key indexes, public keys from the wallet, and 
	 * raw unsigned transaction.
	 */
	public TxData receiveTX(ArrayList<SecretKey> sharedsecret) throws Exception {
		//Receive the encrypted payload
		System.out.println("x3");
	  	byte[] cipherBytes;
	  	int size = in.readInt();
		cipherBytes = new byte[size];
		in.read(cipherBytes);
		TxData data = null;
		//Multiple wallets may use the same IP address. If GCM is off we need to attempt to decrypt the payload
		//with each AES key paired to that IP address.
		for (int a=0; a<sharedsecret.size(); a++){
			System.out.println("x4");
			//Decrypt the payload
			String payload = null;
			boolean decrypted = true;
			try {
				Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
				cipher.init(Cipher.DECRYPT_MODE, sharedsecret.get(a));
				//Split the payload into it's parts.
				payload = Utils.bytesToHex(cipher.doFinal(cipherBytes));
			} catch (BadPaddingException e) {
				System.out.println("x4.5");
				decrypted = false;
				}
			byte[] testpayload = null;
			byte[] hash = null;
			if (decrypted){
				testpayload = Utils.hexStringToByteArray(payload.substring(0,payload.length()-64));
				hash = Utils.hexStringToByteArray(payload.substring(payload.length()-64,payload.length()));
				//Verify the HMAC
				Mac mac = Mac.getInstance("HmacSHA256");
				mac.init(sharedsecret.get(a));
				byte[] macbytes = mac.doFinal(testpayload);
				if (Arrays.equals(macbytes, hash)){
					//Return the payload
					data = new TxData(testpayload);
					System.out.println("x5");
					if (!Wallet_list.GCM) {Wallet_list.walletnum = Wallet_list.IndexArr.get(a);}
				}
				else {
					System.out.println("Message authentication code is invalid");
				}
			}
		}
		return data;
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
	  	out.writeInt(cipherBytes.length);
		out.write(cipherBytes);
    }
	
}
