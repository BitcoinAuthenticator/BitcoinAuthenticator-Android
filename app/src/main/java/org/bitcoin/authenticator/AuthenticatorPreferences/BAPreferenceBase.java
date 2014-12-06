package org.bitcoin.authenticator.AuthenticatorPreferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class BAPreferenceBase {
	Context context;
	protected void setContext(Context value){
		context = value;
	}
	protected Context getContext(){
		return context;
	}
	
	protected SharedPreferences getSharedPreferences(String key, int mode) {
		return getContext().getSharedPreferences(key,mode);
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
