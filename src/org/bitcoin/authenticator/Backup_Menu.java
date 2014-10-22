package org.bitcoin.authenticator;

import org.bitcoin.authenticator.backup.PaperWallet;
import org.bitcoin.authenticator.backup.PaperWalletQR;
import org.bitcoin.authenticator.backup.PaperWalletQR.SeedQRData;
import org.bitcoin.authenticator.core.WalletCore;
import org.bitcoin.authenticator.core.exceptions.NoSeedOrMnemonicsFound;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase.ReadyToScanQROnClickListener;
import org.bitcoin.authenticator.dialogs.ReadyToScanQRDialog;

import com.google.zxing.integration.android.IntentIntegrator;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class Backup_Menu extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup_menu);
		
		setupBackupMnemonicBtn();
		setupBackupQRBtn();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.restore__menu, menu);
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
	
	private void setupBackupMnemonicBtn(){
		Button restoreButton = (Button) findViewById(R.id.backup_menu_btn_mnemonic);
		restoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
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
		});
	}
	
	private void setupBackupQRBtn(){
		Button restoreButton = (Button) findViewById(R.id.backup_menu_btn_qr);
		restoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity (new Intent(Backup_Menu.this, PaperWallet.class));
			}
		});
	}
}
