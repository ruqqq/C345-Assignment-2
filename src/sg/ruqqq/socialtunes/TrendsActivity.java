/*
 * TrendsActivity
 * Displays data from webservice instead of internal DB
 * ...works similar to the other activities (i.e. loadData())
 * however, different in a way that it uses a special SectionedAdapter to display data
 */
package sg.ruqqq.socialtunes;

import java.util.ArrayList;

import sg.ruqqq.socialtunes.adapter.SectionedAdapter;
import sg.ruqqq.socialtunes.adapter.TrendAdapter;
import sg.ruqqq.socialtunes.item.Trend;
import sg.ruqqq.socialtunes.skeleton.SocialTunesActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TrendsActivity extends SocialTunesActivity {
	ArrayList<Trend> recent_songs = new ArrayList<Trend>();
	ArrayList<Trend> nearby_songs = new ArrayList<Trend>();
	ArrayList<Trend> locations = new ArrayList<Trend>();
	
	TrendAdapter aa_recent_songs;
	TrendAdapter aa_nearby_songs;
	TrendAdapter aa_locations;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	// *POOF*
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_common);
        
        // Set title of activity
        setTitle("Trends");
        
        // Get ListView widget
        ListView lv = (ListView) findViewById(R.id.list);
        lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> l, View v, int position,
					long id) {
				onListItemClick((ListView) l, v, position, id);
			}
        });
        
        // Instantiate ArrayAdapter
        aa_recent_songs = new TrendAdapter(this, R.layout.list_item, recent_songs);
        aa_nearby_songs = new TrendAdapter(this, R.layout.list_item, nearby_songs);
        
        aa.addSection("Top Recent Songs", aa_recent_songs);
        aa.addSection("Nearby Songs", aa_nearby_songs);
        
        // Assign Adapter to ListView
        lv.setAdapter(aa);
	}
	
    public void onListItemClick(ListView l, View v, int position, long id) {
    	final Trend t = (Trend) aa.getItem(position);
		Log.d(getPackageName(), "Selected Trend: "+t);
		/*Toast toast = Toast.makeText(this, "Item Clicked: "+t, Toast.LENGTH_SHORT);
    	toast.show();*/
		
		Intent i = new Intent(this, SongLocationsActivity.class);
		i.putExtra("song_title", t.getSong());
		i.putExtra("song_artist", t.getArtist());
		startActivity(i);
	}
	
    @Override
	public void loadData() {
    	Thread threadLoading = new Thread(new Runnable() {
    		public void run() {
				boolean data_found = false;
				
				runOnUiThread(new Runnable(){
					public void run() {
						Toast toast = Toast.makeText(TrendsActivity.this, "Getting data from web. Please wait...", Toast.LENGTH_LONG);
				    	toast.show();
					}
				});
				
				final ArrayList<Trend> recent_songs_from_web = Webservice.recentPopularSongs(86400, 10);
				if (recent_songs_from_web.size() > 0) {
					runOnUiThread(new Runnable(){
								public void run() {
									recent_songs.addAll(recent_songs_from_web);
									aa_recent_songs.notifyDataSetChanged();
									aa.notifyDataSetChanged();
								}
					 });
					
					data_found = true;
				}
				
				double[] location = {1.441632, 103.753853};
				try {
					double[] loc = mpInterface.getCurrentLocation();
					if (loc != null)
						location = loc;
				} catch (RemoteException e) { Log.d(getPackageName(), "currentLocation not locked, using default location"); }
				
				final ArrayList<Trend> nearby_songs_from_web = Webservice.nearbySongs(location[0], location[1], 0, 10);
				if (recent_songs_from_web.size() > 0) {
					runOnUiThread(new Runnable(){
								public void run() {
									nearby_songs.addAll(nearby_songs_from_web);
									aa_nearby_songs.notifyDataSetChanged();
									aa.notifyDataSetChanged();
								}
					 });
					
					data_found = true;
				}
				
				if (!data_found) {
					runOnUiThread(new Runnable(){
						public void run() {
							Toast toast = Toast.makeText(TrendsActivity.this, "No data found", Toast.LENGTH_LONG);
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
