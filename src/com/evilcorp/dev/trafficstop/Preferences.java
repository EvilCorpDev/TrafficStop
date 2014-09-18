package com.evilcorp.dev.trafficstop;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity{
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.overridePendingTransition(R.anim.slite_in_right,R.anim.slide_out_right);
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		TrafficControl.runActivity = 2;
		Intent slideactivity = new Intent(this, TrafficControl.class);
    	startActivity(slideactivity);
	}
	
}
