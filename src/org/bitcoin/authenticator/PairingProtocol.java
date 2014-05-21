package org.bitcoin.authenticator;



import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.*;

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
    public void run(byte[] seed, SecretKey AESKey, String pairingID) throws IOException, NoSuchAlgorithmException, InvalidKeyException  {
    	//Derive the key and chaincode from the seed.
    	int num = 1;
    	HDKeyDerivation HDKey = null;
    	DeterministicKey masterkey = HDKey.createMasterPrivateKey(seed);
    	DeterministicKey childkey = HDKey.deriveChildKey(masterkey,num);
    	byte[] chaincode = childkey.getChainCode(); // 32 bytes
    	byte[] mpubkey = childkey.getPubKey(); // 32 bytes
    	byte[] pairID = pairingID.getBytes();//ByteBuffer.allocate(4).putInt(pairingID).array(); // 4bytes
    	byte[] regID = GcmUtilGlobal.gcmRegistrationToken.getBytes();
    	//Format data into a JSON object
    	Map obj=new LinkedHashMap();
    	obj.put("version", 1);
		obj.put("mpubkey", Utils.bytesToHex(mpubkey));
		obj.put("chaincode", Utils.bytesToHex(chaincode));
		obj.put("pairID", new String (pairID));
		obj.put("gcmID", new String (regID));
		StringWriter jsonOut = new StringWriter();
		try {JSONValue.writeJSONString(obj, jsonOut);} 
		catch (IOException e1) {e1.printStackTrace();}
		String jsonText = jsonOut.toString();
		byte[] jsonBytes = jsonText.getBytes();
    	//Calculate the HMAC
    	Mac mac = Mac.getInstance("HmacSHA256");
    	mac.init(AESKey);
    	byte[] macbytes = mac.doFinal(jsonBytes);
    	//Concatenate with the JSON object
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
    	outputStream.write(jsonBytes);
    	outputStream.write(macbytes);

    	byte payload[] = outputStream.toByteArray();
    	Log.v("ASDF","payload length byte[] - " + payload.length);
    	//Encrypt the payload
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
    	try {out.writeInt(cipherBytes.length);} 
    	catch (IOException e) {e.printStackTrace();}
    	try {out.write(cipherBytes);} 
    	catch (IOException e) {e.printStackTrace();}
    	conn.close();
	  }
	  
}