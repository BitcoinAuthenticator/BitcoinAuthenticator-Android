package org.bitcoin.authenticator;



import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.*;

import org.bitcoin.authenticator.GcmUtil.GcmUtilGlobal;
import org.bitcoin.authenticator.net.Connection;
import org.bitcoin.authenticator.net.Connection.CannotConnectToWalletException;
import org.bitcoin.authenticator.utils.EncodingUtils;
import org.json.simple.JSONValue;

import android.util.Log;

import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import com.google.common.primitives.Ints;
 
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
	public void run(byte[] seed, SecretKey AESKey, byte[] regID, long walletIndex) throws CouldNotPairToWalletException {
    	try {
    		//Derive the key and chaincode from the seed.
    		Log.i("asdf", "Pairing: Deriving Wallet Account");
        	HDKeyDerivation HDKey = null;
        	DeterministicKey masterkey = HDKey.createMasterPrivateKey(seed);
        	DeterministicKey childkey = HDKey.deriveChildKey(masterkey,Ints.checkedCast(walletIndex));
        	byte[] chaincode = childkey.getChainCode(); // 32 bytes
        	byte[] mpubkey = childkey.getPubKey(); // 32 bytes
        	
        	//Format data into a JSON object
        	Log.i("asdf", "Pairing: creating payload");
        	Map obj=new LinkedHashMap();
        	obj.put("version", 1);
    		obj.put("mpubkey", Utils.bytesToHex(mpubkey));
    		obj.put("chaincode", Utils.bytesToHex(chaincode));
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
    		throw new CouldNotPairToWalletException("Could not pair to wallet: " + e.getMessage());
    	}
    	
	  }
    
    public static class PairingQRData {
    	public String AESKey;
    	public String IPAddress;
    	public String LocalIP;
    	public String walletType;
    	public String pairingName;
    	public int networkType;
    	public long walletIndex;
    }
    
    /**
     * Takes payload parsed from the QR image andreturns a {@link org.bitcoin.authenticator.PairingProtocol.PairingQRData PairingQRData} object
     * 
     * @param QRInput
     * @return
     */
    public static PairingQRData parseQRString(String QRInput) {
    	if(!checkQRDataValidity(QRInput))	
    			return null;
    	
    	PairingQRData ret = new PairingQRData();
    	
    	ret.AESKey = QRInput.substring(QRInput.indexOf("AESKey=")+7, QRInput.indexOf("&PublicIP="));
    	ret.IPAddress = QRInput.substring(QRInput.indexOf("&PublicIP=")+10, QRInput.indexOf("&LocalIP="));
    	ret.LocalIP = QRInput.substring(QRInput.indexOf("&LocalIP=")+9, QRInput.indexOf("&pairingName="));
    	ret.pairingName = QRInput.substring(QRInput.indexOf("&pairingName=")+13, QRInput.indexOf("&WalletType="));
    	ret.walletType = QRInput.substring(QRInput.indexOf("&WalletType=")+12, QRInput.indexOf("&NetworkType="));
		/**
		 * 1 for main net, 0 for testnet
		 */
    	ret.networkType = Integer.parseInt(QRInput.substring(QRInput.indexOf("&NetworkType=")+13, QRInput.indexOf("&index=")));
		
		/**
		 * get index
		 */
		String walletIndexHex = QRInput.substring(QRInput.indexOf("&index=")+7, QRInput.length());
		ret.walletIndex = getWalletIndexFromString(walletIndexHex);
    	
    	return ret;
    }
    
    public static long getWalletIndexFromString(String hex) {
    	return new BigInteger(EncodingUtils.hexStringToByteArray(hex)).longValue();
    }
    
    /**
     * Checks to see all necessary data elements are present
     * 
     * @param data
     * @return
     */
    public static boolean checkQRDataValidity(String data) {
    	if(!data.contains("AESKey"))
    		return false;
    	
    	if(!data.contains("PublicIP"))
    		return false;
    	
    	if(!data.contains("LocalIP"))
    		return false;
    	
    	if(!data.contains("WalletType"))
    		return false;
    	
    	if(!data.contains("NetworkType"))
    		return false;
    	
    	if(!data.contains("index"))
    		return false;
    	
    	return true;
    }
    
    static public class CouldNotPairToWalletException extends Exception {
		public CouldNotPairToWalletException(String str) {
			super(str);
		}
	}
   
}