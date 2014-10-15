package org.bitcoin.authenticator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferences;
import org.bitcoin.authenticator.core.WalletCore;
import org.bitcoinj.crypto.MnemonicCode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Restore extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restore);
		setupRestoreBtn();
	}
	
	private void setupRestoreBtn(){
		Button restoreButton = (Button) findViewById(R.id.btnRestore_Done);
		restoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText input = (EditText) findViewById(R.id.restore_seed);
				MnemonicCode mc = null;
				try {mc = new MnemonicCode();} 
				catch (IOException e) {e.printStackTrace();}
				String[] inputstring = input.getText().toString().split(" ");
				if (inputstring.length == 12){
					List<String> words = new ArrayList<String>();		
					for (String s: inputstring){words.add(s);}
					WalletCore wc = new WalletCore();
					wc.saveSeedBytes(Restore.this, mc.toSeed(words, ""));
					wc.saveMnemonic(Restore.this, inputstring);
					BAPreferences.ConfigPreference().setInitialized(true);
					startActivity (new Intent(Restore.this, Pair_wallet.class));
				}
			}
		});
	}
	
	
}
