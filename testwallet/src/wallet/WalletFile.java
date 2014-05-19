package wallet;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.bitcoin.core.Utils;

/**
 * This class manages the saving a loading of keys to and from a .json file.
 */
public class WalletFile {
	
	String filePath = null;

	/**Contructor defines the loclation of the .json file*/
	public WalletFile(){
		try {
			filePath = new java.io.File( "." ).getCanonicalPath() + "/wallet.json";
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Saves the network parameters to the JSON file */
	void writeNetworkParams(Boolean testnet){
		//Load the existing json file
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(filePath));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = (JSONObject) obj;
		String aeskey = (String) jsonObject.get("aes_key");
		String mpubkey = (String) jsonObject.get("master_public_key");
		String chaincode = (String) jsonObject.get("chain_code");
		String gcm = (String) jsonObject.get("GCM");
		long numkeys = (Long) jsonObject.get("keys_n");
		JSONArray msg = (JSONArray) jsonObject.get("keys");
		Iterator<JSONObject> iterator = msg.iterator();
		JSONArray jsonlist = new JSONArray();
		while (iterator.hasNext()) {
			jsonlist.add(iterator.next());
		}		
		//Save the new json file
		Map newobj=new LinkedHashMap();
		newobj.put("aes_key", aeskey);
		newobj.put("master_public_key", mpubkey);
		newobj.put("chain_code", chaincode);
		newobj.put("GCM", gcm);
		newobj.put("testnet", testnet);
		newobj.put("keys_n", numkeys);
		newobj.put("keys", jsonlist);
		StringWriter jsonOut = new StringWriter();
		try {
			JSONValue.writeJSONString(newobj, jsonOut);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String jsonText = jsonOut.toString();
		try {
			FileWriter file = new FileWriter(filePath);
			file.write(jsonText);
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method is used to save a new address and private key to file. It loads the existing .json file,
	 * adds a new wallet object to it, then saves it back to file.  
	 */
	void writeToFile(String privkey, String addr){
		//Load the existing json file
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(filePath));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = (JSONObject) obj;
		String aeskey = (String) jsonObject.get("aes_key");
		String mpubkey = (String) jsonObject.get("master_public_key");
		String chaincode = (String) jsonObject.get("chain_code");
		String gcm = (String) jsonObject.get("GCM");
		Boolean testnet = (Boolean) jsonObject.get("testnet"); 
		long numkeys = (Long) jsonObject.get("keys_n");
		JSONArray msg = (JSONArray) jsonObject.get("keys");
		Iterator<JSONObject> iterator = msg.iterator();
		JSONArray jsonlist = new JSONArray();
		while (iterator.hasNext()) {
			jsonlist.add(iterator.next());
		}
		//Create the key json object
		numkeys ++;
		JSONObject keyobj = new JSONObject();
		keyobj.put("index",new Integer((int) numkeys));
		keyobj.put("priv_key", privkey);
		keyobj.put("address", addr);
		//Add key object to array
		jsonlist.add(keyobj);
		//Save the new json file
		Map newobj=new LinkedHashMap();
		newobj.put("aes_key", aeskey);
		newobj.put("master_public_key", mpubkey);
		newobj.put("chain_code", chaincode);
		newobj.put("GCM", gcm);
		newobj.put("testnet", testnet);
		newobj.put("keys_n", numkeys);
		newobj.put("keys", jsonlist);
		StringWriter jsonOut = new StringWriter();
		try {
			JSONValue.writeJSONString(newobj, jsonOut);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String jsonText = jsonOut.toString();
			try {
				FileWriter file = new FileWriter(filePath);
				file.write(jsonText);
				file.flush();
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	/**This method is used during pairing. It saves the data from the Authenticator to file*/
	void writePairingData(String mpubkey, String chaincode, String key, String GCM){
		JSONArray jsonlist = new JSONArray();
		Map obj=new LinkedHashMap();
		obj.put("aes_key", key);
		obj.put("master_public_key", mpubkey);
		obj.put("chain_code", chaincode);
		obj.put("GCM", GCM);
		obj.put("testnet", false);
		obj.put("keys_n", new Integer(0));
		obj.put("keys", jsonlist);
		StringWriter jsonOut = new StringWriter();
		try {
			JSONValue.writeJSONString(obj, jsonOut);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String jsonText = jsonOut.toString();
		try {
			FileWriter file = new FileWriter(filePath);
			file.write(jsonText);
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**Pulls the AES key from file and returns it*/
	public String getAESKey(){
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(filePath));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = (JSONObject) obj;
		String aeskey = (String) jsonObject.get("aes_key");
		return aeskey;
	}
	
	/**Returns the number of key pairs in the wallet*/
	public long getKeyNum(){
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(filePath));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = (JSONObject) obj;
		long numkeys = (Long) jsonObject.get("keys_n");
		return numkeys;
	}
	
	/**Returns the Master Public Key and Chaincode as an ArrayList object*/
	public ArrayList<String> getPubAndChain(){
		ArrayList<String> arr = new ArrayList<String>();
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(filePath));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = (JSONObject) obj;
		arr.add((String) jsonObject.get("master_public_key"));
		arr.add((String) jsonObject.get("chain_code"));
		return arr;
	}
	
	/** Returns a list of all addresses in the wallet*/
	public ArrayList<String> getAddresses(){
		ArrayList<String> arr = new ArrayList<String>();
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(filePath));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = (JSONObject) obj;
		JSONArray msg = (JSONArray) jsonObject.get("keys");
		Iterator<JSONObject> iterator = msg.iterator();
		JSONArray jsonlist = new JSONArray();
		while (iterator.hasNext()) {
			jsonlist.add(iterator.next());
		}
		JSONObject jsonAddr = (JSONObject) obj;
		for(int i=0; i<jsonlist.size(); i++){
			jsonAddr = (JSONObject) jsonlist.get(i);
			arr.add((String) jsonAddr.get("address"));
		}		
		return arr;
	}
	
	/**Returns the Child Key Index for a given address in the wallet*/
	public long getAddrIndex(String Address){
		ArrayList<String> arr = new ArrayList<String>();
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(filePath));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = (JSONObject) obj;
		JSONArray msg = (JSONArray) jsonObject.get("keys");
		Iterator<JSONObject> iterator = msg.iterator();
		JSONArray jsonlist = new JSONArray();
		while (iterator.hasNext()) {
			jsonlist.add(iterator.next());
		}
		JSONObject jsonAddr = (JSONObject) obj;
		for(int i=0; i<jsonlist.size(); i++){
			jsonAddr = (JSONObject) jsonlist.get(i);
			String jaddr = (String) jsonAddr.get("address");
			long index = (Long) jsonAddr.get("index");
			if (jaddr.equals(Address)){return index;}	
		}
		return 0;
	}
	
	/**Returns the private key for a given address*/
	public String getPrivKey(String Address){
		ArrayList<String> arr = new ArrayList<String>();
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(filePath));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = (JSONObject) obj;
		JSONArray msg = (JSONArray) jsonObject.get("keys");
		Iterator<JSONObject> iterator = msg.iterator();
		JSONArray jsonlist = new JSONArray();
		while (iterator.hasNext()) {
			jsonlist.add(iterator.next());
		}
		JSONObject jsonAddr = (JSONObject) obj;
		for(int i=0; i<jsonlist.size(); i++){
			jsonAddr = (JSONObject) jsonlist.get(i);
			String jaddr = (String) jsonAddr.get("address");
			String pkey = (String) jsonAddr.get("priv_key");
			if (jaddr.equals(Address)){return pkey;}	
		}
		return null;
	}
	
	/**Returns the private key using an index as the input*/
	public String getPrivKeyFromIndex(long index){
		ArrayList<String> arr = new ArrayList<String>();
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(filePath));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = (JSONObject) obj;
		JSONArray msg = (JSONArray) jsonObject.get("keys");
		Iterator<JSONObject> iterator = msg.iterator();
		JSONArray jsonlist = new JSONArray();
		while (iterator.hasNext()) {
			jsonlist.add(iterator.next());
		}
		JSONObject jsonAddr = (JSONObject) obj;
		for(int i=0; i<jsonlist.size(); i++){
			jsonAddr = (JSONObject) jsonlist.get(i);
			long jIndex = (Long) jsonAddr.get("index");
			String pkey = (String) jsonAddr.get("priv_key");
			if (jIndex==index){return pkey;}	
		}
		return null;
	}
	
	public Boolean getTestnet(){
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(filePath));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = (JSONObject) obj;
		Boolean testnet = (Boolean) jsonObject.get("testnet");
		return testnet;
	}
	
	public byte[] getGCMRegID(){
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(filePath));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = (JSONObject) obj;
		String GCM = (String) jsonObject.get("GCM");
		return GCM.getBytes();
	}
	
