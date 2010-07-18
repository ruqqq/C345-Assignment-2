package sg.ruqqq.socialtunes.item;

import sg.ruqqq.socialtunes.Webservice;
import android.content.Context;
import android.location.Address;

public class Trend {
	private int type = 0;
	private String song = "<unknown>";
	private String artist = "<unknown>";
	private String album = "<unknown>";
	private int playcount = -1;
	private String playtime = null;
	private double distance = -1;
	private double lat = -1;
	private double lng = -1;
	private Address address = null;
	
	public Trend() {
	}

	public Trend(String song, String artist) {
		this.song = song;
		this.artist = artist;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public String getSong() {
		return song;
	}

	public void setSong(String song) {
		if (song != null)
			this.song = song;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		if (artist != null)
			this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		if (album != null)
			this.album = album;
	}

	public int getPlaycount() {
		return playcount;
	}
	
	public void setPlaycount(int playcount) {
		this.playcount = playcount;
	}
	
	public void setPlaycount(String playcount) {
		this.playcount = Integer.parseInt(playcount);
	}

	public String getPlaytime() {
		return playtime;
	}

	public void setPlaytime(String playtime) {
		this.playtime = playtime;
	}

	public Double getDistance() {
		return distance;
	}
	
	public Double getDistanceInKm() {
		return (double)((int)distance/10)/100;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}
	
	public void setDistance(String distance) {
		this.distance = Double.parseDouble(distance);
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}
	
	public void setLat(String lat) {
		this.lat = Double.parseDouble(lat);
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}
	
	public void setLng(String lng) {
		this.lng = Double.parseDouble(lng);
	}
	
	public void retrieveAddress(Context ctx) {
		address = Webservice.getAddress(ctx, lat, lng);
	}
	
	public Address getAddress() {
		return address;
	}

	@Override
	public String toString() {
		return "Trend [song=" + song + ", artist=" + artist + ", album="
				+ album + ", playcount=" + playcount + ", playtime=" + playtime
				+ ", distance=" + distance + ", lat=" + lat + ", lng=" + lng
				+ "]";
	}
}
