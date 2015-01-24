package org.bitcoin.authenticator.BAPreferences;

import org.bitcoin.authenticator.BAPreferences.Preferences.ConfigPreference;
import org.bitcoin.authenticator.BAPreferences.Preferences.WalletPreference;

import android.content.Context;

public class BAPreferences {
	private WalletPreference mWalletPreference;
	private ConfigPreference mConfigPreference;
	
	BAPreferences(Context context){
		mWalletPreference = new WalletPreference(context);
		mConfigPreference = new ConfigPreference(context);
	}

    static BAPreferences instance;
    public static void init(Context context) {
        instance = new BAPreferences(context);
    }

    public static BAPreferences getInstance() {
        return instance;
    }
	
	public  WalletPreference WalletPreference(){
		return mWalletPreference;
	}
	
	public  ConfigPreference ConfigPreference(){
		return mConfigPreference;
	}
}
