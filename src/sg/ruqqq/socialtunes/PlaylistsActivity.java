/*
 * PlaylistsActivity for displaying/creating playlists from ContentResolver
 * ...most of the codes are similar to SongsActivity.java
 * except for it's using static methods from Playlist.java to retrieve data
 * 
 * also allows for browseMode for which it acts a List to select a playlist and return the result on finish
 */
package sg.ruqqq.socialtunes;

import java.util.ArrayList;

import sg.ruqqq.socialtunes.adapter.PlaylistAdapter;
import sg.ruqqq.socialtunes.item.Playlist;
import sg.ruqqq.socialtunes.skeleton.SocialTunesActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PlaylistsActivity extends SocialTunesActivity {
	private ArrayList<Playlist> playlists = new ArrayList<Playlist>();
	private PlaylistAdapter aa;
	private ListView lv;
	
	boolean browseMode;
	int song_id = -1;
	int playlist_id = -1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	// *POOF*
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_common);
        
        // Set title of activity
        setTitle("Playlists");
        
        // Get intent to check for browseMode
        Intent i = getIntent();
        browseMode = i.getBooleanExtra("browseMode", false);
        // if browseMode enabled, change the list accordingly
        if (browseMode) {
        	playlist_id = i.getIntExtra("playlist_id", -1);
        	song_id = i.getIntExtra("song_id", -1);
        	setTitle("Select Playlist");
        }
        	
        // Get ListView widget
        lv = (ListView) findViewById(R.id.list);
        lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> l, View v, int position,
					long id) {
				onListItemClick((ListView) l, v, position, id);
			}
        });
        
        // These are the Headers and Footers list item (not part of the list items adapters)
        // ...display them accordingly to the modes of the list
         {
        	LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	
        	{
	        	View v = vi.inflate(R.layout.list_item, null);
	        	TextView vText = (TextView) v.findViewById(R.id.line1);
	        	vText.setText("New Playlist");
	        	vText.setPadding(15, 15, 0, 0);
	        	ImageView vImage = (ImageView) v.findViewById(R.id.icon);
	        	vImage.setImageResource(R.drawable.ic_mp_playlist_list);
	            ((ViewGroup) v).removeView(((TextView) v.findViewById(R.id.line2)));
	            v.setPadding(0, 0, 0, 0);
	            lv.addHeaderView(v);
        	}
        	
        	if (!browseMode) {
            	View v = vi.inflate(R.layout.list_item, null);
            	TextView vText = (TextView) v.findViewById(R.id.line1);
            	vText.setText("Now Playing");
            	vText.setPadding(15, 15, 0, 0);
                ((ViewGroup) v).removeView(((TextView) v.findViewById(R.id.line2)));
                lv.addHeaderView(v);
            }
        	
        	if (browseMode) {
            	View v = vi.inflate(R.layout.list_item, null);
            	TextView vText = (TextView) v.findViewById(R.id.line1);
            	vText.setText("Cancel");
            	vText.setPadding(15, 15, 0, 0);
                ((ViewGroup) v).removeView(((TextView) v.findViewById(R.id.line2)));
                lv.addFooterView(v);
            }
        }
        
        lv.setOnItemLongClickListener(new OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> av, View v,
					int position, long id) {
				if (position < lv.getHeaderViewsCount() || (browseMode && position >= lv.getCount()-lv.getHeaderViewsCount())) { Log.d(getPackageName(), "Long click header: "+position); return true; }
				else { Log.d(getPackageName(), "Long click normal: "+position); return false; }
			}
        });
        
        // Create context menu for deleting playlist
        lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){
 			public void onCreateContextMenu(ContextMenu menu, View v,
 					ContextMenuInfo menuInfo) {
 				   // Set the title for the context menu
 			       menu.setHeaderTitle("Actions");
 			       
 			       // Create the items and set their icons
 			       MenuItem removePlaylist = menu.add(0, 0, Menu.NONE, "Remove playlist");
 			       
 			       removePlaylist.setOnMenuItemClickListener(new OnMenuItemClickListener(){
 			    	   public boolean onMenuItemClick(MenuItem item) {
 			    		   // Get selected index
 			    		   AdapterView.AdapterContextMenuInfo menuInfo;
 			    		   menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 			    		   int index = menuInfo.position-lv.getHeaderViewsCount()-lv.getFooterViewsCount();
 			    		   
 			    		   Playlist p = playlists.get(index);
 			    		   Toast t;
 			    		   
 			    		   if (Playlist.removePlaylist(PlaylistsActivity.this, p.getId()) > 0) {
 			    			  t = Toast.makeText(PlaylistsActivity.this, "Playlists removed", Toast.LENGTH_SHORT);
 			    			  playlists.remove(index);
 			    			  aa.notifyDataSetChanged();
 			    		   } else {
 			    			  t = Toast.makeText(PlaylistsActivity.this, "Failed to remove playlist. An error has occured.", Toast.LENGTH_SHORT);
 			    		   }
 			    		   t.show();
 			    		   return true;
 			    	   }   
 			       });
 			}
        });
        
        // Instantiate ArrayAdapter
        aa = new PlaylistAdapter(this, R.layout.list_item, playlists);
        
        // Assign Adapter to ListView
        lv.setAdapter(aa);
	}
	
	// Handles on list item clock
	// ..in browse mode, it returns the activity result
	// in default mode, it'll display the playlist songs
    public void onListItemClick(ListView l, View v, int position, long id) {
		// Behaviors of Header and Footer list items
    	// (New Playlist, Now Playing, Cancel)
    	if (position < lv.getHeaderViewsCount() || (browseMode && position >= lv.getCount()-lv.getHeaderViewsCount())) {
			if (!browseMode) {
	    		if (position == 0) {
	    			showNewPlaylistDialog();
	    		} else if (position == 1) {
	    			Intent i = new Intent(this, SongsActivity.class);
					i.putExtra("playlist", "Now Playing");
					i.putExtra("playlist_id", -2);
					i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(i);
	    		}
			} else {
				if (position == 0) {
					showNewPlaylistDialog();
	    		} else if (position == lv.getCount()-lv.getHeaderViewsCount()) {
	    			setResult(RESULT_CANCELED);
	    			finish();
	    		}
			}
    	} else {
    		// Behaviors of normal list items
    		final Playlist p = playlists.get(position-lv.getHeaderViewsCount());
    		Log.d(getPackageName(), "Selected Playlist: "+p);
    		
    		if (!browseMode) {
    			Intent i = new Intent(this, SongsActivity.class);
				i.putExtra("playlist", p.getTitle());
				i.putExtra("playlist_id", p.getId());
				i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(i);
    		} else {
    			Intent returnData = new Intent();
	    		returnData.putExtra("playlist", p.getTitle());
	    		returnData.putExtra("playlist_id", p.getId());
	    		returnData.putExtra("song_id", song_id);
	    		setResult(RESULT_OK, returnData);
	    		finish();
    		}
    	}
	}
	
    @Override
	public void loadData() {
    	Thread threadLoading = new Thread(new Runnable() {
    		public void run() {
    			final ArrayList<Playlist> playlist_from_db = Playlist.retrievePlaylists(getBaseContext());
    			
				runOnUiThread(new Runnable(){
					public void run() {
						playlists.addAll(playlist_from_db);
						if (playlist_id != -1) {
							for (Playlist p : playlists) {
								if (p.getId() == playlist_id) {
									playlists.remove(p);
								}
							}
						}
						aa.notifyDataSetChanged();
					}
				});
    		}
    	}, "loader");
        threadLoading.start();
	}
	
	private void newPlaylist(String name) {
		Playlist.createPlaylist(this, name);
		
        playlists.clear();
        loadData();
	}
	
	// Add Playlist Dialog
	// ...code borrowed from Nam (twitter.com/blackhatmac)
	private void showNewPlaylistDialog(){
        final FrameLayout fl = new FrameLayout(this); 
        final EditText input = new EditText(this); 
        fl.setPadding(2,2,2,2);
        fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)); 
        
        input.setText(""); 
        new AlertDialog.Builder(this) 
             .setView(fl)
             .setTitle("Add Playlist") 
             .setPositiveButton("OK", new DialogInterface.OnClickListener(){ 
                  public void onClick(DialogInterface d, int which) {
                	   String name = input.getText().toString();
                	   if (name.equals("")) {
                		   showNewPlaylistDialog();
                	   } else {
                		   newPlaylist(name);
                	   }
                       d.dismiss();
                  } 
             }) 
             .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){ 
                  public void onClick(DialogInterface d, int which) { 
                       d.dismiss(); 
                  } 
             }).create().show();
    }
}
