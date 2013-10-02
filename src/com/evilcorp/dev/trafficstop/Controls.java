package com.evilcorp.dev.trafficstop;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Controls {
	
	Context context;
	
	public Controls(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	
	public boolean isConnected() {
		ConnectivityManager connMan = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		return info.isConnected();
	}
	
	public boolean isConnectedWiFi() {
		ConnectivityManager connMan = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		return info.isConnected();
	}

	public void changeState(boolean enabled) {
	    try {
	    	    final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    	    final Class<?> conmanClass = Class.forName(conman.getClass().getName());
	    	    final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
	    	    iConnectivityManagerField.setAccessible(true);
	    	    final Object iConnectivityManager = iConnectivityManagerField.get(conman);
	    	    final Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
	    	    final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
	    	    setMobileDataEnabledMethod.setAccessible(true);
    	    	
	    	    setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	    }
	}

}
