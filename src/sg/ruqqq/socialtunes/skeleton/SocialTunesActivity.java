package sg.ruqqq.socialtunes.skeleton;

import sg.ruqqq.socialtunes.DashboardActivity;
import sg.ruqqq.socialtunes.IMPSCallbackInterface;
import sg.ruqqq.socialtunes.MPSInterface;
import sg.ruqqq.socialtunes.MediaPlayerActivity;
import sg.ruqqq.socialtunes.MediaPlayerService;
import sg.ruqqq.socialtunes.R;
import sg.ruqqq.socialtunes.SongsActivity;
import sg.ruqqq.socialtunes.item.Song;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

public class SocialTunesActivity extends Activity {
	protected MPSInterface mpInterface;
	private boolean showWallpaper = false;
	private boolean showWallpaperTitlebar = false;
	
	// To handle the special layout we use yet accompanying default behavior of setTitle
	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		
		// Set activity title
		if (findViewById(R.id.tvTitle) != null) {
			((TextView) findViewById(R.id.tvTitle)).setText(title);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		// Set default behavior of Home icon on title bar: Go back Dashboard
		if (findViewById(R.id.ibHome) != null) {
			((ImageButton) findViewById(R.id.ibHome)).setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					Intent homeIntent = new Intent(v.getContext(), DashboardActivity.class);
					homeIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(homeIntent);
					finish();
				}
	        });
		}
		
		// Set default behavior of Now Playing bar: Go to MediaPlayer
		if (findViewById(R.id.nowplaying) != null) {
			((View) findViewById(R.id.nowplaying)).setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					Intent homeIntent = new Intent(v.getContext(), MediaPlayerActivity.class);
					homeIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(homeIntent);
				}
	        });
		}
	}
	
	@Override
    public void onResume() {
    	super.onResume();
    	// if we're not binded to the service, auto bind/create to it
    	if (mpInterface == null) {
    		getApplicationContext().bindService(new Intent(this, MediaPlayerService.class),
					mConnection,
					Context.BIND_AUTO_CREATE);
    	} else {
    		// else, just call updateNowPlaying to refresh data/ui
    		updateNowPlaying();
    	}
    	
    	// special background effects
    	if (showWallpaper) {
    		Bitmap b = getWallpaperSection(getWallpaper());
	    	if (b != null) {
	    		BitmapDrawable bd = new BitmapDrawable(b);
	    		((ViewGroup) findViewById(R.id.titlebar).getParent()).setBackgroundResource(R.drawable.radial_background);
	    		((ViewGroup) findViewById(R.id.titlebar).getParent().getParent()).setBackgroundDrawable(bd);
	    	}
    	} else {
    		((ViewGroup) findViewById(R.id.titlebar).getParent().getParent()).setBackgroundDrawable(null);
    		((ViewGroup) findViewById(R.id.titlebar).getParent()).setBackgroundDrawable(null);
    	}
    	
    	if (showWallpaperTitlebar) {
    		Bitmap b = getWallpaperSection(getWallpaper());
	    	if (b != null) {
	    		BitmapDrawable bd = new BitmapDrawable(b);
	    		((ViewGroup) findViewById(R.id.titlebar).getParent()).setBackgroundResource(R.drawable.top_background);
	    		((ViewGroup) findViewById(R.id.titlebar).getParent().getParent()).setBackgroundDrawable(bd);
	    	}
    	} else {
    		if (!showWallpaper) {
    			((ViewGroup) findViewById(R.id.titlebar).getParent().getParent()).setBackgroundDrawable(null);
    			((ViewGroup) findViewById(R.id.titlebar).getParent()).setBackgroundDrawable(null);
    		}
    	}
    }
	
	public void setShowWallpaper(boolean show) {
		showWallpaper = show;
	}
	
	public void setShowWallpaperTitlebar(boolean show) {
		showWallpaperTitlebar = show;
	}
	
	// special background effects code
	// ...get from Home wallpaper
	public Bitmap getWallpaperSection(Drawable d){
		Bitmap mWallpaper;
		
		if (d instanceof BitmapDrawable) {
            mWallpaper = ((BitmapDrawable) d).getBitmap();
        } else {
            //throw new IllegalStateException("The wallpaper must be a BitmapDrawable.");
        	return null;
        }
        Paint paint = new Paint();

        Display display = getWindowManager().getDefaultDisplay(); 
        int width = display.getWidth();
        int height = display.getHeight();
            
    	float percent = (float)2/(float)3;
    	float x = (float)(d.getIntrinsicWidth()/2)*percent;
        float y = (d.getIntrinsicHeight()-height)/2;
        
        Bitmap b = Bitmap.createBitmap((int) width, (int) height,
                Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(b);
        canvas.drawARGB(255, 0, 255, 0);
        Rect src = new Rect((int)x, (int)y, (int)x+width, (int)y+height);
        Rect dst = new Rect(0, 0, width, height);
		canvas.drawBitmap(mWallpaper, src, dst, paint);
        
		return b;
    }
	
	// see above
	public Bitmap getWallpaperSectionTitlebar(Drawable d){
		Bitmap mWallpaper;
		
		if (d instanceof BitmapDrawable) {
            mWallpaper = ((BitmapDrawable) d).getBitmap();
        } else {
            //throw new IllegalStateException("The wallpaper must be a BitmapDrawable.");
        	return null;
        }
        Paint paint = new Paint();

        Display display = getWindowManager().getDefaultDisplay();
        //((ViewGroup) findViewById(R.id.titlebar)).measure(widthMeasureSpec, heightMeasureSpec);
        int width = display.getWidth();
        int height = 45;
            
    	float percent = (float)2/(float)3;
    	float x = (float)(d.getIntrinsicWidth()/2)*percent;
        float y = 0;
        
        Bitmap b = Bitmap.createBitmap((int) width, (int) height,
                Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(b);
        canvas.drawARGB(255, 0, 255, 0);
        Rect src = new Rect((int)x, (int)y, (int)x+width, (int)y+height);
        Rect dst = new Rect(0, 0, width, height);
		canvas.drawBitmap(mWallpaper, src, dst, paint);
        
		return b;
    }
	
	// Create Option Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	    super.onCreateOptionsMenu(menu);
	    // Create and add our menu items
	    // SHow only if we're not at Dashboard
	    if (!getComponentName().getClassName().equals("sg.ruqqq.socialtunes.DashboardActivity")) {
		    MenuItem itemHome = menu.add(0, 0, Menu.NONE, "Home");
		    itemHome.setIcon(R.drawable.ic_menu_home);
	    }
	    
	    // Show only if we're not in the Now Playing screen
	    if (getIntent().getIntExtra("playlist_id", -1) != -2) {
		    MenuItem itemList = menu.add(0, 1, Menu.NONE, "Now Playing");
		    itemList.setIcon(R.drawable.ic_menu_play_clip);
	    }
	    
	    return true;
    }
    
    // Handles Option Menu Selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    super.onOptionsItemSelected(item);
	    switch (item.getItemId()) {
	    	case (0):
	    		Intent homeIntent = new Intent(this, DashboardActivity.class);
				homeIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(homeIntent);
				finish();
	    		break;
	    	case (1):
	    		Intent i = new Intent(this, SongsActivity.class);
				i.putExtra("playlist", "Now Playing");
				i.putExtra("playlist_id", -2);
				i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(i);
	            break;
	    }
	    
	    return true;
    }
	
	// to manually update the current playing song data and call releveant UI update routines
	protected void updateNowPlaying() {
		// Now Playing Data
        Song currentSong = null;
        try {
        	currentSong = mpInterface.currentSong();
        } catch(RemoteException e) {
			Log.e(getPackageName(), e.getMessage());
		}
        
        // Update
        updateNowPlaying(currentSong, false);
	}
	
	protected void updateNowPlaying(Song currentSong) {
		updateNowPlaying(currentSong, true);
	}
	
	// Callback method
	// make sure every inheriting activity has the current playing song data
	protected void updateNowPlaying(Song currentSong, boolean animate) {
		if (currentSong != null) {
			if (((View) findViewById(R.id.nowplaying)).getVisibility() != View.VISIBLE) {
				if (animate) {
					Animation animation = AnimationUtils.loadAnimation(this,
		                    R.anim.slide_up);
					((View) findViewById(R.id.nowplaying)).startAnimation(animation);
				}
				((View) findViewById(R.id.nowplaying)).setVisibility(View.VISIBLE);
			}
        	((TextView) findViewById(R.id.title)).setText(currentSong.getTitle());
        	((TextView) findViewById(R.id.artist)).setText(currentSong.getArtist());
        } else {
        	if (((View) findViewById(R.id.nowplaying)).getVisibility() != View.GONE) {
        		if (animate) {
		        	Animation animation = AnimationUtils.loadAnimation(this,
		                    R.anim.slide_down);
					((View) findViewById(R.id.nowplaying)).startAnimation(animation);
        		}
	        	((View) findViewById(R.id.nowplaying)).setVisibility(View.GONE);
        	}
        }
	}
	
	// Other callbacks:
	// 1. When song seek changes
	// 2. On bind of service
	protected void playposChanged(int ms) {
		
	}
	
	protected void loadData() {
		
	}
	
	private ServiceConnection mConnection = new ServiceConnection()
    {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mpInterface = MPSInterface.Stub.asInterface((IBinder)service);
			
			// We want to monitor the service for as long as we are
            // connected to it.
            try {
            	mpInterface.registerCallback(mCallback);
            	Log.d(getPackageName(), "Registered Callback");
            	loadData(); // Load the data for each activity once we're connected to the service
            	updateNowPlaying(); // Updates now playing data
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
		}

		public void onServiceDisconnected(ComponentName className) {
			mpInterface = null;
		}
    };
	
	// ----------------------------------------------------------------------
    // Code dealing with callbacks from Service
    // ----------------------------------------------------------------------
    /**
     * This implementation is used to receive callbacks from the remote
     * service.
     */
    private IMPSCallbackInterface mCallback = new IMPSCallbackInterface.Stub() {
		public void songChanged(Song s) throws RemoteException {
			mHandler.sendMessage(mHandler.obtainMessage(MediaPlayerService.SONG_CHANGED, s));
		}
		
		public void playposChanged(int ms) throws RemoteException {
			mHandler.sendMessage(mHandler.obtainMessage(MediaPlayerService.PLAYPOS_CHANGED, ms, 0));
		}
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MediaPlayerService.SONG_CHANGED:
                	//Log.d(getPackageName(), "mHandler SONG_CHANGED");
                	updateNowPlaying((Song) msg.obj);
                    break;
                case MediaPlayerService.PLAYPOS_CHANGED:
                	//Log.d(getPackageName(), "mHandler PLAYPOS_CHANGED");
                	playposChanged(msg.arg1);
                	break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
}
