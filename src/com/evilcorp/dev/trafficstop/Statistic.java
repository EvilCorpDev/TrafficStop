package com.evilcorp.dev.trafficstop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioGroup;

public class Statistic extends Activity implements OnGestureListener {
	
	final int DIALOG_ID = 1;
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
	Diagram d;
	DB db;
	SharedPreferences sPref;
	Context context;
	GestureDetector gestureScanner;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.overridePendingTransition(R.anim.slite_in_left,R.anim.slide_out_left);
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistic);
		
		gestureScanner = new GestureDetector(this);
		
		d = new Diagram();
		db = new DB(this);
		db.open();
		
		sPref = PreferenceManager.getDefaultSharedPreferences(this);
		context = this;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		db.close();
	}
	
	@SuppressWarnings("deprecation")
	public void clear_data(View v) {
		showDialog(DIALOG_ID);
	}
	
	public void make_stat(View v) {
		switch(v.getId()) {
		case R.id.mkBtn:
			RadioGroup rGroup = (RadioGroup)findViewById(R.id.rGroup);
			int id = rGroup.getCheckedRadioButtonId();
			Intent intent = new Intent();
			switch(id) {
			case R.id.rBtnWeek:
				HashMap<String, ArrayList<Double>> dataW = getData(7);
				intent = d.getIntent(this, getResources().getString(R.string.week_traff), getArray(dataW.get("period")), 
						getArray(dataW.get("traff")), new double[] { -20, 20, -20, 500 } , 
						getResources().getString(R.string.days), getResources().getString(R.string.traff_usage), Color.BLUE);
				break;
			case R.id.rBtnMonth:
				HashMap<String, ArrayList<Double>> dataM = getData(30);
				intent = d.getIntent(this, getResources().getString(R.string.month_traff), getArray(dataM.get("period")), 
						getArray(dataM.get("traff")), new double[] { -20, 40, -20, 5000 } , 
						getResources().getString(R.string.days), getResources().getString(R.string.traff_usage), Color.YELLOW);
				break;
			case R.id.rBtnSixMonth:
				HashMap<String, ArrayList<Double>> dataSM = getData(180);
				intent = d.getIntent(this, getResources().getString(R.string.month_traff), getArray(dataSM.get("period")), 
						getArray(dataSM.get("traff")), new double[] { -20, 200, -10, 10000 } , 
						getResources().getString(R.string.months), getResources().getString(R.string.traff_usage), Color.RED);
				break;
			case R.id.rBtnYear:
				HashMap<String, ArrayList<Double>> dataY = getData(365);
				intent = d.getIntent(this, getResources().getString(R.string.year_traff), getArray(dataY.get("period")), 
						getArray(dataY.get("traff")), new double[] { -20, 375, -20, 2000 } , 
						getResources().getString(R.string.months), getResources().getString(R.string.traff_usage), Color.GREEN);
				break;
			}
			
			startActivity(intent);
		}
	}
	
	@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(int id) {
		if(id == DIALOG_ID) {
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			
			adb.setTitle(R.string.clear_data);
			
			adb.setMessage(R.string.message);
			
			adb.setPositiveButton(getResources().getString(R.string.yes), myClickListener);
			
			adb.setNegativeButton(getResources().getString(R.string.no), myClickListener);
			
			return adb.create();
		}
		return super.onCreateDialog(id);
	}
	
	OnClickListener myClickListener = new OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch(which) {
			case Dialog.BUTTON_POSITIVE :
				db.clearTable();
				stopService(new Intent(context, TrafficConrolService.class));
				int mBytes = Integer.parseInt(sPref.getString("pref_Mbytes", "20"));
				startService(new Intent(context, TrafficConrolService.class).putExtra("mBytes", mBytes));
				break;
			case Dialog.BUTTON_NEGATIVE:
				break;
			}
		}
	};
	
	private HashMap<String, ArrayList<Double>> getData(int period) {
		Cursor c = db.getAllData();
		if(c.moveToFirst()) {
			return getLastData(c, period);
		}
		
		return null;
	}
	
	private HashMap<String, ArrayList<Double>> getLastData(Cursor c, int period) {
		
		ArrayList<Double> days = new ArrayList<Double>();
		ArrayList<Double> traff = new ArrayList<Double>();		
		c.moveToLast();
		int count = 0;
		
		do {
			days.add(0, (double) ++count);
			double d = c.getDouble(c.getColumnIndex(DB.COLUMN_END)) - c.getDouble(c.getColumnIndex(DB.COLUMN_START));
			d /=TrafficConrolService.BytesCount;
			traff.add(0, d);
			
		}while(c.moveToPrevious() && count != period);
		
		if(period == 180 || period == 365) {
			traff = (ArrayList<Double>) getMonths(traff);
			days.clear();
			for(int i = 0; i < traff.size(); i++) {
				days.add((double) (i + 1));
			}
		}
		
		HashMap<String, ArrayList<Double>> hm = new HashMap<String, ArrayList<Double>>();
		hm.put("period", days);
		hm.put("traff", traff);
		
		for(int i = 0; i < traff.size(); i++) {
		}
		
		c.close();
		return hm;
		
	}
	
	private List<Double> getMonths(List<Double> l) {
		ArrayList<Double> months = new ArrayList<Double>();
		double sum = 0;
		
		for(int i = 1; i <= l.size(); i++ ) {
			sum += l.get(i - 1);
			if(i % 30 == 0 && i != 0) {
				months.add(sum);
				sum = 0;
			}
		}
		
		months.add(sum);
		
		return months;
	}

	
	private double[] getArray(List<Double> l) {
		double[] arr = new double[l.size()];
		for(int i = 0; i < l.size(); i++) {
			arr[i] = l.get(i);
		}
		return arr;
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
            if (e1.getX() < e2.getX() && e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE 
            		&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            	Intent slideactivity = new Intent(this, TrafficControl.class);
            	TrafficControl.runActivity = 1;
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
		super.onBackPressed();
		Intent slideactivity = new Intent(this, TrafficControl.class);
    	TrafficControl.runActivity = 1;
    	startActivity(slideactivity);
	}
	
	public void onLongPress(MotionEvent e){}
	
	public boolean onScroll(MotionEvent e1,MotionEvent e2,float distanceX,float distanceY){
		return true;
	}
	public void onShowPress(MotionEvent e){}
	public boolean onSingleTapUp(MotionEvent e){ return true;}

}
