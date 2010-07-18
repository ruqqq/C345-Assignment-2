package sg.ruqqq.socialtunes.item;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class DashboardItem {
	private String name;
	private Drawable icon;
	
	public DashboardItem(String name, Drawable icon) {
		this.name = name;
		this.icon = icon;
	}
	
	public void setDrawable(Drawable icon) {
		this.icon = icon;
	}
	
	public String getName() {
		return name;
	}
	
	public Drawable getIcon() {
		return icon;
	}
}
