package sg.ruqqq.socialtunes.item;

public class Artist {
	private int id;
	private String title;
	private int totalAlbums;
	private int totalTracks;
	private boolean playing = false;
	
	public Artist(int id, String title, int totalAlbums, int totalTracks) {
		super();
		this.id = id;
		this.title = title;
		this.totalAlbums = totalAlbums;
		this.totalTracks = totalTracks;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public int getTotalAlbums() {
		return totalAlbums;
	}
	
	public void setTotalAlbums(int totalAlbums) {
		this.totalAlbums = totalAlbums;
	}
	
	public int getTotalTracks() {
		return totalTracks;
	}
	
	public void setTotalTracks(int totalTracks) {
		this.totalTracks = totalTracks;
	}
	
	public boolean getPlaying() {
		return playing;
	}
	
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}
	
	@Override
	public String toString() {
		return "Artist [id=" + id + ", title=" + title + ", totalAlbums="
				+ totalAlbums + ", totalTracks=" + totalTracks + "]";
	}
}
