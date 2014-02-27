package com.evilcorp.dev.trafficstop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AutoBroadReceiv extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		SharedPreferences sdPref = PreferenceManager.getDefaultSharedPreferences(context);
		if(sdPref.getBoolean("pref_autorun", false)) {
			context.startService(new Intent(context, TrafficConrolService.class)); 
		}
	}
	
	

}
