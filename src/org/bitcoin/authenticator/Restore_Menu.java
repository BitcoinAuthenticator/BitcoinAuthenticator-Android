package org.bitcoin.authenticator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;
import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferences;
import org.bitcoin.authenticator.core.WalletCore;
import org.bitcoin.authenticator.core.exceptions.NoSeedOrMnemonicsFound;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase.ReadyToScanQROnClickListener;
import org.bitcoin.authenticator.dialogs.ReadyToScanQRDialog;
import org.bitcoinj.crypto.MnemonicCode;

import com.google.zxing.integration.android.IntentIntegrator;
import com.ipaulpro.afilechooser.utils.FileUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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

	                // Get the File path from the Uri
	                String path = FileUtils.getPath(this, uri);

	                // Alternatively, use FileUtils.getFile(Context, Uri)
	                if (path != null && FileUtils.isLocal(path)) {
	                    File file = new File(path);
	                    InputStream in = null;
	                    try {
	                    	try {in = new BufferedInputStream(new FileInputStream(file));} 
	                    	catch (FileNotFoundException e) {e.printStackTrace();}
	                    	final byte[] cipherbytes = IOUtils.toByteArray(in);          		    
	            		    
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
	            		            Editable value = input.getText(); 
	            		            WalletCore wc = new WalletCore();
	            					String m;
	            					char[] password = new char[input.getText().length()];
	            					for (int i=0; i<input.getText().length(); i++){
	            						password[i]=input.getText().charAt(i);
	            					}
	            					byte[] salt = new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
	            					SecretKeyFactory kf = null;
	            					try {kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");} 
	            					catch (NoSuchAlgorithmException e1) {e1.printStackTrace();	}
	            					PBEKeySpec spec = new PBEKeySpec(password, salt, 8192, 256);
	            					SecretKey tmp = null;
	            					try {tmp = kf.generateSecret(spec);} 
	            					catch (InvalidKeySpecException e1) {e1.printStackTrace();}
	            					SecretKey AESKey = new SecretKeySpec(tmp.getEncoded(), "AES");	
	            					Cipher cipher = null;
									try {cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");} 
									catch (NoSuchAlgorithmException e) {e.printStackTrace();} 
									catch (NoSuchPaddingException e) {e.printStackTrace();}
	    	            		    try {cipher.init(Cipher.DECRYPT_MODE, AESKey);} 
	    	            		    catch (InvalidKeyException e) {e.printStackTrace();}
	    	            		    String mnemonic = null;
	    	            		    try {mnemonic = new String(cipher.doFinal(cipherbytes));} 
	    	            		    catch (IllegalBlockSizeException e) {e.printStackTrace();} 
	    	            		    catch (BadPaddingException e) {e.printStackTrace();}
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
	            		    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	            		        public void onClick(DialogInterface dialog, int whichButton) {
	            		            // Do nothing.
	            		        }
	            		    }).show();
	                    	
	                    } catch (IOException e) {
							e.printStackTrace();
						}
	                    finally {
	                    	if (in != null) {
	                    		try {in.close();} catch (IOException e) {e.printStackTrace();}
	                    	}
	                    }
	                }
	            }
	            break;
	    }
	}
}
	
	
