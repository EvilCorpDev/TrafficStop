package com.evilcorp.dev.trafficstop;

import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TrafficControl extends Activity implements OnSharedPreferenceChangeListener, OnTouchListener{
	
	public static final String LOG_TAG = "myLogs";
	public static final String BROADCAST_ACTION = "com.evilcorp.dev.broadcast";
	
	double mBytes;
	boolean isEnable;
	int startCount;
	
	TextView tvMain;
	ImageView imView;
	ProgressBar pbLimit;
	Controls ctr;
	SharedPreferences sdPref;
	Timer t;
	TimerTask tt;
	BroadcastReceiver br;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		initialize();
		
		 isEnable = ctr.isConnected();
		
		 if(isEnable) {
			 imView.setImageResource(R.drawable.button_up);
		 }
		 else {
			 imView.setImageResource(R.drawable.button_up_start);
		 }
		
		startService(new Intent(this, TrafficConrolService.class).putExtra("mBytes", mBytes));
		
		br = new BroadcastReceiver() {
			public void onReceive(android.content.Context context, Intent intent) {
				updateText(intent.getDoubleExtra("traff", 0), intent.getDoubleExtra("allTraff", 0));
			}
		};
		
		IntentFilter iFilter = new IntentFilter(BROADCAST_ACTION);
		
		registerReceiver(br, iFilter);
		
		sdPref.edit().putInt("startCount", startCount++);
	}
	
	private void initialize() {
		
		sdPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		startCount = sdPref.getInt("startCount", 0);
		
		mBytes = Integer.parseInt(sdPref.getString("pref_Mbytes", "20"));
			
		 tvMain = (TextView)findViewById(R.id.tvMain);
		 
		 imView = (ImageView)findViewById(R.id.imView);
		 imView.setOnTouchListener(this);
		 
		 pbLimit = (ProgressBar)findViewById(R.id.pbLimit);
		 pbLimit.setMax((int)mBytes);
		
		 ctr = new Controls(this);
	}
	
	public void updateText(double traff, double allTraff) {
		tvMain.setText(getResources().getString(R.string.data_day) + " " + NumberFormat.getInstance().format(traff) + ", Mb"
				+ getResources().getString(R.string.all_data) +  " " + NumberFormat.getInstance().format(allTraff) + ", Mb");
		pbLimit.setProgress((int) Math.round(traff));
	}
	
	public void stopServ(View v) {
		stopService(new Intent(this, TrafficConrolService.class));
	}
	
	public void startServ(View v) {
		startService(new Intent(this, TrafficConrolService.class).putExtra("mBytes", mBytes));
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.traffic_control, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, Preferences.class));
			break;
		case R.id.stat:
			startActivity(new Intent(this, Statistic.class));
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}



	@Override
	public void onSharedPreferenceChanged(SharedPreferences sd, String key) {
		// TODO Auto-generated method stub
		if(key.equals("pref_Mbytes") || key.equals("pref_foreground")) {
			stopServ(new View(this));
			mBytes = Integer.parseInt(sd.getString("pref_Mbytes", "20"));
			startService(new Intent(this, TrafficConrolService.class).putExtra("mBytes", mBytes));
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		sdPref.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(br);
		sdPref.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(isEnable) {
				((ImageView)v).setImageResource(R.drawable.button_pressed);
				ctr.changeState(false); 
			}
			else {
				((ImageView)v).setImageResource(R.drawable.button_pressed_start);
				ctr.changeState(true); 
			}
			break;
		case MotionEvent.ACTION_UP:
			if(isEnable) {
				((ImageView)v).setImageResource(R.drawable.button_up_start);
				isEnable = false;
			}
			else {
				((ImageView)v).setImageResource(R.drawable.button_up);
				isEnable = true;
			}
			break;
		}
		return true;
	}
	
	/*public void printDatabase(View v) {
		stopServ(v);
		DB db = new DB(this);
		db.open();
		Cursor c = db.getAllData();
		if(c.moveToFirst()) {
			do {
				Log.d(LOG_TAG, "id = " + c.getInt(c.getColumnIndex(DB.COLUMN_ID)) +  " date = " + c.getString(c.getColumnIndex(DB.COLUMN_DATE)) 
						+ " start = " + c.getDouble(c.getColumnIndex(DB.COLUMN_START)) + " end = " + c.getDouble(c.getColumnIndex(DB.COLUMN_END)));
			} while(c.moveToNext());
		}
		db.close();
		startServ(v);
	}*/
}
