package org.bitcoin.authenticator;

import org.bitcoin.authenticator.AuthenticatorPreferences.BAPreferences;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Creates an activity that explains to the user how the Bitcoin Authenticator works. 
 * This activity is pretty ugly. In the future I'm going to add some descriptive images/clip art and 
 * make it so the user can swipe to see the next image. 
 */
public class How_it_works extends Activity {

	Boolean paired;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_how_it_works);
		
		// Set the ViewPager adapter
		ExplanationPagerAdapter adapter = new ExplanationPagerAdapter();
	    ViewPager pager = (ViewPager) findViewById(R.id.how_it_works_pager);
	    pager.setOffscreenPageLimit(3);
	    pager.setAdapter(adapter);
		
		
		//The user can access this activity from two places ― the welcome activity and the menu in the 
		//wallet_list activity. If she's accessing it from the welcome activity (ie. the device isn't paired)
		//the button text should say "Begin Setup". If she's accessing it from the menu it should just say "Continue" 
	    paired = BAPreferences.ConfigPreference().getPaired(false);
	    ImageButton btn = (ImageButton) findViewById(R.id.btnBeginSetup);
	    /*if (paired==true){
	    	btn.setText("Continue");
	    }*/
		setupBeginSetupBtn();
	}
	
	/** 
	 * Like before, if the user is accessing this activity from the welcome activity, this button should take her
	 * to the Show_seed activity. Otherwise, it should just take her back to the main wallet_list activity.
	 */
	private void setupBeginSetupBtn(){
		ImageButton BeginSetupButton = (ImageButton) findViewById(R.id.btnBeginSetup);
		BeginSetupButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (paired==true){
			    	startActivity (new Intent(How_it_works.this, Wallet_list.class));
			    }
			    else {
					startActivity (new Intent(How_it_works.this, Show_seed.class));
			    }		
				
			}
		});
	}
	
	class ExplanationPagerAdapter extends PagerAdapter {

    
        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);
        }
        
        @Override
        public Object instantiateItem(ViewGroup container, int position){
        	int resId = 0;
            switch (position) {
            case 0:
                resId = R.id.how_it_works_page_one;
                break;
            case 1:
                resId = R.id.how_it_works_page_two;
                break;
            case 2:
                resId = R.id.how_it_works_page_three;
                break;
            }
        	
            View v = findViewById(resId);
            container.addView(v);
            return v;
        }
        
        @Override
        public void destroyItem(View container, int position, Object object) {
             //((ViewPager) container).removeView((View) object);
        }
    }
}
