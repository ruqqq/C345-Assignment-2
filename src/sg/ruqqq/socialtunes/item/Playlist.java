/*
 * Playlist class
 * ...stores Playlist data (usually just the id and title)
 * ...also contains static methods for managing playlist (retrieve songs, create/delete playlist) 
 */
package sg.ruqqq.socialtunes.item;

import java.util.ArrayList;

import sg.ruqqq.socialtunes.adapter.DbAdapter;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class Playlist {
	private int id;
	private String title;
	private ArrayList<Song> songs = new ArrayList<Song>();
	
	public Playlist(int id, String title) {
		super();
		this.id = id;
		this.title = title;
	}
	
	public Playlist(Context ctx, int id, String title) {
		this(id, title);
		
		songs = retrieveSongs(ctx, id);
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

	public ArrayList<Song> getSongs() {
		return songs;
	}

	public void setSongs(ArrayList<Song> songs) {
		this.songs = songs;
	}

	@Override
	public String toString() {
		return "Playlist [id=" + id + ", title=" + title + ", songs=" + songs
				+ "]";
	}
	
	static public boolean createPlaylist(Context ctx, String name) {
		ContentResolver resolver = ctx.getContentResolver();
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Audio.Playlists.NAME, name);
        
        Uri u = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
        if (u != null) return true; else return false;
	}
	
	static public int removePlaylist(Context ctx, int playlist_id) {
		ContentResolver resolver = ctx.getContentResolver();
		Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        
        return resolver.delete(uri, MediaStore.Audio.Playlists._ID+" = "+playlist_id, null);
	}
	
	static public ArrayList<Playlist> retrievePlaylists(Context ctx) {
		ArrayList<Playlist> playlists = new ArrayList<Playlist>();
		ContentResolver contentResolver = ctx.getContentResolver();
		
		Cursor audioCursor = contentResolver.query( MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
													new String[] {  "_id",
																	MediaStore.Audio.PlaylistsColumns.NAME
																 }, null, null, null);
		if (audioCursor.moveToFirst()) {
			do {
				Playlist playlist = new Playlist(audioCursor.getInt(0), audioCursor.getString(1));
				playlists.add(playlist);
				
				//Log.d("Playlist Class", "Playlist Found: "+playlist);
			} while (audioCursor.moveToNext());
		}
		audioCursor.close();
		
		return playlists;
	}
	
	static public boolean addSongToPlaylist(Context ctx, int playlist_id, int song_id) {
		ContentResolver resolver = ctx.getContentResolver();
		Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist_id);
		
		Cursor cur = resolver.query(uri, new String[]{ "count(*)" }, null, null, null);
		cur.moveToFirst();
		int total = cur.getInt(0);
		cur.close();
		
        ContentValues values = new ContentValues(2);
        //values.put(MediaStore.Audio.Playlists.Members.PLAYLIST_ID, playlist_id);
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, total+1);
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, song_id);
        
        Uri u = resolver.insert(uri, values);
        if (u != null) return true; else return false;
	}
	
	static public boolean addSongsToPlaylist(Context ctx, int playlist_id, int[] song_ids) {
		boolean result = true;
		
		for (int id : song_ids) {
			result &= Playlist.addSongToPlaylist(ctx, playlist_id, id);
		}
		
		return result;
	}
	
	static public boolean addSongToPlaylist(Context ctx, int playlist_id, Song song) {
		return Playlist.addSongToPlaylist(ctx, playlist_id, song.getId());
	}
	
	static public boolean addSongsToPlaylist(Context ctx, int playlist_id, Song[] songs) {
		boolean result = true;
		
		for (Song s : songs) {
			result &= Playlist.addSongToPlaylist(ctx, playlist_id, s.getId());
		}
		
		return result;
	}
	
	static public int removeSongFromPlaylist(Context ctx, int playlist_id, int song_id) {
		ContentResolver resolver = ctx.getContentResolver();
		Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist_id);
        
        return resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID+" = "+song_id, null);
	}
	
	static public int removeSongFromPlaylist(Context ctx, int playlist_id, Song song) {
		return removeSongFromPlaylist(ctx, playlist_id, song.getId());
	}
	
	static public int removeSongsFromPlaylist(Context ctx, int playlist_id, int[] song_ids) {
		int result = 0;
		
		for (int s : song_ids) {
			result *= Playlist.removeSongFromPlaylist(ctx, playlist_id, s);
		}
		
		return result;
	}
	
	static public int removeSongsFromPlaylist(Context ctx, int playlist_id, Song[] songs) {
		int result = 0;
		
		for (Song s : songs) {
			result *= Playlist.removeSongFromPlaylist(ctx, playlist_id, s.getId());
		}
		
		return result;
	}
	
	static public ArrayList<Song> retrieveSongs(Context ctx, int id) {
		ArrayList<Song> songs = new ArrayList<Song>();
		ContentResolver contentResolver = ctx.getContentResolver();
		
		Cursor audioCursor = contentResolver.query( MediaStore.Audio.Playlists.Members.getContentUri("external", id),
													new String[] {  MediaStore.Audio.Playlists.Members.AUDIO_ID,
																	MediaStore.Audio.Playlists.Members.TITLE,
																	MediaStore.Audio.Playlists.Members.ARTIST,
																	MediaStore.Audio.Playlists.Members.ARTIST_ID,
																	MediaStore.Audio.Playlists.Members.ALBUM,
																	MediaStore.Audio.Playlists.Members.ALBUM_ID,
																	MediaStore.Audio.Playlists.Members.DURATION,
																	MediaStore.Audio.Playlists.Members.DATA
																 }, null, null, null);
		if (audioCursor.moveToFirst()) {
			do {
				Song song = new Song(audioCursor.getInt(0), audioCursor.getString(1), audioCursor.getString(2), audioCursor.getInt(3), audioCursor.getString(4), audioCursor.getInt(5), audioCursor.getInt(6), audioCursor.getString(7));
				songs.add(song);
				
				//Log.d(getPackageName(), "Song Found: "+song);
			} while (audioCursor.moveToNext());
		}
		audioCursor.close();
		
		return songs;
	}
	
	static public ArrayList<Song> retrieveRecentSongs(Context ctx) {
		ArrayList<Song> songs = new ArrayList<Song>();
		DbAdapter mDbAdapter = new DbAdapter(ctx);
		mDbAdapter.open();
		
		Cursor idCursor = mDbAdapter.getRecentSongs();
		
		ArrayList<Integer> song_ids = new ArrayList<Integer>();
		
		if (idCursor.moveToFirst()) {
			do {
				song_ids.add(idCursor.getInt(1));
				
				//Log.d(getPackageName(), "Song Found: "+song);
			} while (idCursor.moveToNext());
		}
		idCursor.close();
		mDbAdapter.close();
		
		//Log.d("Playlist", "Where clause: "+where);
		
		ContentResolver contentResolver = ctx.getContentResolver();
		
		for (int song_id : song_ids) {
			Cursor audioCursor = contentResolver.query( MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
														new String[] {  "_id",
																		MediaStore.Audio.Media.TITLE,
																		MediaStore.Audio.Media.ARTIST,
																		MediaStore.Audio.Media.ARTIST_ID,
																		MediaStore.Audio.Media.ALBUM,
																		MediaStore.Audio.Media.ALBUM_ID,
																		MediaStore.Audio.Media.DURATION,
																		MediaStore.Audio.Media.DATA
																	 }, "_id = "+song_id, null, null);
			if (audioCursor.moveToFirst()) {
				do {
					Song song = new Song(audioCursor.getInt(0), audioCursor.getString(1), audioCursor.getString(2), audioCursor.getInt(3), audioCursor.getString(4), audioCursor.getInt(5), audioCursor.getInt(6), audioCursor.getString(7));
					songs.add(song);
					
					Log.d("Recent Song", "Song Found: "+song);
				} while (audioCursor.moveToNext());
			}
			audioCursor.close();
		}
		
		return songs;
	}
}