	/**
	 * Builds the transaction JSON object to send to the Authenticator
	 * The reason this is here is because WalletOperation uses a different JSON library which conflicts with JSONSimple
	 */
	public static byte[] formatMessage(int numInputs, ArrayList<byte[]> publickeys, ArrayList<Integer> childkeyindex, String transaction){
		Map obj=new LinkedHashMap();
		obj.put("version", 1);
		obj.put("ins_n", numInputs);
		obj.put("tx", transaction);
		JSONArray keylist = new JSONArray();
		for (int a=0; a<numInputs; a++){
			JSONObject keyobj = new JSONObject();
			keyobj.put("index", childkeyindex.get(a));
			keyobj.put("pubkey", bytesToHex(publickeys.get(a)));
			//Add key object to array
			keylist.add(keyobj);
		}
		obj.put("keylist", keylist);
		StringWriter jsonOut = new StringWriter();
		try {JSONValue.writeJSONString(obj, jsonOut);} 
		catch (IOException e1) {e1.printStackTrace();}
		String jsonText = jsonOut.toString();
		System.out.println(jsonText);
		byte[] jsonBytes = jsonText.getBytes();
		return jsonBytes;
	}
	
	public static ArrayList<byte[]> deserializeMessage(byte[] payload) throws ParseException{
		ArrayList<byte[]> sigs = new ArrayList<byte[]>();
		String strJson = new String(payload);
		JSONParser parser=new JSONParser();	  
		Object obj = parser.parse(strJson);
		JSONObject jsonObject = (JSONObject) obj;
		int version = ((Long) jsonObject.get("version")).intValue();
		int numSigs = ((Long) jsonObject.get("sigs_n")).intValue();
		JSONArray msg = (JSONArray) jsonObject.get("siglist");
		Iterator<JSONObject> iterator = msg.iterator();
		JSONArray jsonlist = new JSONArray();
		while (iterator.hasNext()) {
			jsonlist.add(iterator.next());
		}
		JSONObject jsonObj = (JSONObject) obj;
		for(int i=0; i<jsonlist.size(); i++){
			jsonObj = (JSONObject) jsonlist.get(i);
			String sig = (String) jsonObj.get("signature");
			sigs.add(hexStringToByteArray(sig));
		}
		return sigs;
	}
	
	/**Converts a hex string to a byte array*/
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	 /**Converts a byte array to a hex string*/
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
}
