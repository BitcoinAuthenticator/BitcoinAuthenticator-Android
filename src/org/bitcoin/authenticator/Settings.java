package org.bitcoin.authenticator;

import android.app.Activity;
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
		SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
		GCM = settings.getBoolean("GCM", true);
		CheckBox GCMChkBx = ( CheckBox ) findViewById( R.id.chkGCM );
		if (GCM){GCMChkBx.setChecked(true);}
	    else{GCMChkBx.setChecked(false);}
	    Testnet = settings.getBoolean("testnet", false);
	    CheckBox TestnetChkBx = ( CheckBox ) findViewById( R.id.chkTestnet );
		if (Testnet){TestnetChkBx.setChecked(true);}
	    else{TestnetChkBx.setChecked(false);}
		setupGCMCheckbox();
		setupTestnetCheckbox();
	}
	
	
	private void setupGCMCheckbox(){
		CheckBox ChkBx = ( CheckBox ) findViewById( R.id.chkGCM );
		ChkBx.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{    
				SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
				SharedPreferences.Editor settingseditor = settings.edit();	
				if ( isChecked ) {
					settingseditor.putBoolean("GCM", true);
					settingseditor.commit();
				}
				else {
					settingseditor.putBoolean("GCM", false);
					settingseditor.commit();
				}
			}	
		});
	}
	
	private void setupTestnetCheckbox(){
		CheckBox ChkBx = ( CheckBox ) findViewById( R.id.chkTestnet );
		ChkBx.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{    
				SharedPreferences settings = getSharedPreferences("ConfigFile", 0);
				SharedPreferences.Editor settingseditor = settings.edit();	
				if ( isChecked ) {
					settingseditor.putBoolean("testnet", true);
					settingseditor.commit();
				}
				else {
					settingseditor.putBoolean("testnet", false);
					settingseditor.commit();
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
