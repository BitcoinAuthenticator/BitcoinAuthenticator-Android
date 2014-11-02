package org.bitcoin.authenticator;

import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferences;
import org.bitcoin.authenticator.core.WalletCore;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase.ReadyToScanQROnClickListener;
import org.bitcoin.authenticator.dialogs.ReadyToScanQRDialog;

import com.google.zxing.integration.android.IntentIntegrator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

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
	
	private void setupRestoreFromFileBtn(){
		Button restoreButton = (Button) findViewById(R.id.restore_menu_btn_save);
		restoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//startActivity (new Intent(Restore_Menu.this, Restore_Mnemonic.class));
			}
		});
	}
	
	
}
