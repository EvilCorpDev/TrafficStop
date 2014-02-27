package com.evilcorp.dev.trafficstop;

import java.util.Calendar;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.TrafficStats;

public class DB {
	
	private static final String DB_NAME = "trafficStats";
	private static final int DB_VERSION = 1;
	private static final String DB_TABLE = "trafficOfDays";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_START = "start";
	public static final String COLUMN_END = "end";
	
	public static final String DB_CREATE = 
			"create table " + DB_TABLE + "(" +
				COLUMN_ID + " integer primary key autoincrement, " +
				COLUMN_DATE + " text, " +
				COLUMN_START + " real, " +
				COLUMN_END + " real);";
	
	private final Context ctx;
	
	private DBHelper mDBHelper;
	private SQLiteDatabase db;
	
	public DB(Context context) {
		ctx = context;
	}
	
	public void open() {
		mDBHelper = new DBHelper(ctx, DB_NAME, null, DB_VERSION);
		db = mDBHelper.getWritableDatabase();
	}
	
	public void close() {
		if(mDBHelper != null) {
			mDBHelper.close();
		}
	}
	
	public Cursor getAllData() {
		return db.query(DB_TABLE, null, null, null, null, null, null);
	}
	
	public HashMap<String, Double> getDateData(String date) {
		Cursor c = getAllData();
		if(c.moveToFirst()) {
			do {
				if(c.getString(c.getColumnIndex(COLUMN_DATE)).equals(date)) {
					
					HashMap<String, Double> hm = new HashMap<String, Double>();
					hm.put(COLUMN_START, c.getDouble(c.getColumnIndex(COLUMN_START)));
					hm.put(COLUMN_END, c.getDouble(c.getColumnIndex(COLUMN_END)));
					hm.put("pos", (double) c.getInt(c.getColumnIndex(COLUMN_ID)));
					c.close();
					return hm;
				}
			}while(c.moveToNext());
		}
		c.close();
		return null;
	}
	
	public boolean dbIsOpen() {
		return db.isOpen();
	}
	
	public HashMap<String, Double> getLastRec() {
		Cursor c = getAllData();
		if(c.moveToFirst()) {					
			c.moveToLast();
			HashMap<String, Double> hm = new HashMap<String, Double>();
			hm.put(COLUMN_START, c.getDouble(c.getColumnIndex(COLUMN_START)));
			hm.put(COLUMN_END, c.getDouble(c.getColumnIndex(COLUMN_END)));
			hm.put("pos", (double) c.getInt(c.getColumnIndex(COLUMN_ID)));
			c.close();
			return hm;
		}
		return null;
	}
	
	public void addRec(String date, double start, double end) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_DATE, date);
		cv.put(COLUMN_START, start);
		cv.put(COLUMN_END, end);
		
		db.insert(DB_TABLE, null, cv);
	}
	
	public void delRec(long id) {
		db.delete(DB_TABLE, COLUMN_ID + " = " + id, null);
	}
	
	public void updateRec(String date, double end) {
		HashMap<String, Double> hm = getDateData(date);
		if(hm == null) {
			return;
		}
		double start = hm.get("start");
		double pos = hm.get("pos");
		
		end += start;

		ContentValues cv = new ContentValues();
		cv.put(COLUMN_DATE, date);
		cv.put(COLUMN_START, start);
		cv.put(COLUMN_END, end);
		
		String where = COLUMN_ID + " =  ? ";
		db.update(DB_TABLE, cv, where, new String[] { Double.toString(pos)});
	}
	
	public void clearTable() {
		db.delete(DB_TABLE, null, null);
	}
	
	private class DBHelper extends SQLiteOpenHelper {
		
		public DBHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(DB_CREATE);
			
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, 0);
			
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_DATE, c.get(Calendar.DATE) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.YEAR));
			double start = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
			cv.put(COLUMN_START, start);
			cv.put(COLUMN_END, start);
			db.insert(DB_TABLE, null, cv);
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}
	}
}
