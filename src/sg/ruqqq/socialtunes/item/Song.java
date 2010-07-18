/*
 * Song class
 * ..stores Song data
 * ..implements Parcelable so we could transfer a Song object between Service and Activities
 * ..done through AIDL interfac-ing
 */
package sg.ruqqq.socialtunes.item;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
	private int _id;
	private String title;
	private String title_key;
	private String artist;
	private int artist_id;
	private String artist_key;
	private String album;
	private int album_id;
	private String album_key;
	private int duration;
	private String filename;
	private boolean playing = false;
	
	public Song(int _id, String title, String artist, int artist_id, String album, int album_id, int duration,
			String filename) {
		super();
		this._id = _id;
		this.title = title;
		this.artist = artist;
		this.artist_id = artist_id;
		this.album = album;
		this.album_id = album_id;
		this.duration = duration;
		this.filename = filename;
	}
	
	private Song(Parcel in) {
        readFromParcel(in);
    }
	
	public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
	
    public int getId() {
    	return _id;
    }
    
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle_key() {
		return title_key;
	}
	public void setTitle_key(String title_key) {
		this.title_key = title_key;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public int getArtist_id() {
		return artist_id;
	}
	public void setArtist_id(int artist_id) {
		this.artist_id = artist_id;
	}
	public String getArtist_key() {
		return artist_key;
	}
	public void setArtist_key(String artist_key) {
		this.artist_key = artist_key;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public int getAlbum_id() {
		return album_id;
	}
	public void setAlbum_id(int album_id) {
		this.album_id = album_id;
	}
	public String getAlbum_key() {
		return album_key;
	}
	public void setAlbum_key(String album_key) {
		this.album_key = album_key;
	}
	public int getDuration() {
		return duration;
	}
	public String getDurationFormatted() {
		return msToTime(duration);
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public boolean getPlaying() {
		return playing;
	}
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	@Override
	public String toString() {
		return "Song [title=" + title + ", artist=" + artist + ", album="
				+ album + ", duration=" + getDurationFormatted() + ", filename=" + filename
				+ "]";
	}

	public int describeContents() {
		return 0;
	}
	
	public void readFromParcel(Parcel in) {
		this._id = in.readInt();
		this.title = in.readString();
		this.artist = in.readString();
		this.artist_id = in.readInt();
		this.album = in.readString();
		this.album_id = in.readInt();
		this.duration = in.readInt();
		this.filename = in.readString();
    }

	public void writeToParcel(Parcel out, int flags) {
		writeToParcel(out);
	}
	
	public void writeToParcel(Parcel out) {
		out.writeInt(_id);
		out.writeString(title);
		out.writeString(artist);
		out.writeInt(artist_id);
		out.writeString(album);
		out.writeInt(album_id);
		out.writeInt(duration);
		out.writeString(filename);
	}
	
    // Converts duration to human readable time
    static public String msToTime(long ms) {
        String format = String.format("%%0%dd", 2);
        String seconds = String.format(format, (ms / 1000) % 60);
        String minutes = String.format(format, (ms / 1000 / 60) % 60);
        String hours = String.format(format, (ms / 1000 / 60 / 60) % 24);
        String time = "";
        if (ms >= 1000 * 60 * 60) time =  hours + ":" + minutes + ":" + seconds;
        else time =  minutes + ":" + seconds;
        return time;
    }
}
