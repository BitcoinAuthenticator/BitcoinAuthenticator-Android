package org.bitcoin.authenticator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

public class BAAlertDialogBase extends AlertDialog{

	TextView titleView;
	TextView secondaryTitleView;
	TextView okButton;
	TextView cancelButton;
	ImageView icon;
	ImageView centerIcon;
	
	LayoutInflater li;
	View  content;
	
	public BAAlertDialogBase(Context context){
		super(context);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
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
	
	public void setSecondaryTitle(Spanned value){
		secondaryTitleView.setText(value);
	}
	
	public void setDialogBackgroundColor(String value){
		content.setBackgroundColor(Color.parseColor(value));
	}
	
	public void setDialogIcon(int iconR){
		icon.setImageResource(iconR);
	}
	
	public void setDialogCenterIcon(int iconR){
		centerIcon.setImageResource(iconR);
	}
	
	
	public interface SingleInputOnClickListener{
		public void onClick(BAAlertDialogBase alert, String input);
	}
	
	public interface DeleteOnClickListener{
		public void onClick(BAAlertDialogBase alert);
	}
	
	public interface ConfirmTxOnClickListener{
		public void onClick(BAAlertDialogBase alert);
	}
	
	public interface ReadyToScanQROnClickListener{
		public void onClick(BAAlertDialogBase alert);
	}
}
