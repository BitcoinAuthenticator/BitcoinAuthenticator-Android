package org.bitcoin.authenticator.dialogs;
import org.bitcoin.authenticator.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;

public class BASingleInputDialog extends BAAlertDialogBase{
	EditText input;
	
	public BASingleInputDialog(Context context) {
		super(context);
	    content = li.inflate(R.layout.alert_dialog_single_input, null);
	    initWidgets(content);
		setView(content, 0, 0, 0, 0);
	}
	
	public void initWidgets(View v){
		titleView = (TextView) v.findViewById(R.id.dialog_single_input_title);
		secondaryTitleView = (TextView) v.findViewById(R.id.dialog_single_input_secondary_title);
		okButton = (TextView) v.findViewById(R.id.dialog_single_input_ok_button);
		cancelButton = (TextView) v.findViewById(R.id.dialog_single_input_cancel_button);
		input = (EditText) v.findViewById(R.id.dialog_single_input_edit_text);
	}
	

	public void setOkButtonListener(final SingleInputOnClickListener listener){
		final BASingleInputDialog t = this;
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(t, input.getText().toString());
				t.cancel();
			}
		});
	}
	
	public void setCancelButtonListener(final SingleInputOnClickListener listener){
		final BASingleInputDialog t = this;
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(t, input.getText().toString());
				t.cancel();
			}
		});
	}
}
