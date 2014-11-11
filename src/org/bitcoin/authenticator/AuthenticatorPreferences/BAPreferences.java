package org.bitcoin.authenticator.AuthenticatorPreferences;

import org.bitcoin.authenticator.AuthenticatorPreferences.Preferences.ConfigPreference;
import org.bitcoin.authenticator.AuthenticatorPreferences.Preferences.WalletPreference;

import android.app.Activity;
import android.content.Context;

public class BAPreferences {
	static private WalletPreference mWalletPreference;
	static private ConfigPreference mConfigPreference;
	
	public BAPreferences(Context context){
		mWalletPreference = new WalletPreference(context);
		mConfigPreference = new ConfigPreference(context);
	}
	
	public static WalletPreference WalletPreference(){
		return mWalletPreference;
	}
	
	public static ConfigPreference ConfigPreference(){
		return mConfigPreference;
	}
}
