package org.bitcoin.authenticator.AuthenticatorPreferences.Preferences;

import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferenceBase;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class WalletPreference extends BAPreferenceBase{
	
	
	public WalletPreference(Context context){
		setContext(context);
		setPrefix("WalletData");
	}
	
	/**
	 * Wallet
	 */
	
	public void setWallet(String 
			walletID, 
			String name,
			String type, 
			String extIP, 
			String locIP, 
			int networkType,
			boolean deleted){
		setName(walletID, name);
		setType(walletID, type);
		setExternalIP(walletID, extIP);
		setLocalIP(walletID, locIP);
		setNetworkType(walletID, networkType);
		setDeleted(walletID, deleted);
	}
	
	/**
	 * will return true if wallet id is not been used
	 * @param walletID
	 * @return
	 */
	public boolean checkIFWalletNumAvailable(String walletID){
		return getName(walletID, null) == null? true:false;
	}
	
	/**
	 * ID
	 */
	
	public void setName(String walletID,String value){
		SharedPreferences.Editor editor = getEditor(getPrefix() + walletID);	
		editor.putString(BAPreferenceType.ID.toString(), value);
		editor.commit();
	}
	
	public String getName(String walletID, String defValue){
		SharedPreferences data = getContext().getSharedPreferences(getPrefix() + walletID, 0);
		return data.getString(BAPreferenceType.ID.toString(), defValue);
	}
	
	/**
	 * Deleted
	 */
		
	public void setDeleted(String walletID,boolean value){
		SharedPreferences.Editor editor = getEditor(getPrefix() + walletID);
		editor.putBoolean(BAPreferenceType.DELETED.toString(), value);
		editor.commit();
	}

	public boolean getDeleted(String walletID, boolean defValue){
		SharedPreferences data = getContext().getSharedPreferences(getPrefix() + walletID, 0);
		return data.getBoolean(BAPreferenceType.DELETED.toString(), defValue);
	}
	
	/**
	 * Network Type
	 */
	
	public void setNetworkType(String walletID,int value){
		SharedPreferences.Editor editor = getEditor(getPrefix() + walletID);
		editor.putInt(BAPreferenceType.NETWORK.toString(), value);
		editor.commit();
	}

	public int getNetworkType(String walletID, int defValue){
		SharedPreferences data = getContext().getSharedPreferences(getPrefix() + walletID, 0);
		return data.getInt(BAPreferenceType.NETWORK.toString(), defValue);
	}
	
	/**
	 * Type
	 */
	
	public void setType(String walletID,String value){
		SharedPreferences.Editor editor = getEditor(getPrefix() + walletID);
		editor.putString(BAPreferenceType.TYPE.toString(), value);
		editor.commit();
	}

	public String getType(String walletID, String defValue){
		SharedPreferences data = getContext().getSharedPreferences(getPrefix() + walletID, 0);
		return data.getString(BAPreferenceType.TYPE.toString(), defValue);
	}
	
	/**
	 * External IP
	 */
	
	public void setExternalIP(String walletID,String value){
		SharedPreferences.Editor editor = getEditor(getPrefix() + walletID);
		editor.putString(BAPreferenceType.EXTERNAL_IP.toString(), value);
		editor.commit();
	}

	public String getExternalIP(String walletID, String defValue){
		SharedPreferences data = getContext().getSharedPreferences(getPrefix() + walletID, 0);
		return data.getString(BAPreferenceType.EXTERNAL_IP.toString(), defValue);
	}
	
	/**
	 * Local IP
	 */
	
	public void setLocalIP(String walletID,String value){
		SharedPreferences.Editor editor = getEditor(getPrefix() + walletID);
		editor.putString(BAPreferenceType.LOCAL_IP.toString(), value);
		editor.commit();
	}

	public String getLocalIP(String walletID, String defValue){
		SharedPreferences data = getContext().getSharedPreferences(getPrefix() + walletID, 0);
		return data.getString(BAPreferenceType.LOCAL_IP.toString(), defValue);
	}
	
	/**
	 * Enum for the various keys in the wallet preference
	 * @author alon
	 *
	 */
	private enum BAPreferenceType {
		ID					("ID"			),
		DELETED				("Deleted"		),
		NETWORK				("NetworkType"	),
		TYPE				("Type"			),
		EXTERNAL_IP			("ExternalIP"	),
		LOCAL_IP			("LocalIP"		);
		
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
