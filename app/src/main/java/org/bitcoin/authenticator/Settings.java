package org.bitcoin.authenticator;

import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferences;
import org.bitcoin.authenticator.GcmUtil.GCMRegister;
import org.bitcoin.authenticator.GcmUtil.GcmUtilGlobal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
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
		
		// GCM
		CheckBox GCMChkBx = ( CheckBox ) findViewById( R.id.chkGCM );
		if (GCM){GCMChkBx.setChecked(true);}
	    else{GCMChkBx.setChecked(false);}
	    Testnet = BAPreferences.ConfigPreference().getTestnet(false);
	    
	    Button btnResetGCM = ( Button ) findViewById( R.id.btn_reset_gcm );
	    btnResetGCM.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GCMRegister.storeRegistrationId(getApplicationContext(), null);
				
				// notify user to restart application
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Settings.this);
				alertDialogBuilder.setTitle("Important !");
				alertDialogBuilder.setMessage("Please restart the application so the changes will take effect")
								  .setCancelable(true);
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}
	    });
	    
	    // Network
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
