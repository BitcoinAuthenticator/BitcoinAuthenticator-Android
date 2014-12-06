package org.bitcoin.authenticator.dialogs;

import org.bitcoin.authenticator.R;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase.SingleInputOnClickListener;

import android.content.Context;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ReadyToScanQRDialog extends BAAlertDialogBase{
	TextView text;
	
	
	public ReadyToScanQRDialog(Context context) {
		super(context);
		content = li.inflate(R.layout.alert_dialog_ready_to_scan_qr, null);
		initWidgets(content);
		setView(content, 0, 0, 0, 0);
	}

	public void initWidgets(View v){
		centerIcon = (ImageView) v.findViewById(R.id.dialog_ready_to_scan_qr_center_icon);
		titleView = (TextView) v.findViewById(R.id.dialog_ready_to_scan_qr_title);
		secondaryTitleView = (TextView) v.findViewById(R.id.dialog_ready_to_scan_qr_secondary_title);
		text = (TextView) v.findViewById(R.id.dialog_ready_to_scan_qr_text);
		okButton = (TextView) v.findViewById(R.id.dialog_ready_to_scan_qr_ok_button);
		cancelButton = (TextView) v.findViewById(R.id.dialog_ready_to_scan_qr_cancel_button);
	}
	
	public void setReadyText(String value){
		text.setText(value);
	}
	
	public void setOkButtonListener(final ReadyToScanQROnClickListener listener){
		final ReadyToScanQRDialog t = this;
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(t);
				t.cancel();
			}
		});
	}
	
	public void setCancelButtonListener(final ReadyToScanQROnClickListener listener){
		final ReadyToScanQRDialog t = this;
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(t);
				t.cancel();
			}
		});
	}
}
