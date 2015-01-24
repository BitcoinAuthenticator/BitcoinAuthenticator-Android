package org.bitcoin.authenticator;

import org.bitcoin.authenticator.BAPreferences.BAPreferences;
import org.bitcoin.authenticator.core.Backup.FileBackup;
import org.bitcoin.authenticator.core.Backup.Exceptions.CannotBackupToFileException;
import org.bitcoin.authenticator.core.WalletCore;
import org.bitcoin.authenticator.core.exceptions.NoSeedOrMnemonicsFound;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
		
	    Boolean initialized = BAPreferences.getInstance().ConfigPreference().getInitialized(false);//settings.getBoolean("initialized", false);
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
			WalletCore wc = new WalletCore();
			try {
				String mnemonic = wc.getMnemonicString(getApplicationContext());
				
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
				ClipData clip = ClipData.newPlainText("Wallet's mnemonic", mnemonic);
				clipboard.setPrimaryClip(clip);
				Toast.makeText(getApplicationContext(), "Copied successfully", Toast.LENGTH_LONG).show();
			} catch (NoSeedOrMnemonicsFound e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "Could not copy the mnemonic string", Toast.LENGTH_LONG).show();
			}
		}
		if (id == R.id.action_save){
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
			new AlertDialog.Builder(Show_seed.this)
		    .setTitle("Save seed to file")
		    .setMessage("Enter a password to encrypt your seed")
		    .setView(layout)
		    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            WalletCore wc = new WalletCore();
					String m;
		            try {
						m = wc.getMnemonicString(getApplicationContext());
						FileBackup.backupToFile(m, input.getText().toString());						
						Toast.makeText(getApplicationContext(), "Saved seed to " + FileBackup.getBackupFileAbsolutePath() + "/backups/", Toast.LENGTH_LONG).show();
					} catch (NoSeedOrMnemonicsFound e) {
						e.printStackTrace();
						Toast.makeText(getApplicationContext(), "Unable to save seed", Toast.LENGTH_LONG).show();
					}
					catch (CannotBackupToFileException e) {
						e.printStackTrace();
						Toast.makeText(getApplicationContext(), "Unable to save seed", Toast.LENGTH_LONG).show();
					}
		        }
		    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            // Do nothing.
		        }
		    }).show();
			
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
					BAPreferences.getInstance().ConfigPreference().setInitialized(true);
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
			    Boolean paired = BAPreferences.getInstance().ConfigPreference().getPaired(false);
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
