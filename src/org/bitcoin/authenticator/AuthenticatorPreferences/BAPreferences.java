package org.bitcoin.authenticator.AuthenticatorPreferences;

import org.bitcoin.authenticator.AuthenticatorPreferences.Preferences.ConfigPreference;
import org.bitcoin.authenticator.AuthenticatorPreferences.Preferences.WalletPreference;

import android.app.Activity;

public class BAPreferences {
	static private WalletPreference mWalletPreference;
	static private ConfigPreference mConfigPreference;
	
	public BAPreferences(Activity activity){
		mWalletPreference = new WalletPreference(activity);
		mConfigPreference = new ConfigPreference(activity);
	}
	
	public static WalletPreference WalletPreference(){
		return mWalletPreference;
	}
	
	public static ConfigPreference ConfigPreference(){
		return mConfigPreference;
	}
}
