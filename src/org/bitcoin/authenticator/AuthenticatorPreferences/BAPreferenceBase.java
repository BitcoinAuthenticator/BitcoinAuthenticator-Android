package org.bitcoin.authenticator.AuthenticatorPreferences;

import android.app.Activity;
import android.content.SharedPreferences;

public class BAPreferenceBase {
	Activity activity;
	protected void setActivity(Activity value){
		activity = value;
	}
	protected Activity getActivity(){
		return activity;
	}
	
	protected SharedPreferences getSharedPreferences(String key, int mode){
		return getActivity().getSharedPreferences(key,mode);
	}
	
	protected SharedPreferences.Editor getEditor(String key){
		return getSharedPreferences(key,0).edit();
	}
	
	/**
	 * The shared preference prefix key 
	 */
	private String prefix;
	protected void setPrefix(String value){
		prefix = value;
	}
	protected String getPrefix(){
		return prefix;
	}
	
}
