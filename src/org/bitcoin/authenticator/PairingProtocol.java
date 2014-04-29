package org.bitcoin.authenticator;



import java.io.*;
import java.security.*;

import javax.crypto.*;

import com.google.bitcoin.crypto.DeterministicKey;
import com.google.bitcoin.crypto.HDKeyDerivation;
 
/**
 *	Opens a TCP socket connection to the wallet, derives a new master public key, encrypts it
 *  and sends it over to the wallet.
 */
public class PairingProtocol {
 
    static DataOutputStream out;
    static DataInputStream in;
    public static Connection conn;
    
    /**	Constructor creates a new connection object to connect to the wallet. */
    public PairingProtocol(String IP) throws IOException {
    	conn = new Connection(IP);
    	out = conn.getOutputStream();
    	in = conn.getInputStream();
    }
    
    /**	
     * Takes in Authenticator seed and uses it to derive the master public key and chaincode.
     * Uses the AES key to calculate the message authentication code for the payload and concatenates 
     * it with the master public key and chaincode. The payload is encrypted and sent over to the wallet.
     */
    public void run(byte[] seed, SecretKey AESKey) throws IOException, NoSuchAlgorithmException, InvalidKeyException  {
    	//Derive the key and chaincode from the seed.
    			int num = 1;
    	  		HDKeyDerivation HDKey = null;
    	  		DeterministicKey masterkey = HDKey.createMasterPrivateKey(seed);
    			DeterministicKey childkey = HDKey.deriveChildKey(masterkey,num);
    	  		byte[] chaincode = childkey.getChainCode();
    	  		byte[] mpubkey = childkey.getPubKeyBytes();
    	  		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
    			outputStream.write(mpubkey);
    			outputStream.write(chaincode);
    			byte[] keychaincode = outputStream.toByteArray();
    	  		//Calculate the HMAC
    	  		Mac mac = Mac.getInstance("HmacSHA256");
    			mac.init(AESKey);
    			byte[] macbytes = mac.doFinal(keychaincode);
    			//Concatenate with the Key and Chaincode
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
    					cipher.init(Cipher.ENCRYPT_MODE, AESKey);
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
    	            try {
    					out.writeInt(cipherBytes.length);
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    	  	        try {
    					out.write(cipherBytes);
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
	  }
	  
	}