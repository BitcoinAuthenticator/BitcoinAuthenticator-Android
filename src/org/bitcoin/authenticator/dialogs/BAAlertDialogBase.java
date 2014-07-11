package org.bitcoin.authenticator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Button;

public class BAAlertDialogBase extends AlertDialog{

	TextView titleView;
	TextView secondaryTitleView;
	TextView okButton;
	TextView cancelButton;
	
	LayoutInflater li;
	View  content;
	
	public BAAlertDialogBase(Context context){
		super(context);
		li = LayoutInflater.from(context);
		setCancelable(true);
	}
	
	public BAAlertDialogBase(Context context, View view) {
		super(context);
		setContentView(view);
	}
	
	public void setTitle(String value){
		titleView.setText(value);
	}
	
	public void setSecondaryTitle(String value){
		secondaryTitleView.setText(value);
	}
	
	public void setDialogBackgroundColor(String value){
		content.setBackgroundColor(Color.parseColor(value));
	}
	
	
	public interface SingleInputOnClickListener{
		public void onClick(BAAlertDialogBase alert, String input);
	}
}
