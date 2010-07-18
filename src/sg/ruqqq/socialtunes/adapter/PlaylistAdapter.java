package sg.ruqqq.socialtunes.adapter;

import java.util.ArrayList;

import sg.ruqqq.socialtunes.R;
import sg.ruqqq.socialtunes.item.Playlist;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PlaylistAdapter extends ArrayAdapter<Playlist> {
	private ArrayList<Playlist> items;
    Context context;
    int textViewResourceId;
    
    public PlaylistAdapter(Context context, int textViewResourceId, ArrayList<Playlist> items) {
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
            Playlist s = items.get(position);
            if (s != null) {
                    TextView vText = (TextView) v.findViewById(R.id.line1);
                    TextView vText2 = (TextView) v.findViewById(R.id.line2);
                    vText.setPadding(15, 15, 0, 0);
                    ((ViewGroup) v).removeView(vText2);
                    //TextView vText3 = (TextView) v.findViewById(R.id.duration);
                    if (vText != null) {
                    	vText.setText(s.getTitle());
                    }
                    /*if (vText2 != null) {
                    	vText2.setText(s.getArtist());
                    }
                    if (vText3 != null) {
                    	vText3.setText(s.getDurationFormatted());
                    }*/
            }
            return v;
    }
}
