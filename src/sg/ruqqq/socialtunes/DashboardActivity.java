/*
 * Dashboard of Social Tunes - Central Point of accessing its features
 */
package sg.ruqqq.socialtunes;

import java.util.ArrayList;

import sg.ruqqq.socialtunes.adapter.DashboardAdapter;
import sg.ruqqq.socialtunes.item.DashboardItem;
import sg.ruqqq.socialtunes.skeleton.SocialTunesActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class DashboardActivity extends SocialTunesActivity {
	// GridView data storage
	private ArrayList<DashboardItem> dashboardItems;
	private DashboardAdapter dashboardAdapter;
	
	// Views from Layout
	private GridView gvDashboard;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// *POOF*
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        
        // Refer to SocialTunes Activity: Sets the background effect based on user Home wallpaper
        setShowWallpaperTitlebar(true);
        
        // Get Views from Layout
        gvDashboard = (GridView) findViewById(R.id.gvDashboard);

        // Create our item list and populate
        dashboardItems = new ArrayList<DashboardItem>();
        dashboardItems.add(new DashboardItem("Recent Songs", getResources().getDrawable(R.drawable.ic_mp_screen_recent)));
        dashboardItems.add(new DashboardItem("Trends", getResources().getDrawable(R.drawable.ic_mp_screen_trends)));
        dashboardItems.add(new DashboardItem("Songs", getResources().getDrawable(R.drawable.ic_mp_screen_tracks)));
        dashboardItems.add(new DashboardItem("Artists", getResources().getDrawable(R.drawable.ic_mp_screen_artists)));
        dashboardItems.add(new DashboardItem("Albums", getResources().getDrawable(R.drawable.ic_mp_screen_albums)));
        dashboardItems.add(new DashboardItem("Playlists", getResources().getDrawable(R.drawable.ic_mp_screen_playlists)));
        
        // Link our item list to our adapter
        dashboardAdapter = new DashboardAdapter(this, dashboardItems);
        
        // Attach our item adapter to our GridView
        gvDashboard.setAdapter(dashboardAdapter);
        
        // Set GridView onClickListener
        gvDashboard.setOnItemClickListener(itemClickListener);
    }
    
    // GridView Item Click Listener
    private OnItemClickListener itemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			Intent intent = new Intent();
			//intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			
			switch (position) {
				case 0:
					intent.setClass(parent.getContext(), SongsActivity.class);
					intent.putExtra("recent_songs", true);
					break;
				case 1:
					intent.setClass(parent.getContext(), TrendsActivity.class);
					break;
				case 2:
					intent.setClass(parent.getContext(), SongsActivity.class);
					break;
				case 3:
					intent.setClass(parent.getContext(), ArtistsActivity.class);
					break;
				case 4:
					intent.setClass(parent.getContext(), AlbumsActivity.class);
					break;
				case 5:
					intent.setClass(parent.getContext(), PlaylistsActivity.class);
					break;
				default:
			}
			
			// If it's a valid activity, start it
			if (intent.getComponent() != null)
				startActivity(intent);
		}
	};
}