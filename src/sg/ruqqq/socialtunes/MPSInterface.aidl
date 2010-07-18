package sg.ruqqq.socialtunes;

import sg.ruqqq.socialtunes.item.Song;
import sg.ruqqq.socialtunes.IMPSCallbackInterface;

interface MPSInterface {
	void registerCallback(IMPSCallbackInterface cb);
    void unregisterCallback(IMPSCallbackInterface cb);
    
	void clearPlaylist();
	void addSongPlaylist( in Song song ); 
	void addPlaylist( in List<Song> songslist ); 
	void playFile( in int position );
	List<Song> getPlaylist();
	Song currentSong();
	
	void pause();
	void stop();
	boolean togglePlay();
	boolean isPlaying();
	void skipForward();
	void skipBack(); 
	int currentPosition();
	void setCurrentPosition(in int position);
	
	double[] getCurrentLocation();
} 