package org.bitcoin.authenticator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferences;
import org.bitcoin.authenticator.backup.PaperWallet;
import org.bitcoin.authenticator.core.WalletCore;
import org.bitcoin.authenticator.core.exceptions.NoSeedOrMnemonicsFound;

import com.google.bitcoin.crypto.MnemonicCode;
import com.google.bitcoin.crypto.MnemonicException.MnemonicLengthException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

/**
 * Uses BIP39 to generate a seed from 128 bits of entropy. The seed and mnemonic are saved to internal storage.  
 * Creates the activity which displays the seed to the user.
 */
public class Show_seed extends Activity {
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_seed);
		setupConfirmationCheckbox();
		setupContinueButton();
		setupBackupButtons();
		
	    Boolean initialized = BAPreferences.ConfigPreference().getInitialized(false);//settings.getBoolean("initialized", false);
	    if (initialized == false){
	    	try {
				generateSeed();
			} catch (NoSeedOrMnemonicsFound e) {
				e.printStackTrace();
			}
	    	BAPreferences.ConfigPreference().setInitialized(true);
	    }
	    else {
	    	try {
				displaySeed();
			} catch (NoSeedOrMnemonicsFound e) {
				e.printStackTrace();
			}
	    }
	}
	
	/** Prevents the back button from being pressed. Forces users to confirm they have saved their mnemonic seed.*/
	@Override
	public void onBackPressed() {
	}
	
	/**Loads the mnemonic encoded seed from internal storage and displays it in a textview.
	 * @throws NoSeedOrMnemonicsFound */
	private void displaySeed() throws NoSeedOrMnemonicsFound{
		WalletCore wc = new WalletCore();
		
		TextView tv = (TextView)findViewById(R.id.show_seed_MnemonicSeed);
		tv.setText(wc.getMnemonicString(getApplicationContext()));
	}

	/**
	 * This method implements BIP39 to generate a 512 bit seed from 128 bit checksummed entropy. The seed and the
	 * mnemonic encoded entropy are saved to internal storage.
	 * @throws NoSeedOrMnemonicsFound 
	 */
	@SuppressLint("TrulyRandom")
	private void generateSeed() throws NoSeedOrMnemonicsFound{
		
		WalletCore wc = new WalletCore();
		wc.generateSeed(getApplicationContext(), true);		
		displaySeed();
	}
	
	private void setupBackupButtons(){
		Button btnQR = ( Button ) findViewById( R.id.show_seed_backup_QR );
		btnQR.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				startActivity (new Intent(Show_seed.this, PaperWallet.class));
			}
		});
	}
	
	/**These last two methods setup the activity components*/
	private void setupConfirmationCheckbox(){
		CheckBox repeatChkBx = ( CheckBox ) findViewById( R.id.show_seed_chk_Confirmation );
		repeatChkBx.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				Button cont = (Button)findViewById(R.id.show_seed_btn_Continue);	    
				if ( isChecked )
				{
				cont.setEnabled(true);
				}
				else {
	        	cont.setEnabled(false);
				}
			}	
		});
	}
	
	private void setupContinueButton(){
		Button ContinueButton = (Button) findViewById(R.id.show_seed_btn_Continue);
		ContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			    Boolean paired = BAPreferences.ConfigPreference().getPaired(false);
			    if (paired == false){
			    	startActivity (new Intent(Show_seed.this, Pair_wallet.class));
			    	}
			    else {
			    	startActivity (new Intent(Show_seed.this, Wallet_list.class));
			    }
			}
		});
	}
	
}
