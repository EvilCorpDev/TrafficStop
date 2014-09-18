package com.evilcorp.dev.trafficstop;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class TrafficWidget extends AppWidgetProvider {

	BroadcastReceiver br;
	SharedPreferences sdPref;

	public static String OPEN_APP = "com.evilcorp.dev.trafficcontrol";

	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		super.onEnabled(context);
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		ComponentName thisAppWidget = new ComponentName(
				context.getPackageName(), getClass().getName());
		int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
		for (int appWidgetID : ids) {
			updateText(context, appWidgetManager, appWidgetID, 0, 0, 0);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
		String action = intent.getAction();
		if (action.equals(TrafficControl.BROADCAST_ACTION)) {
			ComponentName thisAppWidget = new ComponentName(
					context.getPackageName(), getClass().getName());
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
			for (int appWidgetID : ids) {
				updateText(context, appWidgetManager, appWidgetID,
						intent.getDoubleExtra("traff", 0),
						intent.getDoubleExtra("allTraff", 0),
						intent.getDoubleExtra("mBytes", 0));
			}
		} else if (action.equals(OPEN_APP)) {
			context.startActivity(new Intent(context, TrafficControl.class));
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.d("myLogs", "onUpdate");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, 0);
		DB db = new DB(context);
		db.open();
		HashMap<String, Double> hm2 = db.getDateData(c.get(Calendar.DATE) + "-"
				+ (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.YEAR));
		sdPref = PreferenceManager.getDefaultSharedPreferences(context);
		int mBytes = Integer.parseInt(sdPref.getString("pref_Mbytes", "20"));
		for (int id : appWidgetIds) {
			updateText(context, appWidgetManager, id, hm2.get("end"),
					hm2.get("end"), mBytes);
		}
	}

	public void updateText(Context context, AppWidgetManager appWidgetManager,
			int widgetID, double traff, double allTraff, double mBytes) {
		RemoteViews widgetView = new RemoteViews(context.getPackageName(),
				R.layout.widget);
		widgetView.setTextViewText(R.id.tvInfoDigit, NumberFormat.getInstance()
				.format(mBytes - traff) + ", MB");
		widgetView.setTextViewText(R.id.tvUsed, context.getResources()
				.getString(R.string.widget_used)
				+ NumberFormat.getInstance().format(traff) + ", MB");
		widgetView.setTextViewText(R.id.tvAll, context.getResources()
				.getString(R.string.widget_all)
				+ NumberFormat.getInstance().format(allTraff) + ", MB");

		Intent intent = new Intent(context, TrafficControl.class);
		PendingIntent pIntent = PendingIntent.getActivity(context, widgetID,
				intent, 0);
		widgetView.setOnClickPendingIntent(widgetID, pIntent);

		appWidgetManager.updateAppWidget(widgetID, widgetView);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onDeleted(context, appWidgetIds);
		Log.d("myLogs", "onDeleted Widget" + Arrays.toString(appWidgetIds));
	}

	@Override
	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		super.onDisabled(context);
		Log.d("myLogs", "onDisabled Widget");
	}
}
