package sg.ruqqq.socialtunes.adapter;

import java.util.ArrayList;

import sg.ruqqq.socialtunes.R;
import sg.ruqqq.socialtunes.item.Album;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumAdapter extends ArrayAdapter<Album> {
	private ArrayList<Album> items;
    Context context;
    int textViewResourceId;
    
    public AlbumAdapter(Context context, int textViewResourceId, ArrayList<Album> items) {
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
            Album a = items.get(position);
            if (a != null) {
                    TextView vText = (TextView) v.findViewById(R.id.line1);
                    TextView vText2 = (TextView) v.findViewById(R.id.line2);
                    TextView vText3 = (TextView) v.findViewById(R.id.duration);
                    ImageView vImage = (ImageView) v.findViewById(R.id.icon);
                    if (vText != null) {
                    	vText.setText(a.getTitle());
                    }
                    if (vText2 != null) {
                    	vText2.setText(a.getArtist());
                    }
                    if (vText3 != null) {
                    	vText3.setText("Tracks: "+a.getTotal());
                    }
                    if (vImage != null) {
                    	vImage.setBackgroundDrawable(a.getAlbumArt());
                    	vImage.setPadding(0, 0, 1, 0);
                    }
                    if (a.getPlaying()) ((View) v.findViewById(R.id.play_indicator)).setVisibility(View.VISIBLE);
                    else ((View) v.findViewById(R.id.play_indicator)).setVisibility(View.INVISIBLE);
            }
            
            v.setPadding(0, 0, 0, 0);
            return v;
    }
}
