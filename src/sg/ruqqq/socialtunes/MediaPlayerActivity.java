/*
 * Frontend for the MediaPlayer
 * ..only provide the controls for the music and display informations
 * the real music handling is all handled in the MediaPlayerService
 */
package sg.ruqqq.socialtunes;

import sg.ruqqq.socialtunes.item.Album;
import sg.ruqqq.socialtunes.item.Song;
import sg.ruqqq.socialtunes.skeleton.SocialTunesActivity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MediaPlayerActivity extends SocialTunesActivity {
	TextView tvAlbum, tvArtist, tvCurrentTime, tvTotalTime;
	ImageView ivAlbum;
	
	ImageButton btnPrev, btnPause, btnNext;
	SeekBar sbProgress;
	
	Song s = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mediaplayer);
		
		setTitle("Now Playing");
		setShowWallpaper(true); // See SocialTunesActivity
		((LinearLayout) findViewById(R.id.titlebar)).setBackgroundDrawable(null);
		((TextView) findViewById(R.id.tvTitle)).setGravity(Gravity.CENTER_HORIZONTAL);
		((TextView) findViewById(R.id.tvTitle)).setPadding(-50, 0, 0, 0);
		//((LinearLayout) findViewById(R.id.titlebar)).getBackground().setAlpha(50);
		
		ivAlbum = (ImageView) findViewById(R.id.ivAlbum);
		tvAlbum = (TextView) findViewById(R.id.tvAlbum);
		//tvTitle = (TextView) findViewById(R.id.tvSongTitle);
		tvArtist = (TextView) findViewById(R.id.tvArtist);
		
		btnPrev = (ImageButton) findViewById(R.id.btnPrev);
		btnPause = (ImageButton) findViewById(R.id.btnPause);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		
		tvCurrentTime = (TextView) findViewById(R.id.tvCurrentTime);
		sbProgress = (SeekBar) findViewById(R.id.sbProgress);
		tvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
		
		sbProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					try {
						mpInterface.setCurrentPosition(progress);
						tvCurrentTime.setText(Song.msToTime(progress));
					} catch (RemoteException e) {
						
					}
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		btnPrev.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				try {
					mpInterface.skipBack();
				} catch (RemoteException e) {
					
				}
			}
		});
		
		btnPause.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				try {
					if (mpInterface.togglePlay()) {
						btnPause.setImageResource(android.R.drawable.ic_media_pause);
					} else {
						btnPause.setImageResource(android.R.drawable.ic_media_play);
					}
					
				} catch (RemoteException e) {
					
				}
			}
		});
		
		btnNext.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				try {
					mpInterface.skipForward();
				} catch (RemoteException e) {
					
				}
			}
		});
		
		sbProgress.setThumb(null);
	}
	
	// Handles differently for other activities as this activity displays/controls a whole lot more information
	@Override
	protected void updateNowPlaying(Song currentSong, boolean animate) {
		if (currentSong != null) {
			// When received a currentSong data, update the UI accordingly
			if (s == null || (s != null && s.getId() != currentSong.getId())) {
				s = currentSong;
				
				//ivAlbum.setImageDrawable(currentSong);
				tvAlbum.setText(currentSong.getAlbum());
				//tvTitle.setText(currentSong.getTitle());
				setTitle(currentSong.getTitle());
				tvArtist.setText(currentSong.getArtist());
				
				btnPause.setImageResource(android.R.drawable.ic_media_pause);
				
				sbProgress.setMax(currentSong.getDuration());
				tvTotalTime.setText(currentSong.getDurationFormatted());
				
				Log.d(getPackageName(), "Id: "+currentSong.getId()+"; Album Id: "+currentSong.getAlbum_id());
				// Get the album art for now playing song
				Bitmap bm = null;
				bm = Album.getArtwork(this, currentSong.getId(), currentSong.getAlbum_id());
				if (bm != null) ivAlbum.setImageBitmap(bm);
			}
			
			try {
				int ms = mpInterface.currentPosition();
				tvCurrentTime.setText(Song.msToTime(ms));
				sbProgress.setProgress(ms);
				
				if (mpInterface.isPlaying()) {
					btnPause.setImageResource(android.R.drawable.ic_media_pause);
				} else {
					btnPause.setImageResource(android.R.drawable.ic_media_play);
				}
			} catch (RemoteException e) {
				
			}
		} else {
			btnPause.setImageResource(android.R.drawable.ic_media_play);
		}
	}
	
	@Override
	protected void playposChanged(int ms) {
		// Change seek bar and play button accordingly to the current song data
		sbProgress.setProgress(ms);
		tvCurrentTime.setText(Song.msToTime(ms));
		
		if (ms == 0) btnPause.setImageResource(android.R.drawable.ic_media_play);
	}
}
