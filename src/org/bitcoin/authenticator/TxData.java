package org.bitcoin.authenticator;

import java.util.ArrayList;

/**Class creates an object that contains all the transaction data sent over from the wallet*/
public class TxData {
	
	String version;
	int numInputs;
	ArrayList<Integer> ChildKeyIndex;
	ArrayList<byte[]> PublicKeys;
	byte[] tx;
	
	/**Constructor takes in the message payload as a string and parses it into its relevant parts*/
	public TxData(String payload){
		System.out.println("#1");
		ChildKeyIndex = new ArrayList<Integer>();
		PublicKeys = new ArrayList<byte[]>();
		version = payload.substring(0, 2);
		System.out.println(version);
		numInputs = Integer.parseInt(payload.substring(2,6),16);
		System.out.println(numInputs);
		int pos = 6;
		for (int i=0; i<numInputs; i++){
			ChildKeyIndex.add(Integer.parseInt(payload.substring(pos, pos+8),16));
			System.out.println(ChildKeyIndex.get(i));
			PublicKeys.add(Utils.hexStringToByteArray(payload.substring(pos+8, pos + 74)));
			System.out.println(Utils.bytesToHex(PublicKeys.get(i)));
			pos = pos + 74;
		}
		tx = Utils.hexStringToByteArray(payload.substring(((numInputs*74)+6),payload.length()));
	}
	
	/**Returns the version as a string*/
	public String getVersion(){
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
	public  ArrayList<byte[]> getPublicKeys(){
		return PublicKeys;
	}
	
	/**Returns the raw unsigned transaction*/
	public byte[] getTransaction(){
		return tx;
	}
}
