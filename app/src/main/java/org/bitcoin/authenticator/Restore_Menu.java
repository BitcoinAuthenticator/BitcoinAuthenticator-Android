package org.bitcoin.authenticator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bitcoin.authenticator.BAPreferences.BAPreferences;
import org.bitcoin.authenticator.Backup.FileBackup;
import org.bitcoin.authenticator.Backup.Exceptions.CannotRestoreBackupFileException;
import org.bitcoin.authenticator.core.WalletCore;
import org.bitcoinj.crypto.MnemonicCode;

import com.ipaulpro.afilechooser.utils.FileUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Restore_Menu extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restore_menu);
		
		setupRestoreFromMnemonicBtn();
		setupRestoreFromFileBtn();
	}
	
	private void setupRestoreFromMnemonicBtn(){
		Button restoreButton = (Button) findViewById(R.id.restore_menu_btn_mnemonic);
		restoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity (new Intent(Restore_Menu.this, Restore_Mnemonic.class));
			}
		});
	}
	

	private static final int REQUEST_CHOOSER = 1234;
	
	private void setupRestoreFromFileBtn(){
		Button restoreButton = (Button) findViewById(R.id.restore_menu_btn_save);
		restoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				    // Create the ACTION_GET_CONTENT Intent
				    Intent getContentIntent = FileUtils.createGetContentIntent();

				    Intent intent = Intent.createChooser(getContentIntent, "Select a file");
				    startActivityForResult(intent, REQUEST_CHOOSER);
				}

		});
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch (requestCode) {
	        case REQUEST_CHOOSER:   
	            if (resultCode == RESULT_OK) {
	                final Uri uri = data.getData();
	                final String path = FileUtils.getPath(this, uri);

	                if (path != null && FileUtils.isLocal(path)) {
	                	showDecryptDialog(path);
	                }
	            }
	            break;
	    }
	}
	
	private void showDecryptDialog(final String path) {
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		input.setHint("Password");
		layout.addView(input);

		final CheckBox showpw = new CheckBox(this);
		showpw.setText("Show Password?");
		showpw.setChecked(false);
		layout.addView(showpw);
		showpw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

		       @Override
		       public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		    	   if (showpw.isChecked()){input.setInputType(InputType.TYPE_CLASS_TEXT);}
		    	   else {input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);}
		       }
		   }
		);     
	    new AlertDialog.Builder(Restore_Menu.this)
	    .setTitle("Restore from file")
	    .setMessage("Enter your password")
	    .setView(layout)
	    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	try {
	        		Editable value = input.getText(); 
		            WalletCore wc = new WalletCore();
        		    
		            String mnemonic = FileBackup.getAndDecryptBackupFileContent(path, input.getText().toString());
		            
        		    Toast.makeText(getApplicationContext(), mnemonic, Toast.LENGTH_LONG).show();
        		    String[] inputstring = mnemonic.split(" ");
    				if (inputstring.length == 12){
    					List<String> words = new ArrayList<String>();		
    					for (String s: inputstring){words.add(s);}
    					MnemonicCode mc = null;
    					try {mc = new MnemonicCode();} 
    					catch (IOException e) {e.printStackTrace();}
    					WalletCore wc2 = new WalletCore();
    					wc2.saveSeedBytes(Restore_Menu.this, mc.toSeed(words, ""));
    					wc2.saveMnemonic(Restore_Menu.this, inputstring);
    					BAPreferences.ConfigPreference().setInitialized(true);
    					startActivity (new Intent(Restore_Menu.this, Pair_wallet.class));
    				}
	        	} 
	        	catch (CannotRestoreBackupFileException e) {
					e.printStackTrace();
        		    Toast.makeText(getApplicationContext(), "Could not restore wallet from file", Toast.LENGTH_LONG).show();

				}	            		            
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    }).show();
	}
}
	
	
