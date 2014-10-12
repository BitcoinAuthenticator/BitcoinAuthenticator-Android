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

import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Uses BIP39 to generate a seed from 128 bits of entropy. The seed and mnemonic are saved to internal storage.  
 * Creates the activity which displays the seed to the user.
 */
public class Show_seed extends Activity {
		
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_seed);
		setupConfirmationCheckbox();
		setupContinueButton();
		
	    Boolean initialized = BAPreferences.ConfigPreference().getInitialized(false);//settings.getBoolean("initialized", false);
	    if (initialized == false){
	    	generateSeed();
	    }
	    else {
	    	try {
				displaySeed();
			} catch (NoSeedOrMnemonicsFound e) {
				e.printStackTrace();
			}
	    }
	}
	
	/**Inflates the menu and adds it to the action bar*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.show_seed_menu, menu);
		return true;
	}

	/**This method handles the clicks in the option menu*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_copy){
			
		}
		if (id == R.id.action_save){
			
		}
		if (id == R.id.action_qr){
			startActivity (new Intent(Show_seed.this, PaperWallet.class));
		}
		if (id == R.id.action_sss){
			
		}
		return super.onOptionsItemSelected(item);
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
	private void generateSeed(){
		new GenerateSeedTask() {
			@Override
	        protected void onPostExecute(String result) {
				try {
					//
					CheckBox chk = (CheckBox)findViewById(R.id.show_seed_chk_Confirmation);
		            chk.setEnabled(true);
		            Button btn = (Button)findViewById(R.id.show_seed_btn_Continue);
		            btn.setEnabled(true);
					
					displaySeed();
					BAPreferences.ConfigPreference().setInitialized(true);
				} catch (NoSeedOrMnemonicsFound e) {
					e.printStackTrace();
					
					// show error
					Toast.makeText(getApplicationContext(), "Error while creating seed", Toast.LENGTH_LONG).show();
				}
				finally {
					mProgressDialog.hide();
				}
	        }
		}.execute("");
	}
	
	private class GenerateSeedTask extends AsyncTask<String, Void, String> {

		@Override
        protected void onPreExecute() { 
			//Display a spinner while the device is pairing.
			mProgressDialog = new ProgressDialog(Show_seed.this, R.style.CustomDialogSpinner);
			mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
    		mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
            
            CheckBox chk = (CheckBox)findViewById(R.id.show_seed_chk_Confirmation);
            chk.setEnabled(false);
            Button btn = (Button)findViewById(R.id.show_seed_btn_Continue);
            btn.setEnabled(false);
		}
		
		@Override
		protected String doInBackground(String... params) {
		
			WalletCore wc = new WalletCore();
			wc.generateSeed(getApplicationContext(), true);
			return null;
		}
		
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
