package org.bitcoin.authenticator;



import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.*;

import org.bitcoin.authenticator.Connection.CannotConnectToWalletException;
import org.bitcoin.authenticator.GcmUtil.GcmUtilGlobal;
import org.json.simple.JSONValue;

import android.util.Log;

import com.google.bitcoin.crypto.DeterministicKey;
import com.google.bitcoin.crypto.HDKeyDerivation;
 
/**
 *	Opens a TCP socket connection to the wallet, derives a new master public key, encrypts it
 *  and sends it over to the wallet.
 */
public class PairingProtocol {
 
    private String[] ips;
    
    /**	Constructor creates a new connection object to connect to the wallet. */
    public PairingProtocol(String[] ips) {
    	this.ips = ips;
    }
    
    /**	
     * Takes in Authenticator seed and uses it to derive the master public key and chaincode.
     * Uses the AES key to calculate the message authentication code for the payload and concatenates 
     * it with the master public key and chaincode. The payload is encrypted and sent over to the wallet.
     * @throws CouldNotPairToWalletException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes", "static-access" })
	public void run(byte[] seed, SecretKey AESKey, String pairingID, byte[] regID, int num) throws CouldNotPairToWalletException {
    	try {
    		//Derive the key and chaincode from the seed.
    		Log.i("asdf", "Pairing: Deriving Wallet Account");
        	HDKeyDerivation HDKey = null;
        	DeterministicKey masterkey = HDKey.createMasterPrivateKey(seed);
        	DeterministicKey childkey = HDKey.deriveChildKey(masterkey,num);
        	byte[] chaincode = childkey.getChainCode(); // 32 bytes
        	byte[] mpubkey = childkey.getPubKey(); // 32 bytes
        	
        	//Format data into a JSON object
        	Log.i("asdf", "Pairing: creating payload");
        	Map obj=new LinkedHashMap();
        	obj.put("version", 1);
    		obj.put("mpubkey", Utils.bytesToHex(mpubkey));
    		obj.put("chaincode", Utils.bytesToHex(chaincode));
    		obj.put("pairID", pairingID);
    		obj.put("gcmID", new String (regID));
    		StringWriter jsonOut = new StringWriter();
    		try {JSONValue.writeJSONString(obj, jsonOut);} 
    		catch (IOException e1) {e1.printStackTrace();}
    		String jsonText = jsonOut.toString();
    		byte[] jsonBytes = jsonText.getBytes();
    		
        	//Calculate the HMAC
    		Log.i("asdf", "Pairing: calculating checksum");
        	Mac mac = Mac.getInstance("HmacSHA256");
        	mac.init(AESKey);
        	byte[] macbytes = mac.doFinal(jsonBytes);
        	
        	//Concatenate with the JSON object
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        	outputStream.write(jsonBytes);
        	outputStream.write(macbytes);
        	byte payload[] = outputStream.toByteArray();
        	
        	//Encrypt the payload
        	Log.i("asdf", "Pairing: encrypting payload");
        	Cipher cipher = null;
        	try {cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");} 
        	catch (NoSuchAlgorithmException e) {e.printStackTrace();} 
        	catch (NoSuchPaddingException e) {e.printStackTrace();}
        	try {cipher.init(Cipher.ENCRYPT_MODE, AESKey);} 
        	catch (InvalidKeyException e) {e.printStackTrace();}
        	byte[] cipherBytes = null;
        	try {cipherBytes = cipher.doFinal(payload);} 
        	catch (IllegalBlockSizeException e) {e.printStackTrace();} 
        	catch (BadPaddingException e) {e.printStackTrace();}
        	
        	//Send the payload over to the wallet
        	Connection.getInstance().writeAndClose(ips, cipherBytes);
    	}
    	catch (Exception e) {
    		throw new CouldNotPairToWalletException("Could not pair to wallet");
    	}
    	
	  }
    
    static public class CouldNotPairToWalletException extends Exception {
		public CouldNotPairToWalletException(String str) {
			super(str);
		}
	}
	
}