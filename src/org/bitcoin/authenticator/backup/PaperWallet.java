package org.bitcoin.authenticator.backup;


import org.bitcoin.authenticator.R;
import org.bitcoin.authenticator.core.WalletCore;
import org.bitcoin.authenticator.core.exceptions.NoSeedOrMnemonicsFound;

import org.bitcoinj.wallet.DeterministicSeed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PaperWallet extends Activity {

	LinearLayout waitingLayout;
	LinearLayout mainLayout;
	ImageView iv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_paper_wallet);
		
		waitingLayout = (LinearLayout) findViewById(R.id.paper_wallet_loading_layout);
		mainLayout = (LinearLayout) findViewById(R.id.paper_wallet_main_layout); mainLayout.setVisibility(View.INVISIBLE);
		iv = (ImageView) findViewById(R.id.paper_wallet_iv);	
		
		generateFullQR(getApplicationContext()); 
		displaySeedQR(); 
		
		Button save = (Button) findViewById(R.id.paper_wallet_btn_save_to_gallery);	
		save.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(fullQR != null){
//					MediaStore.Images.Media.insertImage(getContentResolver(), fullQR, "paper wallet" , "");
					MediaSaver.insertImage(getContentResolver(), fullQR, "paper wallet" , "");
					Toast.makeText(getApplicationContext(), "Saved QR to gallery !", Toast.LENGTH_LONG).show();
				}
				else
					Toast.makeText(getApplicationContext(), "Failed, try again !", Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.paper_wallet, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	Bitmap fullQR = null;
	private void generateFullQR(final Context c) {
		new Thread() {
			@Override
			public void run() {
				PaperWalletQR paperwalletQR = new PaperWalletQR(c);
				WalletCore wc = new WalletCore();
				
				try {
					String mnemonicStr = wc.getMnemonicString(c);
					DeterministicSeed seed = wc.getDeterministicSeed(c);
					
					fullQR = paperwalletQR.generatePaperWallet(mnemonicStr, seed, seed.getCreationTimeSeconds());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private void displaySeedQR() {
		new Thread() {
			@Override
			public void run() {
				try {
					PaperWalletQR paperwalletQR = new PaperWalletQR(getApplicationContext());
					WalletCore wc = new WalletCore();
					DeterministicSeed seed;
					seed = wc.getDeterministicSeed(getApplicationContext());
					final Bitmap qr = paperwalletQR.createQRSeedImage(seed, seed.getCreationTimeSeconds());
					

					// update UI
					PaperWallet.this.runOnUiThread(new Runnable() {
				        @Override
				        public void run() {
				        	iv.setImageBitmap(qr);
				        	mainLayout.setVisibility(View.VISIBLE);
				        	waitingLayout.setVisibility(View.INVISIBLE);
				        }
					});
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
}
