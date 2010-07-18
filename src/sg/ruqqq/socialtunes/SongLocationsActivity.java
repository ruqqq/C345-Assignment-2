/*
 * A slimmed down version of TrendsActivity to show selected song last played location
 * ...data is grabbed from webservice
 */
package sg.ruqqq.socialtunes;

import java.util.ArrayList;

import sg.ruqqq.socialtunes.adapter.SectionedAdapter;
import sg.ruqqq.socialtunes.adapter.TrendAdapter;
import sg.ruqqq.socialtunes.item.Song;
import sg.ruqqq.socialtunes.item.Trend;
import sg.ruqqq.socialtunes.skeleton.SocialTunesActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SongLocationsActivity extends SocialTunesActivity {
	ArrayList<Trend> locations = new ArrayList<Trend>();
	TrendAdapter aa_locations;
	
	String song_title = null;
	String song_artist = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	// *POOF*
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_common);
        
        // Set title of activity
        setTitle("Last Played Locations");
        
        Intent i = getIntent();
        song_title = i.getStringExtra("song_title");
        song_artist = i.getStringExtra("song_artist");
        
        if (song_title == null) {
        	Toast toast = Toast.makeText(this, "Invalid song data provided", Toast.LENGTH_SHORT);
        	toast.show();
        	finish();
        }
        
        // Get ListView widget
        ListView lv = (ListView) findViewById(R.id.list);
        /*lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> l, View v, int position,
					long id) {
				onListItemClick((ListView) l, v, position, id);
			}
        });*/
        
        // Instantiate ArrayAdapter
        aa_locations = new TrendAdapter(this, R.layout.list_item, locations);
        aa.addSection(song_title+" by "+song_artist, aa_locations);
        
        // Assign Adapter to ListView
        lv.setAdapter(aa);
	}
	
	@Override
	public void loadData() {
        Thread threadLoading = new Thread(new Runnable() {
    		public void run() {
				runOnUiThread(new Runnable(){
					public void run() {
						Toast toast = Toast.makeText(SongLocationsActivity.this, "Getting data from web. Please wait...", Toast.LENGTH_LONG);
				    	toast.show();
					}
				});
				
				final ArrayList<Trend> locations_from_web;
				
				if (song_artist != null) {
					locations_from_web = Webservice.songLocations(song_title, song_artist);
				} else {
					locations_from_web = Webservice.songLocations(song_title);
				}
				
				if (locations_from_web.size() > 0) {
					for (Trend t : locations_from_web) {
						t.retrieveAddress(SongLocationsActivity.this);
					}
					
					runOnUiThread(new Runnable(){
								public void run() {
									locations.addAll(locations_from_web);
									aa_locations.notifyDataSetChanged();
									aa.notifyDataSetChanged();
								}
					 });
				} else {
					runOnUiThread(new Runnable(){
						public void run() {
							Toast toast = Toast.makeText(SongLocationsActivity.this, "No data found", Toast.LENGTH_LONG);
					    	toast.show();
						}
					});
				}
    		}
    	}, "loader");
        threadLoading.start();
	}
	
	SectionedAdapter aa = new SectionedAdapter() {
		protected View getHeaderView(String caption, int index, View convertView, ViewGroup parent) {
			TextView result = (TextView) convertView;

			if (convertView == null) {
				result = (TextView) getLayoutInflater().inflate(R.layout.list_section_header, null);
			}

			result.setText(caption);

			return(result);
		}
	};
}
