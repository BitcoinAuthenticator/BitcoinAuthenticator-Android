package org.bitcoin.authenticator.AuthenticatorPreferences.Preferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferenceBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;

public class ConfigPreference  extends BAPreferenceBase{
	public ConfigPreference(Activity activity){
		setActivity(activity);
		setPrefix("ConfigFile");
	}
	
	/**
	 * Wallet Count
	 */
	
	public Set<Long> getWalletIndexList(){
		SharedPreferences settings2 = getSharedPreferences(getPrefix(), 0);
		Set<String> str = settings2.getStringSet(BAPreferenceType.WALLET_INDEX_LIST.toString(), null);
		if(str == null)
			str = new HashSet<String>();
		
		Set<Long> ret = new HashSet<Long>();
		for(String s: str) {
			Long v = Long.parseLong(s);
			ret.add(v);
		}
		
		return ret;
	}
	
	public void addWalletIndex(Long value){
		SharedPreferences.Editor editor = getEditor(getPrefix());
		Set<Long> all = getWalletIndexList();
		if(all == null)
			all = new HashSet<Long>();
		all.add(value);
		
		Set<String> toStore = new HashSet<String>();
		for(Long l:all) {
			String v = Long.toString(l);
			toStore.add(v);
		}
		
		editor.putStringSet(BAPreferenceType.WALLET_INDEX_LIST.toString(), toStore);
		editor.commit();
	}
	
	/**
	 * testnet
	 */
	
	public boolean getTestnet(boolean defValue){
		SharedPreferences settings2 = getSharedPreferences(getPrefix(), 0);
		return settings2.getBoolean(BAPreferenceType.TESTNET.toString(), defValue);
	}
	
	public void setTestnet(boolean value){
		SharedPreferences.Editor editor = getEditor(getPrefix());
		editor.putBoolean(BAPreferenceType.TESTNET.toString(), value);
		editor.commit();
	}
	
	/**
	 * Pending list
	 */
	
	private String getPendingListString(){
		SharedPreferences settings2 = getSharedPreferences(getPrefix(), 0);
		return settings2.getString(BAPreferenceType.PENDING_LIST.toString(), "");
	}
	
	public JSONArray getPendingJsonList() throws JSONException{
		return new JSONArray(getPendingListString());
	}
	
	public ArrayList<String> getPendingList() {
		ArrayList<String> ret = new ArrayList<String>();
		try {
			JSONArray arr = getPendingJsonList();
			for (int i = 0; i < arr.length(); i++)
				ret.add(arr.getString(i));
		} catch (JSONException e) {
			// no list yet
		}
		
		return ret;
	}
	
	public void setPendingList(JSONArray arr){
		setPendingList(arr.toString());
	}
	
	public void setPendingList(String value){
		SharedPreferences.Editor editor = getEditor(getPrefix());
		editor.putString(BAPreferenceType.PENDING_LIST.toString(), value);
		editor.commit();
	}
	
	public void removePendingRequestFromListAndThenUpdate(String requestID) throws JSONException{
		   JSONArray pja = getPendingJsonList();
		   JSONArray newPja = new JSONArray();
		   if(pja != null){
			   for(int i=0;i<pja.length();i++){
				   if(!requestID.equals(pja.get(i)))
					   newPja.put(pja.get(i));
			   }
			}
		   setPendingList(newPja);
	}
	
	public void addPendingRequestToList(String requestID) throws JSONException {
		JSONArray pja = getPendingJsonList();
		pja.put(requestID);
		setPendingList(pja);
	}
	
	/**
	 * Pending Request
	 */
	
	public String getPendingRequestAsString(String reqID){
		SharedPreferences settings2 = getSharedPreferences(getPrefix(), 0);
		return settings2.getString(reqID, null);
	}
	
	public JSONObject getPendingRequestAsJsonObject(String reqID) throws JSONException{
		return new JSONObject(getPendingRequestAsString(reqID));
	}
	
	public void setPendingRequest(String reqID, JSONObject req){
		setPendingRequest(reqID, req.toString());
	}
	
	public void setPendingRequest(String reqID, String req){
		SharedPreferences.Editor editor = getEditor(getPrefix());
		editor.putString(reqID, req);
		editor.commit();
	}
	
	/**
	 * Request
	 */
	
	public void setRequest(boolean value){
		SharedPreferences.Editor editor = getEditor(getPrefix());
		editor.putBoolean(BAPreferenceType.REQUEST.toString(), value);
		editor.commit();
	}

	public boolean getRequest(boolean defValue){
		SharedPreferences data = getActivity().getSharedPreferences(getPrefix(), 0);
		return data.getBoolean(BAPreferenceType.REQUEST.toString(), defValue);
	}
	
	/**
	 * Paired
	 */
	
	public void setPaired(boolean value){
		SharedPreferences.Editor editor = getEditor(getPrefix());
		editor.putBoolean(BAPreferenceType.PAIRED.toString(), value);
		editor.commit();
	}

	public boolean getPaired(boolean defValue){
		SharedPreferences data = getActivity().getSharedPreferences(getPrefix(), 0);
		return data.getBoolean(BAPreferenceType.PAIRED.toString(), defValue);
	}
	
	/**
	 * GCM
	 */
	
	public void setGCM(boolean value){
		SharedPreferences.Editor editor = getEditor(getPrefix());
		editor.putBoolean(BAPreferenceType.GCM.toString(), value);
		editor.commit();
	}

	public boolean getGCM(boolean defValue){
		SharedPreferences data = getActivity().getSharedPreferences(getPrefix(), 0);
		return data.getBoolean(BAPreferenceType.GCM.toString(), defValue);
	}
	
	/**
	 * Initialized
	 */
	
	public void setInitialized(boolean value){
		SharedPreferences.Editor editor = getEditor(getPrefix());
		editor.putBoolean(BAPreferenceType.INITIALIZED.toString(), value);
		editor.commit();
	}

	public boolean getInitialized(boolean defValue){
		SharedPreferences data = getActivity().getSharedPreferences(getPrefix(), 0);
		return data.getBoolean(BAPreferenceType.INITIALIZED.toString(), defValue);
	}
	
	/**
	 * Enum for the various keys in the config preference
	 * @author alon
	 *
	 */
	private enum BAPreferenceType {
		PENDING_LIST			("pendingList"		),
		WALLET_INDEX_LIST		("walletIndexList"	),
		TESTNET					("testnet"			),
		REQUEST					("request"			),
		PAIRED					("paired"			),
		GCM						("GCM"				),
		INITIALIZED				("initialized"		);
		
		private String name;       

	    private BAPreferenceType(String s) {
	        name = s;
	    }

	    public boolean equalsName(String otherName){
	        return (otherName == null)? false:name.equals(otherName);
	    }

	    public String toString(){
	       return name;
	    }
	}
}
