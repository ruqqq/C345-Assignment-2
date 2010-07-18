package sg.ruqqq.socialtunes.adapter;

import java.util.List;

import sg.ruqqq.socialtunes.R;
import sg.ruqqq.socialtunes.item.DashboardItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DashboardAdapter extends BaseAdapter {

	private List<DashboardItem> dashboardItems;
	private LayoutInflater layoutInflater;

	public DashboardAdapter(Context context, List<DashboardItem> dashboardItems) {
		layoutInflater = LayoutInflater.from(context);
		this.dashboardItems = dashboardItems;
	}

	public int getCount() {
		return dashboardItems.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	static class ViewHolder {
		TextView name;
		ImageView icon;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid unneccessary calls
		// to findViewById() on each row.
		ViewHolder holder;

		// When convertView is not null, we can reuse it directly, there is no need
		// to reinflate it. We only inflate a new View when the convertView supplied
		// by ListView is null.
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.dashboard_item, null);

			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.tvName);
			holder.icon = (ImageView) convertView.findViewById(R.id.ivIcon);
			convertView.setTag(holder);

		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}
		
		// Bind the data efficiently with the holder.
		DashboardItem dashboardItem = dashboardItems.get(position);
		holder.name.setText(dashboardItem.getName());
		if (dashboardItem.getIcon() != null) {
			holder.icon.setImageDrawable(dashboardItem.getIcon());
		}
		
		return convertView;
	}
}
