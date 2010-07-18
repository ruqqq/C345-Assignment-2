/*
 * Widgets for this app
 * ...pain in the ass to do
 * layout/xml is set to 0 refresh
 * widget updating is done by AlarmManager
 */
package sg.ruqqq.socialtunes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

public class SocialTunesAppWidget extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {	
		// Set the initial data
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		views.setTextViewText(R.id.tvInfo, "Loading data...");
		appWidgetManager.updateAppWidget(appWidgetIds, views);
		
		// Run the service for updating widget
		Intent svc = new Intent(context, SocialTunesAppWidgetService.class);
		context.startService(svc);
	}
	
	@Override
	public void onDisabled(Context context) {
		// Disable alarm when there's no more widget left
		setAlarm(context, false);
		
		super.onDisabled(context);
	}
	
	// Method for setting the repeating alarm on and off
	private void setAlarm(Context context, boolean onOrOff) {
		AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE); // The broadcast to use
		PendingIntent pi = PendingIntent.getBroadcast(
			    context.getApplicationContext(), 0, i, 
			    PendingIntent.FLAG_UPDATE_CURRENT);
		
		if (onOrOff) {
			// Start
			Log.d(context.getPackageName(), "Set next alarm");
			// Set time to wake up the alarm (30 minutes)
			mgr.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + (30*60*1000), pi);
		} else {
			// Stop
			Log.d(context.getPackageName(), "Stopping repeating alarm");
			mgr.cancel(pi);
		}
	}
	
	// Handle received broadcast
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		Log.d(context.getPackageName(), "OnReceive: "+intent.getAction()+" vs "+SocialTunesAppWidgetService.UPDATEALERT);
		if (intent.getAction().equals(SocialTunesAppWidgetService.UPDATEALERT)) {
			// Update all the widget when we receive data from service
			ComponentName thisWidget = new ComponentName(context, SocialTunesAppWidget.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
			
			Bundle b = intent.getExtras();
			String title = b.getString("song_title");
			String artist = b.getString("song_artist");
			
			Log.d(context.getPackageName(), "Song: "+title+"; Artist: "+artist);
			
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
			views.setTextViewText(R.id.tvInfo, "Most Popular Song Today:");
			views.setTextViewText(R.id.tvSong, title);
			views.setTextViewText(R.id.tvArtist, artist);
			appWidgetManager.updateAppWidget(appWidgetIds, views);
			Log.d(context.getPackageName(), "Widgets updated");
		} else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
			// Set the alarm again after each update
			setAlarm(context, true);
		}
	}
	
}
