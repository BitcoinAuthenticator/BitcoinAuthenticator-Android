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
	    Boolean initialized = BAPreferences.ConfigPreference().getInitialized(false);//settings.getBoolean("initialized", false);
	    if (initialized == false){
	    	generateSeed();
	    	BAPreferences.ConfigPreference().setInitialized(true);
	    }
	    else {
	    	displaySeed();
	    }
	}
	
	/** Prevents the back button from being pressed. Forces users to confirm they have saved their mnemonic seed.*/
	@Override
	public void onBackPressed() {
	}
	
	/**Loads the mnemonic encoded seed from internal storage and displays it in a textview.*/
	private void displaySeed(){
		String FILENAME = "mnemonic";
		FileInputStream fin = null;
		try {
			fin = openFileInput(FILENAME);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int c;
		String temp="";
		try {
			while( (c = fin.read()) != -1){
			   temp = temp + Character.toString((char)c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//String temp contains all the data of the file.
		try {
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		TextView tv = (TextView)findViewById(R.id.txtMnemonicSeed);
		tv.setText(temp);
	}

	/**
	 * This method implements BIP39 to generate a 512 bit seed from 128 bit checksummed entropy. The seed and the
	 * mnemonic encoded entropy are saved to internal storage.
	 */
	@SuppressLint("TrulyRandom")
	private void generateSeed(){
		//Generate 128 bits entropy.
        SecureRandom secureRandom = null;
		try {
			secureRandom = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] bytes = new byte[16];
		secureRandom.nextBytes(bytes);
		MnemonicCode ms = null;
		try {
			ms = new MnemonicCode();
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<String> mnemonic = null;
		try {
		mnemonic = ms.toMnemonic(bytes);
		} catch (MnemonicLengthException e) {
			e.printStackTrace();
		}
		byte[] seed = MnemonicCode.toSeed(mnemonic, "");	
		String[] strArray = mnemonic.toArray(new String[0]);
		String strMnemonic = Arrays.toString(strArray);
		strMnemonic = strMnemonic.replace("[", "");
		strMnemonic = strMnemonic.replace("]", "");
		strMnemonic = strMnemonic.replace(",", "");
		//Save to private internal storage.
		try
		{
		    String FILENAME = "mnemonic";
		    FileOutputStream outputStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);
		    outputStream.write(strMnemonic.getBytes());
		    outputStream.close();
		}
		catch (final Exception ex) { Log.e("JAVA_DEBUGGING", "Exception while creating save file!"); ex.printStackTrace(); }
		try
		{
		    String FILENAME = "seed";
		    FileOutputStream outputStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);
		    outputStream.write(seed);
		    outputStream.close();
		}
		catch (final Exception ex) { Log.e("JAVA_DEBUGGING", "Exception while creating save file!"); ex.printStackTrace(); }
	    //Display the seed.
		displaySeed();
	}
	
	/**These last two methods setup the activity components*/
	private void setupConfirmationCheckbox(){
		CheckBox repeatChkBx = ( CheckBox ) findViewById( R.id.chkConfirmation );
		repeatChkBx.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				Button cont = (Button)findViewById(R.id.btnContinue);	    
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
		Button ContinueButton = (Button) findViewById(R.id.btnContinue);
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
