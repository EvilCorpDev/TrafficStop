package com.evilcorp.dev.trafficstop;

import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TrafficControl extends Activity implements OnSharedPreferenceChangeListener, OnGestureListener{
	
	public static final String LOG_TAG = "myLogs";
	public static final String BROADCAST_ACTION = "com.evilcorp.dev.broadcast";
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    final int DIALOG = 1;
	
	double mBytes;
	boolean isEnable;
	int startCount;
	
	TextView tvMain;
	TextView tvLimit;
	TextView tvProgress;
	ImageView imView;
	ProgressBar pbLimit;
	Controls ctr;
	SharedPreferences sdPref;
	Timer t;
	TimerTask tt;
	BroadcastReceiver br;
	GestureDetector gestureScanner;
	public static int runActivity;
	
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(runActivity == 1) {
			this.overridePendingTransition(R.anim.slite_in_right,R.anim.slide_out_right);
		}
		else if(runActivity == 2) {
			this.overridePendingTransition(R.anim.slite_in_left,R.anim.slide_out_left);
		}
		setContentView(R.layout.main);
		
		initialize();
				 
		startService(new Intent(this, TrafficConrolService.class).putExtra("mBytes", mBytes));
		
		br = new BroadcastReceiver() {
			public void onReceive(android.content.Context context, Intent intent) {
				updateText(intent.getDoubleExtra("traff", 0), intent.getDoubleExtra("allTraff", 0));
			}
		};
		
		IntentFilter iFilter = new IntentFilter(BROADCAST_ACTION);
		
		registerReceiver(br, iFilter);
		
		sdPref.edit().putInt("startCount", startCount++);
		
		if(!sdPref.getBoolean("never", false) && !sdPref.getBoolean("later", false)) {
			showDialog(DIALOG);
		}
		else if(sdPref.getBoolean("later", false)) {
			Editor ed = sdPref.edit();
		    ed.putBoolean("later", false);
		    ed.commit();  
		}
	}
	
	@SuppressWarnings("deprecation")
	private void initialize() {
		
		gestureScanner = new GestureDetector(this);

		sdPref = PreferenceManager.getDefaultSharedPreferences(this);
		startCount = sdPref.getInt("startCount", 0);
		mBytes = Integer.parseInt(sdPref.getString("pref_Mbytes", "20"));
			
		tvMain = (TextView)findViewById(R.id.tvMain);
		tvLimit = (TextView)findViewById(R.id.textLimit);
		tvProgress = (TextView)findViewById(R.id.tvProgress);
		tvLimit.setText(getResources().getString(R.string.limit_status) + mBytes + ", Mb");
		pbLimit = (ProgressBar)findViewById(R.id.pbLimit);
		pbLimit.setMax((int)mBytes);
		
		ctr = new Controls(this);
	}
	
	public void updateText(double traff, double allTraff) {
		tvMain.setText(getResources().getString(R.string.data_day) + " " + NumberFormat.getInstance().format(traff) + ", Mb"
				+ getResources().getString(R.string.all_data) +  " " + NumberFormat.getInstance().format(allTraff) + ", Mb");
		tvProgress.setText(getResources().getString(R.string.download) + " " + NumberFormat.getInstance().format((traff/mBytes)*100) + ", %");
		pbLimit.setProgress((int) Math.round(traff));
	}
	
	public void stopServ(View v) {
		stopService(new Intent(this, TrafficConrolService.class));
		Toast.makeText(this, getResources().getString(R.string.stopped_service), Toast.LENGTH_SHORT).show();
	}
	
	public void startServ(View v) {
		startService(new Intent(this, TrafficConrolService.class).putExtra("mBytes", mBytes));
		Toast.makeText(this, getResources().getString(R.string.started_service), Toast.LENGTH_SHORT).show();
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
		try {
		if(key.equals("pref_Mbytes") || key.equals("pref_foreground")) {
			stopServ(new View(this));
			mBytes = Integer.parseInt(sd.getString("pref_Mbytes", "20"));
			tvLimit.setText(getResources().getString(R.string.limit_status) + mBytes + ", Mb");
			startService(new Intent(this, TrafficConrolService.class).putExtra("mBytes", mBytes));
		}
		else if(key.equals("pref_round") || key.equals("pref_tarif")) {
			stopServ(new View(this));
			startService(new Intent(this, TrafficConrolService.class).putExtra("mBytes", mBytes));
		}
		}
		catch(Exception e) {
			Log.d(LOG_TAG, e.getMessage());
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		sdPref.registerOnSharedPreferenceChangeListener(this);
		IntentFilter iFilter = new IntentFilter(BROADCAST_ACTION);
		registerReceiver(br, iFilter);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(br);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		runActivity = 0;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent me){
		return gestureScanner.onTouchEvent(me);
	}
	public boolean onDown(MotionEvent e){
		return true;
	}
	public boolean onFling(MotionEvent e1,MotionEvent e2,float velocityX,float velocityY){
		try {
            if(e1.getX() > e2.getX() && Math.abs(e1.getX() - e2.getX()) > SWIPE_MIN_DISTANCE 
            		&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            	Intent slideactivity = new Intent(this, Statistic.class);
            	startActivity(slideactivity);
            }else if (e1.getX() < e2.getX() && e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE 
            		&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            	Intent slideactivity = new Intent(this, Preferences.class);
            	startActivity(slideactivity);
            }
        } catch (Exception e) {
            // nothing
        }
        return true;
		
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		moveTaskToBack(true);
		super.onBackPressed();
	}
	
	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		if (id == DIALOG) {
	        AlertDialog.Builder adb = new AlertDialog.Builder(this);
	        adb.setTitle(R.string.dialog_name);
	        adb.setMessage(R.string.dialog_msg);
	        adb.setIcon(R.drawable.icon);
	        adb.setPositiveButton(R.string.dialog_rate, myClickListener);
	        adb.setNegativeButton(R.string.dialog_later, myClickListener);
	        adb.setNeutralButton(R.string.dialog_never, myClickListener);
	        return adb.create();
	      }
		return super.onCreateDialog(id);
	}
	
	OnClickListener myClickListener = new OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			 switch (which) {
		      case Dialog.BUTTON_POSITIVE:
		    	  Intent intent  = new Intent();
		    	  intent.setAction(Intent.ACTION_VIEW);
		    	  intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.evilcorp.dev.trafficstop"));
		    	  startActivity(intent);
		    	  Editor ed2 = sdPref.edit();
			      ed2.putBoolean("never", true);
			      ed2.commit();
		        break;
		      case Dialog.BUTTON_NEGATIVE:
		    	  Editor ed = sdPref.edit();
		    	  ed.putBoolean("later", true);
		    	  ed.commit(); 
		        break;
		      case Dialog.BUTTON_NEUTRAL:
		    	  Editor ed1 = sdPref.edit();
			      ed1.putBoolean("never", true);
			      ed1.commit();
		        break;
		      }
		}
	};
	
	public void onLongPress(MotionEvent e){}
	
	public boolean onScroll(MotionEvent e1,MotionEvent e2,float distanceX,float distanceY){
		return true;
	}
	public void onShowPress(MotionEvent e){}
	public boolean onSingleTapUp(MotionEvent e){ return true;}

}


