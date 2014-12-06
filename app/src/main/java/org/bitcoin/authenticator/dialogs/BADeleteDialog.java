package org.bitcoin.authenticator.dialogs;

import org.bitcoin.authenticator.R;
import org.bitcoin.authenticator.dialogs.BAAlertDialogBase.SingleInputOnClickListener;

import android.content.Context;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class BADeleteDialog extends BAAlertDialogBase{
	TextView deleteButton;
	TextView text;
	
	
	public BADeleteDialog(Context context) {
		super(context);
		content = li.inflate(R.layout.alert_dialog_delete, null);
		initWidgets(content);
		setView(content, 0, 0, 0, 0);
	}

	public void initWidgets(View v){
		centerIcon = (ImageView) v.findViewById(R.id.dialog_delete_center_icon);
		titleView = (TextView) v.findViewById(R.id.dialog_delete_title);
		secondaryTitleView = (TextView) v.findViewById(R.id.dialog_delete_secondary_title);
		text = (TextView) v.findViewById(R.id.dialog_delete_text);
		deleteButton = (TextView) v.findViewById(R.id.dialog_delete_delete_button);
		cancelButton = (TextView) v.findViewById(R.id.dialog_delete_cancel_button);
	}
	
	public void setDeleteText(String value){
		text.setText(value);
	}
	
	public void setDeleteText(Spanned value){
		text.setText(value);
	}
	
	public void setDeleteButtonListener(final DeleteOnClickListener listener){
		final BADeleteDialog t = this;
		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(t);
				t.cancel();
			}
		});
	}
	
	public void setCancelButtonListener(final DeleteOnClickListener listener){
		final BADeleteDialog t = this;
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(t);
				t.cancel();
			}
		});
	}
}
