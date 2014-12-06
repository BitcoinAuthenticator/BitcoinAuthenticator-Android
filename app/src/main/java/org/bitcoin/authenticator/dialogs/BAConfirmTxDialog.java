package org.bitcoin.authenticator.dialogs;

import org.bitcoin.authenticator.R;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase.SingleInputOnClickListener;

import android.content.Context;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class BAConfirmTxDialog extends BAAlertDialogBase{
	TextView confirmButton;
	TextView dontAuthorizeButton;
	TextView text;
	
	
	public BAConfirmTxDialog(Context context) {
		super(context);
		content = li.inflate(R.layout.alert_dialog_confirm_tx, null);
		initWidgets(content);
		setView(content, 0, 0, 0, 0);
	}

	public void initWidgets(View v){
		titleView = (TextView) v.findViewById(R.id.dialog_confirm_tx_title);
		text = (TextView) v.findViewById(R.id.dialog_confirm_tx_text);
		confirmButton = (TextView) v.findViewById(R.id.dialog_confirm_tx_confirm_button);
		dontAuthorizeButton = (TextView) v.findViewById(R.id.dialog_confirm_tx_dont_authorize_button);
		cancelButton = (TextView) v.findViewById(R.id.dialog_confirm_tx_cancel_button);
	}
	
	public void setDeleteText(String value){
		text.setText(value);
	}
	
	public void setDeleteText(Spanned value){
		text.setText(value);
	}
	
	public void setConfirmButtonListener(final ConfirmTxOnClickListener listener){
		final BAConfirmTxDialog t = this;
		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(t);
				t.cancel();
			}
		});
	}
	
	public void setDontAuthorizeButtonListener(final ConfirmTxOnClickListener listener){
		final BAConfirmTxDialog t = this;
		dontAuthorizeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(t);
				t.cancel();
			}
		});
	}
	
	public void setCancelButtonListener(final ConfirmTxOnClickListener listener){
		final BAConfirmTxDialog t = this;
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(t);
				t.cancel();
			}
		});
	}
}
