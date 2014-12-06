package org.bitcoin.authenticator.dialogs;

import java.util.ArrayList;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class BAPopupMenu extends PopupMenu implements OnMenuItemClickListener{

	ArrayList<BAPopupMenu.PopupButton> buttons;
	ActionsListener listener;
	
	public BAPopupMenu(Context context, View anchor) {
		super(context, anchor);
		this.setOnMenuItemClickListener(this);
	}

	public BAPopupMenu setButtons(ArrayList<BAPopupMenu.PopupButton> buttons){
		this.buttons = buttons;
		int i = 1;
		for (PopupButton s:buttons){
			this.getMenu().add(Menu.NONE, i, Menu.NONE, s.name);
			this.getMenu().getItem(i-1).setEnabled(s.active);
			i++;
		}
		return this;
	}
	
	public BAPopupMenu setActionsListener(ActionsListener listener){
		this.listener = listener;
		return this;
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		listener.pressed(item);
		return false;
	}
	
	public interface ActionsListener{
		public void pressed(MenuItem item);
	}
	
	static public class PopupButton{
		
		public PopupButton(String name, boolean active){
			this.name = name;
			this.active = active;
		}
		
		public String name;
		public boolean active;
	};
	
}
