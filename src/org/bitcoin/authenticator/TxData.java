package org.bitcoin.authenticator;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**Class creates an object that contains all the transaction data sent over from the wallet*/
public class TxData {
	
	int version;
	int numInputs;
	ArrayList<Integer> ChildKeyIndex;
	ArrayList<String> PublicKeys;
	byte[] tx;
	
	/**Constructor takes in the message payload as a string and parses it into its relevant parts*/
	public TxData(byte[] payload) throws ParseException{
		ChildKeyIndex = new ArrayList<Integer>();
		PublicKeys = new ArrayList<String>();
		String strJson = new String(payload);
		JSONParser parser=new JSONParser();	  
		Object obj = parser.parse(strJson);
		JSONObject jsonObject = (JSONObject) obj;
		version = ((Long) jsonObject.get("version")).intValue();
		numInputs = ((Long) jsonObject.get("in_n")).intValue();
		tx = Utils.hexStringToByteArray((String) jsonObject.get("tx"));
		JSONArray msg = (JSONArray) jsonObject.get("keylist");
		Iterator<JSONObject> iterator = msg.iterator();
		JSONArray jsonlist = new JSONArray();
		while (iterator.hasNext()) {
			jsonlist.add(iterator.next());
		}
		JSONObject jsonObj = (JSONObject) obj;
		for(int i=0; i<jsonlist.size(); i++){
			jsonObj = (JSONObject) jsonlist.get(i);
			int index = ((Long) jsonObj.get("index")).intValue();
			ChildKeyIndex.add(index);
			String pubkey = (String) jsonObj.get("pubkey");
			PublicKeys.add(pubkey);
		}
	}
	
	/**Returns the version as a string*/
	public int getVersion(){
		return version;
	}
	
	/**Returns the number of inputs*/
	public int getInputCount(){
		return numInputs;
	}
	
	/**Returns an array of the child key indexes used when creating the P2SH addresses*/
	public ArrayList<Integer> getIndexes(){
		return ChildKeyIndex;
	}
	
	/**Returns and array of public keys from the wallet*/
	public  ArrayList<String> getPublicKeys(){
		return PublicKeys;
	}
	
	/**Returns the raw unsigned transaction*/
	public byte[] getTransaction(){
		return tx;
	}
}
