/*
 * Song list activity
 * ...handles any action pertaining to listing of songs
 * loadData is performed in a separate thread so it doesn't hang the UI
 */
package sg.ruqqq.socialtunes;

import java.util.ArrayList;
import java.util.List;

import sg.ruqqq.socialtunes.adapter.SongAdapter;
import sg.ruqqq.socialtunes.item.Playlist;
import sg.ruqqq.socialtunes.item.Song;
import sg.ruqqq.socialtunes.skeleton.SocialTunesActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class SongsActivity extends SocialTunesActivity {
	ArrayList<Song> songs = new ArrayList<Song>();
	SongAdapter aa;
	
	// Specific whereClause for some situations
	String whereClause = null;
	
	// startActivityForResult identifier
	// For adding songs to Playlist
	static int GET_PLAYLIST = 1;
	
	// Not implemented
	private boolean browseMode = false;
	
	// Various vars storing ids relevant to the current 'operating mode' of SongsActivity
	// Modes: All Songs List (default), Album Songs List, Playlist Songs List, Recent Songs List
	private int album_id = -1;
	private int playlist_id = -1;
	private boolean recent_songs = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	// *POOF*
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_common);
        
        // Set title of activity
        setTitle("All Songs");
        
        // Get Extras and set the Activity to the specific 'operating modes'
        Intent i = getIntent();
        if (i.getIntExtra("album_id", -1) != -1) {
        	// Album Mode
        	setTitle(i.getStringExtra("album"));
        	album_id = i.getIntExtra("album_id", -1);
        	// Special Where Clause for retrieving Songs that belongs to this album
        	whereClause = MediaStore.Audio.AudioColumns.ALBUM_ID+" = "+album_id;
        } else if (i.getIntExtra("playlist_id", -1) != -1) {
        	// Playlist Mode / Now Playing Mode
        	setTitle(i.getStringExtra("playlist"));
        	playlist_id = i.getIntExtra("playlist_id", -1);	
        } else if (i.getBooleanExtra("recent_songs", false)) {
        	// Recent Songs Mode
        	setTitle("Recent Songs");
        	recent_songs = i.getBooleanExtra("recent_songs", false);
        }
        
        // Get ListView widget
        ListView lv = (ListView) findViewById(R.id.list);
        // Set LV items onClick behavior
        lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> l, View v, int position,
					long id) {
				onListItemClick((ListView) l, v, position, id);
			}
        });
        // Set LV items context menu behavior
        lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				   // Set the title for the context menu
			       menu.setHeaderTitle("Actions");
			       
			       // Create the items
			       // Option to add to playlist
			       MenuItem addToPlaylist = menu.add(0, 0, Menu.NONE, "Add to playlist");
			       addToPlaylist.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			    	   public boolean onMenuItemClick(MenuItem item) {
			    		   // Get selected index
			    		   AdapterView.AdapterContextMenuInfo menuInfo;
			    		   menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			    		   int index = menuInfo.position;
			    		   
			    		   // Get the item at the index
			    		   Song s = songs.get(index);
			    			
			    		   // Start PlaylistsActivity in browsing mode and wait for it to return result
			    		   Intent i = new Intent(SongsActivity.this, PlaylistsActivity.class);
			    		   i.putExtra("browseMode", true);
			    		   i.putExtra("song_id", s.getId());
			    		   if (playlist_id != -1) i.putExtra("playlist_id", playlist_id);
			    		   startActivityForResult(i, GET_PLAYLIST);
			    		   return true;
			    	   }   
			       });
			       
			       // Add 'Remove' if we're currently listing songs part of Playlist
			       if (playlist_id > -1)  {
			    	   MenuItem removeFromPlaylist = menu.add(0, 1, Menu.NONE, "Remove from playlist");
			    	   removeFromPlaylist.setOnMenuItemClickListener(new OnMenuItemClickListener(){
				    	   public boolean onMenuItemClick(MenuItem item) {
				    		   // Get selected index
				    		   AdapterView.AdapterContextMenuInfo menuInfo;
				    		   menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
				    		   int index = menuInfo.position;
				    		   
				    		   // Get the item at the index
				    		   Song s = songs.get(index);
				    		   
				    		   // Prepare to remove song
				    		   // 	call removeSongFromPlaylist that'll handle removing it from system db
				    		   Toast t;
				    		   if (Playlist.removeSongFromPlaylist(SongsActivity.this, playlist_id, s.getId()) > 0) {
				    			   t = Toast.makeText(SongsActivity.this, "Removed song from playlist", Toast.LENGTH_SHORT);
				    			   // Remove the item from the list and refresh LV
				    			   songs.remove(index);
					    		   aa.notifyDataSetChanged();
				    		   } else {
				    			   t = Toast.makeText(SongsActivity.this, "Failed to remove song from playlist. An error has occured.", Toast.LENGTH_SHORT);
				    		   }
				    		   
				    		   t.show();
				    		   return true;
				    	   }   
				       });
			       }
			       
			       // Option to view last played location
			       MenuItem lastLocations = menu.add(0, 2, Menu.NONE, "Last Played Locations");
			       lastLocations.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			    	   public boolean onMenuItemClick(MenuItem item) {
			    		   // Get selected index
			    		   AdapterView.AdapterContextMenuInfo menuInfo;
			    		   menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			    		   int index = menuInfo.position;
			    		   
			    		   // Get the item at the index
			    		   Song s = songs.get(index);
			    		
			    		   // Start SongLocationsActivity with the relevant data
			    		   Intent i = new Intent(SongsActivity.this, SongLocationsActivity.class);
			    		   i.putExtra("song_title", s.getTitle());
			    		   i.putExtra("song_artist", s.getArtist());
			    		   startActivity(i);
			    		   return true;
			    	   }   
			       });
			}
        });
        
        // Instantiate ArrayAdapter
        aa = new SongAdapter(this, R.layout.list_item, songs);
        
        // Assign Adapter to ListView
        lv.setAdapter(aa);
	}
	
	// Method to handle data received from Playlist in browsing mode
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// Perform action only if RESULT_OK
		if (resultCode == RESULT_OK) {
			// Resulting actions for GET_PLAYLIST
			if (requestCode == GET_PLAYLIST) {
				Log.d(getPackageName(), "playlist_id: "+data.getIntExtra("playlist_id", -1)+"; song_id: "+data.getIntExtra("song_id", -1));
				Toast t;
				// call addSongToPlaylist to handle adding of song to the playlist in system db
				if (Playlist.addSongToPlaylist(this, data.getIntExtra("playlist_id", -1), data.getIntExtra("song_id", -1))) {
					t = Toast.makeText(this, "Song added to '"+data.getStringExtra("playlist")+"'", Toast.LENGTH_SHORT);
				} else {
					t = Toast.makeText(this, "Failed to add song to playlist. An error has occured.", Toast.LENGTH_SHORT);
				}
	    		t.show();
			}
		}
	}
	
	// Handles List Item Click
    public void onListItemClick(ListView l, View v, int position, long id) {
		final Song s = songs.get(position);
		Log.d(getPackageName(), "Selected Song: "+s);
		
		// Always clear the current now playlist
		try {
			mpInterface.clearPlaylist();
			if (playlist_id != -1) {
				// if we're showing list of songs from a playlist
				mpInterface.addPlaylist(songs); // sync the list with the service playlist
				mpInterface.playFile(position); // play the selected song
			} else {
				mpInterface.addSongPlaylist(s); // add the selected song
				mpInterface.playFile(0); // play it
			}
		} catch(RemoteException e) {
			Log.e(getPackageName(), e.getMessage());
		}
		
		// Call the MediaPlayerActivity to show
		Intent i = new Intent(this, MediaPlayerActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
	}
	
    // Loads the required data for activity
    // See SocialTunesActivity
    @Override
	public void loadData() {
		Thread threadLoading = new Thread(new Runnable() {
    		public void run() {
    			Log.d(getPackageName(), "Loading song list");
    			if (playlist_id == -2) {
    				// Now Playing list
    				try {
    					List<Song> now_playing_list = mpInterface.getPlaylist();
    					if (now_playing_list.size() > 0) {
    						songs.addAll(now_playing_list);
    						runOnUiThread(new Runnable(){
    							public void run() {
    								aa.notifyDataSetChanged();
    							}
    						});
    					}
    				} catch (RemoteException e) {}
    			} else if (playlist_id > -1) {
    				// Specific Playlist
    				songs.addAll(Playlist.retrieveSongs(getBaseContext(), playlist_id));
    				runOnUiThread(new Runnable(){
						public void run() {
							aa.notifyDataSetChanged();
						}
					});
    			} else if (recent_songs) {
    				// Recent Songs
    				songs.addAll(Playlist.retrieveRecentSongs(getBaseContext()));
    				runOnUiThread(new Runnable(){
						public void run() {
							aa.notifyDataSetChanged();
						}
					});
    			} else {
    				// All Songs or Album's Songs
	    			ContentResolver contentResolver = getBaseContext().getContentResolver();
	    			
	    			Cursor audioCursor = contentResolver.query( MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
	    														new String[] {  "_id",
	    																		MediaStore.Audio.AudioColumns.TITLE,
	    																		MediaStore.Audio.AudioColumns.ARTIST,
	    																		MediaStore.Audio.AudioColumns.ARTIST_ID,
	    																		MediaStore.Audio.AudioColumns.ALBUM,
	    																		MediaStore.Audio.AudioColumns.ALBUM_ID,
	    																		MediaStore.Audio.AudioColumns.DURATION,
	    																		MediaStore.MediaColumns.DATA
	    																	 }, whereClause, null, null);
	    			if (audioCursor.moveToFirst()) {
	    				do {
	    					final Song song = new Song(audioCursor.getInt(0), audioCursor.getString(1), audioCursor.getString(2), audioCursor.getInt(3), audioCursor.getString(4), audioCursor.getInt(5), audioCursor.getInt(6), audioCursor.getString(7));
	    					runOnUiThread(new Runnable(){
	    						public void run() {
	    							songs.add(song);
	    							aa.notifyDataSetChanged();
	    						}
	    					});
	    					//Log.d(getPackageName(), "Song Found: "+song);
	    				} while (audioCursor.moveToNext());
	    			}
	    			audioCursor.close();
    			}
    			
    			// Refresh the now playing icon tag on the list
    			runOnUiThread(new Runnable(){
					public void run() {
						updateNowPlaying();
					}
				});
    		}
    	}, "loader");
        threadLoading.start();
	}
	
    // Override the default behavior so that we can show badge on the currently playing song
	@Override
	protected void updateNowPlaying(Song currentSong, boolean animate) {
		super.updateNowPlaying(currentSong, animate);
		
		for (Song s : songs) {
			if (currentSong != null && s.getId() == currentSong.getId()) {
				s.setPlaying(true);
				//Log.d(getPackageName(), s.getTitle()+" found");
			} else {
				s.setPlaying(false);
			}
		}
		
		aa.notifyDataSetChanged();
	}
}
