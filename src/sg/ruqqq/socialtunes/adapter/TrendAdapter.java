package sg.ruqqq.socialtunes.adapter;

import java.util.ArrayList;

import sg.ruqqq.socialtunes.R;
import sg.ruqqq.socialtunes.item.Trend;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TrendAdapter extends ArrayAdapter<Trend> {
	private ArrayList<Trend> items;
    Context context;
    int textViewResourceId;
    
    public TrendAdapter(Context context, int textViewResourceId, ArrayList<Trend> items) {
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
            Trend t = items.get(position);
            if (t != null) {
                    TextView vText = (TextView) v.findViewById(R.id.line1);
                    TextView vText2 = (TextView) v.findViewById(R.id.line2);
                    TextView vText3 = (TextView) v.findViewById(R.id.duration);
                    
                    if (t.getType() == 0) {
	                    if (vText != null) {
	                    	vText.setText(t.getSong());
	                    }
	                    if (vText2 != null) {
	                    	vText2.setText(t.getArtist()+" / "+t.getAlbum());
	                    }
	                    if (vText3 != null) {
	                    	if (t.getPlaycount() != -1) {
	                    		vText3.setText(t.getPlaycount()+" times");
	                    	}
	                    }
                    } else if (t.getType() == 1) {
	                    if (vText != null) {
	                    	vText.setText(t.getSong());
	                    }
	                    if (vText2 != null) {
	                    	vText2.setText(t.getArtist()+" / "+t.getAlbum());
	                    }
	                    if (vText3 != null) {
	                    	if (t.getDistance() != null) {
	                    		vText3.setText(t.getDistanceInKm()+"km away");
	                    	}
	                    }
                    } else if (t.getType() == 2) {
	                    if (vText != null) {
	                    	vText.setText(t.getAddress().getAddressLine(0));
	                    }
	                    if (vText2 != null) {
	                    	vText2.setText(t.getAddress().getAddressLine(1));
	                    }
                    }
            }
            
            return v;
    }
}
