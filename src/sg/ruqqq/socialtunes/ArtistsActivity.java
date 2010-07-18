/*
 * Same concept as SongsActivity (inherits SocialTunesActivity)
 * ...excepts it only operates in 1 mode and loads the relevant artists data instead of song list
 */
package sg.ruqqq.socialtunes;

import java.util.ArrayList;

import sg.ruqqq.socialtunes.adapter.ArtistAdapter;
import sg.ruqqq.socialtunes.item.Artist;
import sg.ruqqq.socialtunes.item.Song;
import sg.ruqqq.socialtunes.skeleton.SocialTunesActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ArtistsActivity extends SocialTunesActivity {
	ArrayList<Artist> artists = new ArrayList<Artist>();
	ArtistAdapter aa;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	// *POOF*
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_common);
        
        // Set title of activity
        setTitle("Artists");
        
        // Get ListView widget
        ListView lv = (ListView) findViewById(R.id.list);
        lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> l, View v, int position,
					long id) {
				onListItemClick((ListView) l, v, position, id);
			}
        });
        
        // Instantiate ArrayAdapter
        aa = new ArtistAdapter(this, R.layout.list_item, artists);
        
        // Assign Adapter to ListView
        lv.setAdapter(aa);
	}
	
    public void onListItemClick(ListView l, View v, int position, long id) {
		final Artist a = artists.get(position);
		Log.d(getPackageName(), "Selected Album: "+a);
		
		Intent i = new Intent(this, AlbumsActivity.class);
		i.putExtra("artist", a.getTitle());
		i.putExtra("artist_id", a.getId());
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
	}
	
    @Override
	public void loadData() {
    	Thread threadLoading = new Thread(new Runnable() {
    		public void run() {
				ContentResolver contentResolver = getBaseContext().getContentResolver();
				
				Cursor audioCursor = contentResolver.query( MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
															new String[] {  "_id",
																			MediaStore.Audio.ArtistColumns.ARTIST,
																			MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
																			MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS,
																		 }, null,null, null);
				if (audioCursor.moveToFirst()) {
					do {
						final Artist artist = new Artist(audioCursor.getInt(0), audioCursor.getString(1), audioCursor.getInt(2), audioCursor.getInt(3));
						runOnUiThread(new Runnable(){
							public void run() {
								artists.add(artist);
								aa.notifyDataSetChanged();
							}
						});
						//Log.d(getPackageName(), "Artist Found: "+artist);
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
		
		for (Artist a : artists) {
			if (currentSong != null && a.getId() == currentSong.getArtist_id()) {
				a.setPlaying(true);
				Log.d(getPackageName(), a.getTitle()+" found");
			} else {
				a.setPlaying(false);
			}
		}
		
		aa.notifyDataSetChanged();
	}
}
