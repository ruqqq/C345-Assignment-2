/*
 * Same concept as SongsActivity (inherits SocialTunesActivity)
 * ...excepts it only operates in 1 mode and loads the relevant albums data instead of song list
 */
package sg.ruqqq.socialtunes;

import java.util.ArrayList;

import sg.ruqqq.socialtunes.adapter.AlbumAdapter;
import sg.ruqqq.socialtunes.adapter.SongAdapter;
import sg.ruqqq.socialtunes.item.Album;
import sg.ruqqq.socialtunes.item.Song;
import sg.ruqqq.socialtunes.skeleton.SocialTunesActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class AlbumsActivity extends SocialTunesActivity {
	ArrayList<Album> albums = new ArrayList<Album>();
	AlbumAdapter aa;
	
	String whereClause = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	// *POOF*
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_common);
        
        // Set title of activity
        setTitle("Albums");
        
        Intent i = getIntent();
        if (i.getIntExtra("artist_id", -1) != -1) {
        	setTitle(i.getStringExtra("artist"));
        	whereClause = "artist_id = "+i.getIntExtra("artist_id", -1);
        }
        
        // Get ListView widget
        ListView lv = (ListView) findViewById(R.id.list);
        lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> l, View v, int position,
					long id) {
				onListItemClick((ListView) l, v, position, id);
			}
        });
        
        // Instantiate ArrayAdapter
        aa = new AlbumAdapter(this, R.layout.list_item, albums);
        
        // Assign Adapter to ListView
        lv.setAdapter(aa);
	}
	
    public void onListItemClick(ListView l, View v, int position, long id) {
		final Album a = albums.get(position);
		Log.d(getPackageName(), "Selected Album: "+a);
		
		Intent i = new Intent(this, SongsActivity.class);
		i.putExtra("album", a.getTitle());
		i.putExtra("album_id", a.getId());
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
	}
	
    @Override
	public void loadData() {
    	Thread threadLoading = new Thread(new Runnable() {
    		public void run() {
				ContentResolver contentResolver = getBaseContext().getContentResolver();
				
				Cursor audioCursor = contentResolver.query( MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
															new String[] {  "_id",
																			MediaStore.Audio.AlbumColumns.ALBUM,
																			MediaStore.Audio.AlbumColumns.ARTIST,
																			MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS,
																		 }, whereClause, null, null);
				if (audioCursor.moveToFirst()) {
					do {
						final Album album = new Album(AlbumsActivity.this, audioCursor.getInt(0), audioCursor.getString(1), audioCursor.getString(2), audioCursor.getInt(3));
						runOnUiThread(new Runnable(){
							public void run() {
								albums.add(album);
								aa.notifyDataSetChanged();
							}
						});
						
						//uiRefresh.sendEmptyMessage(0);
						//Log.d(getPackageName(), "Album Found: "+album);
					} while (audioCursor.moveToNext());
				}
				audioCursor.close();
				
				runOnUiThread(new Runnable(){
					public void run() {
						updateNowPlaying();
					}
				});
    		}
    	}, "loader");
        threadLoading.start();
	}
	
	@Override
	protected void updateNowPlaying(Song currentSong, boolean animate) {
		super.updateNowPlaying(currentSong, animate);
		
		for (Album a : albums) {
			if (currentSong != null && a.getId() == currentSong.getAlbum_id()) {
				a.setPlaying(true);
				Log.d(getPackageName(), a.getTitle()+" found");
			} else {
				a.setPlaying(false);
			}
		}
		
		aa.notifyDataSetChanged();
	}
}
