package sg.ruqqq.socialtunes.adapter;

import java.util.ArrayList;

import sg.ruqqq.socialtunes.R;
import sg.ruqqq.socialtunes.item.Artist;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ArtistAdapter extends ArrayAdapter<Artist> {
	private ArrayList<Artist> items;
    Context context;
    int textViewResourceId;
    
    public ArtistAdapter(Context context, int textViewResourceId, ArrayList<Artist> items) {
            super(context, textViewResourceId, items);
            this.items = items;
            this.context = context;
            this.textViewResourceId = textViewResourceId;
    }

    // getView override which allows the use of custom XML for our list
    // this method associate the data to the view for the list item
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(textViewResourceId, null);
            }
            Artist a = items.get(position);
            if (a != null) {
                    TextView vText = (TextView) v.findViewById(R.id.line1);
                    TextView vText2 = (TextView) v.findViewById(R.id.line2);
                    if (vText != null) {
                    	vText.setText(a.getTitle());
                    }
                    if (vText2 != null) {
                    	vText2.setText(a.getTotalAlbums()+" albums, "+a.getTotalTracks()+" total tracks");
                    }
                    if (a.getPlaying()) ((View) v.findViewById(R.id.play_indicator)).setVisibility(View.VISIBLE);
                    else ((View) v.findViewById(R.id.play_indicator)).setVisibility(View.INVISIBLE);
            }
            
            return v;
    }
}
