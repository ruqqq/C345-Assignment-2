/*
 * Service for Widget
 * ..does nothing other than update the widget and kill itself
 * it is being started by AlarmManager after a period - see SocialTunesAppWidget.java
 */
package sg.ruqqq.socialtunes;

import java.util.ArrayList;

import sg.ruqqq.socialtunes.item.Trend;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SocialTunesAppWidgetService extends Service {
	public static final String UPDATEALERT = "sg.ruqqq.socialtunes.APPWIDGET_UPDATE";
	Trend trend;
	
	@Override
	public void onCreate() {
		Log.d(getPackageName(), "Widget Service Running");
		// Update in thread so it doesn't hang the main application
		// .. since it involves getting data from net
		Thread threadLoading = new Thread(new Runnable() {
    		public void run() {
    			loadRecentSongs();
    			
    			if (trend != null)
    				updateWidget();
    		}
    	}, "loader");
        threadLoading.start();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(getPackageName(), "Widget Service Destroyed");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void updateWidget() {
		Log.d(getPackageName(), "Updating Widgets");
		// Create the IntentFilter
		Intent bIntent = new Intent(UPDATEALERT);
		bIntent.putExtra("song_title", trend.getSong());
		bIntent.putExtra("song_artist", trend.getArtist());
		bIntent.putExtra("song_count", trend.getPlaycount());
		
		// Send the Intent Broadcast
		sendBroadcast(bIntent);
		stopSelf();
	}
	
	private void loadRecentSongs() {
		Log.d(getPackageName(), "Getting data...");
		// Get data from Webservice
		ArrayList<Trend> recent_songs_from_web = Webservice.recentPopularSongs(86400, 1);
		if (recent_songs_from_web.size() > 0) {
			trend = recent_songs_from_web.get(0);
		}
	}

}
