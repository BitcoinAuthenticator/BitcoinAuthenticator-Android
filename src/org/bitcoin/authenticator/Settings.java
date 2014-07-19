package org.bitcoin.authenticator;

import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

public class Settings extends Activity {
	
	Boolean GCM;
	Boolean Testnet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		GCM = BAPreferences.ConfigPreference().getGCM(true);
		CheckBox GCMChkBx = ( CheckBox ) findViewById( R.id.chkGCM );
		if (GCM){GCMChkBx.setChecked(true);}
	    else{GCMChkBx.setChecked(false);}
	    Testnet = BAPreferences.ConfigPreference().getTestnet(false);
	    CheckBox TestnetChkBx = ( CheckBox ) findViewById( R.id.chkTestnet );
		if (Testnet){TestnetChkBx.setChecked(true);}
	    else{TestnetChkBx.setChecked(false);}
		setupGCMCheckbox();
		setupTestnetCheckbox();
	}
	
	public void onBackPressed() {  
	    this.startActivity(new Intent(Settings.this,Wallet_list.class));  
	    return;  
	}  
	
	private void setupGCMCheckbox(){
		CheckBox ChkBx = ( CheckBox ) findViewById( R.id.chkGCM );
		ChkBx.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{    
				if ( isChecked ) {
					BAPreferences.ConfigPreference().setGCM(true);
				}
				else {
					BAPreferences.ConfigPreference().setGCM(false);
				}
			}	
		});
	}
	
	private void setupTestnetCheckbox(){
		CheckBox ChkBx = ( CheckBox ) findViewById( R.id.chkTestnet );
		ChkBx.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{    
				if ( isChecked ) {
					BAPreferences.ConfigPreference().setTestnet(true);
				}
				else {
					BAPreferences.ConfigPreference().setTestnet(false);
				}
			}	
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if ((keyCode == KeyEvent.KEYCODE_BACK))
	    {
	        finish();
	    }
	    return super.onKeyDown(keyCode, event);
	}
}
