/*
 * MediaPlayerService handles ALL music playback
 * ...Uses AIDL and CallbackInterface (MPSInterface.aidl & IMPSCallbackInterface.aidl)
 * so that the service and the activities are closely binded and have a 2-way communication path
 */
package sg.ruqqq.socialtunes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import sg.ruqqq.socialtunes.adapter.DbAdapter;
import sg.ruqqq.socialtunes.item.Song;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class MediaPlayerService extends Service {

	// main vars for playback controls and informations
	private MediaPlayer mp = new MediaPlayer();
	private List<Song> songs = new ArrayList<Song>();
	private Song currentSong = null;
	private int currentPosition = 0;

	// not implemented
	private boolean shuffle = false;
	private int repeatMode = 0;
	
	// for locations
	private LocationManager lm;
	private MyLocationListener ll;
	private Location mLocation;
	
	// for notifications
	private NotificationManager nm;
	private static final int NOTIFY_ID = 1;
	
	// for recording recent songs
	DbAdapter mDbAdapter;
	
	/**
     * This is a list of callbacks that have been registered with the
     * service.  Note that this is package scoped (instead of private) so
     * that it can be accessed more efficiently from inner classes.
     */
    final RemoteCallbackList<IMPSCallbackInterface> mCallbacks
            = new RemoteCallbackList<IMPSCallbackInterface>();

	@Override
	public void onCreate() {
		super.onCreate();
		// gets locationmanager
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		// instantiate our locationlistener (see bottom)
		ll = new MyLocationListener();
		
		Log.d(getPackageName(), "Finding Location...");
		// Try to find location with GPS
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 30, ll);
		// ...if within 15 seconds there's no lock on, fall back to network provide
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){
			@Override
			public void run() {
				Log.d(getPackageName(), "Reverting to network for Location update");
				// needs to be done in handler because Timer doesn't run in main thread
				mHandler.sendEmptyMessage(SWITCH_TO_NETWORK_LOCATION); 
			}
		}, 15000);
		
		// get notificationmanager
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		// opens database for writing
		mDbAdapter = new DbAdapter(this);
		mDbAdapter.open();
	}

	@Override
	public void onDestroy() {
		// service is no longer needed
		
		// close database
		mDbAdapter.close();
		
		// remove location updates to conserve battery
		lm.removeUpdates(ll);
		ll = null;
		
		// stop music playing and release lock on mediaplayer
		// ...remove notification too
		mp.stop();
		mp.release();
		nm.cancel(NOTIFY_ID);
		
		// kill all our callbacks
		mCallbacks.kill();
	}
	
	// method to submit song to webservice
	// relies on Webservice.java
	private void submitSong(boolean withLocation) {
		if (withLocation && mLocation != null) {
			Webservice.submitSong(currentSong.getTitle(), currentSong.getArtist(), currentSong.getAlbum(), mLocation.getLatitude(), mLocation.getLongitude());
		} else {
			Webservice.submitSong(currentSong.getTitle(), currentSong.getArtist(), currentSong.getAlbum());
		}
	}

	/*
	 * ALL MEDIA CONTROL METHODS
	 */
	private void playSong(Song s, boolean reset) {
		try {
			// Show an ONGOING notification that we're playing a song
			Notification notification = new Notification(R.drawable.indicator_ic_mp_playing_large, "Now Playing: "+s.getTitle(), System.currentTimeMillis());
			Intent notificationIntent = new Intent(this, MediaPlayerActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			notification.setLatestEventInfo(this, s.getTitle(), s.getArtist(), contentIntent);
			notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
			nm.notify(NOTIFY_ID, notification);

			// if there isn't any song playing
			// OR if the current song isn't the selected song
			// OR we're forced to restart the current song
			if (currentSong == null || reset == true || (currentSong != null && currentSong.getId() != s.getId())) {
				currentSong = s;
				
				// initialize our mp and play the selected song
				mp.reset();
				mp.setDataSource(s.getFilename());
				mp.prepare();
				mp.start();
	
				mDbAdapter.pushSong(s.getId()); // save the id in db for getting recent songs
				
				// set the action on what to do after this song ends
				// ..go to next song
				mp.setOnCompletionListener(new OnCompletionListener() {
					public void onCompletion(MediaPlayer arg0) {
						nextSong();
					}
				});
				
				// Submit song to Webservice
				// ...runs in a thread because it might take a long time
		        Thread threadSubmit = new Thread(new Runnable() {
		    		public void run() {
		    			submitSong(true);
		    		}
		    	}, "songSubmitterThread");
		        threadSubmit.start();
			} else {
				// else, just resume the currently playing song
				mp.start();
			}
			
			// Call our PLAYPOS_CHANGED callback to notify activities that the seek has changed
			mHandler.sendEmptyMessage(PLAYPOS_CHANGED);

		} catch (IOException e) {
			Log.e(getString(R.string.app_name), e.getMessage());
		}
		
		// Call our SONG_CHANGED callback to notify activities that the song has changed
		mHandler.sendMessage(mHandler.obtainMessage(SONG_CHANGED, songs.get(currentPosition)));
	}
	
	// overload of above
	// ...play the selected song without force reset
	private void playSong(Song s) {
		playSong(s, false);
	}

	private void nextSong() {
		// Check if last song or not
		if (++currentPosition >= songs.size()) {
			// if so, stop playing
			currentPosition = 0;
			stopSong();
		} else {
			// else, go to next
			playSong(songs.get(currentPosition));
		}
	}

	// same as above except its opposite
	private void prevSong() {
		if (mp.getCurrentPosition() < 10000 && currentPosition >= 1) {
			playSong(songs.get(--currentPosition));
		} else {
			playSong(songs.get(currentPosition), true);
		}
	}
	
	// pause song and cancel any ongoing notifications
	private void pauseSong() {
		nm.cancel(NOTIFY_ID);
		mp.pause();
	}
	
	// 'stop' song: pause the song but seek it to position 0
	private void stopSong() {
		mp.seekTo(0);
		nm.cancel(NOTIFY_ID);
		mp.pause();
		mHandler.sendMessage(mHandler.obtainMessage(SONG_CHANGED, null));
	}
	
	// for manual seeking to certain position of the song
	private void seekTo(int ms) {
		mp.seekTo(ms);
	}

	// Our interface for the activities to call the Service actions
	// ... most methods are just actions as wrapper to above methods
	private final MPSInterface.Stub mBinder = new MPSInterface.Stub() {
		public void registerCallback(IMPSCallbackInterface cb) {
            if (cb != null) mCallbacks.register(cb);
        }
		
        public void unregisterCallback(IMPSCallbackInterface cb) {
            if (cb != null) mCallbacks.unregister(cb);
        }
		
		public void playFile(int position) throws DeadObjectException {
			try {
				currentPosition = position;
				playSong(songs.get(position));
			} catch (IndexOutOfBoundsException e) {
				Log.e(getString(R.string.app_name), e.getMessage());
			}
		}

		public void addSongPlaylist(Song song) throws DeadObjectException {
			songs.add(song);
			//Log.d(getPackageName(), "Songs Added To Playlist: "+ song);
		}
		
		public void addPlaylist(List<Song> songslist) throws DeadObjectException {
			songs.addAll(songslist);
			/*int i = 0;
			for (Song s : songs) {
				Log.d(getPackageName(), i+": Songs Added To Playlist: "+ s);
				i++;
			}*/
		}
		
		public List<Song> getPlaylist() {
			return songs;
		}

		public void clearPlaylist() throws DeadObjectException {
			/*if (mp.isPlaying()) {
				Song s = songs.get(currentPosition);
				songs.clear();
				songs.add(s);
			} else {*/
				songs.clear();
			//}
		}

		public void skipBack() throws DeadObjectException {
			prevSong();
		}

		public void skipForward() throws DeadObjectException {
			nextSong();
		}

		public boolean togglePlay() throws DeadObjectException {
			if (mp.isPlaying()) {
				pauseSong();
				return false;
			} else {
				playSong(songs.get(currentPosition));
				return true;
			}
		}
		
		public boolean isPlaying() throws DeadObjectException {
			return mp.isPlaying();
		}
		
		public void pause() throws DeadObjectException {
			pauseSong();
		}

		public void stop() throws DeadObjectException {
			stopSong();
		}

		public Song currentSong() throws DeadObjectException {
			if (currentSong != null)
				return currentSong;
			else
				return null;
		}
		
		// Song seeking methods/wrappers
		public int currentPosition() throws DeadObjectException {
			return mp.getCurrentPosition();
		}
		
		public void setCurrentPosition(int position) throws DeadObjectException {
			seekTo(position);
		}
		
		// gets user current location if we're already locked on
		public double[] getCurrentLocation() throws DeadObjectException {
			if (mLocation != null) {
				double[] location = {mLocation.getLatitude(), mLocation.getLongitude()};
				return location;
			} else {
				return null;
			}
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	/*
	 * BELOW ARE THE CALLBACK METHODS FOR THE SERVICE TO TALK TO THE CONNECTED CLIENTS(ACTIVITIES)
	 */
    public static final int SONG_CHANGED = 1;
    public static final int PLAYPOS_CHANGED = 2;
    private static final int SWITCH_TO_NETWORK_LOCATION = 3; // INTERNAL USE

    /**
     * Our Handler used to execute operations on the main thread.  This is used
     * to schedule increments of our value.
     */
    private final Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case SONG_CHANGED: {
                	// Log.d(getPackageName(), "SENDING BROADCAST FOR SONG CHANGE");
                    // Broadcast to all clients the new value.
                    final int N = mCallbacks.beginBroadcast();
                    for (int i=0; i<N; i++) {
                        try {
                        	if (msg.obj instanceof Song)
                        		mCallbacks.getBroadcastItem(i).songChanged((Song) msg.obj);
                        	else
                        		mCallbacks.getBroadcastItem(i).songChanged(null);
                        } catch (RemoteException e) {
                            // The RemoteCallbackList will take care of removing
                            // the dead object for us.
                        }
                    }
                    mCallbacks.finishBroadcast();
                } break;
                case PLAYPOS_CHANGED: {
                	// Log.d(getPackageName(), "SENDING BROADCAST FOR PLAYPOS CHANGED");
                    // Broadcast to all clients the new value.
                    final int N = mCallbacks.beginBroadcast();
                    for (int i=0; i<N; i++) {
                        try {
                        	mCallbacks.getBroadcastItem(i).playposChanged(mp.getCurrentPosition());
                        } catch (RemoteException e) {
                            // The RemoteCallbackList will take care of removing
                            // the dead object for us.
                        }
                    }
                    mCallbacks.finishBroadcast();
                    
                    if (mp.isPlaying()) mHandler.sendEmptyMessageDelayed(PLAYPOS_CHANGED, 1000);
                } break;
                case SWITCH_TO_NETWORK_LOCATION: {
                	Log.d(getPackageName(), "SWITCH_TO_NETWORK_LOCATION (Location) called");
                	lm.removeUpdates(ll);
    				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 30, ll);
                } break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
    
    // Location Listener Class
	private class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location l) {
			Log.d(getPackageName(), "Current Location Received: "+"Latitude: "+l.getLatitude()+"; Longitude: "+l.getLongitude());
			mLocation = l;
			Toast toast = Toast.makeText(MediaPlayerService.this, "Location found", Toast.LENGTH_SHORT);
	    	toast.show();
			lm.removeUpdates(ll);
		}

		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
		}

		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
