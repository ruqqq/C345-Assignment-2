/*
 * DbAdapter class for talking to our socialtunes.db
 * ..db is just for storing song_ids of last 25 played songs
 */
package sg.ruqqq.socialtunes.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class DbAdapter {
	private static final String DATABASE_NAME = "socialtunes.db";
	private static final String DATABASE_TABLE_NAME = "socialtunes";
	private static final int DATABASE_VERSION = 1;
	
	public static int RECENT_SONG_LIMIT = 25;
	
	public static final String KEY_SONG_ID = "_id";
	public static final String KEY_SONG_SONG_ID = "song_id";
	public static final String KEY_SONG_COUNT = "count";

	private static final String DATABASE_CREATE= "create table "
			+ DATABASE_TABLE_NAME + " (" + KEY_SONG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ KEY_SONG_COUNT + " INTEGER NOT NULL, "+KEY_SONG_SONG_ID+" INTEGER NOT NULL);";  
	
	private SQLiteDatabase db;
	private final Context context;
	private MyDbHelper myDbHelper;
	
	public DbAdapter(Context _context) {
		context = _context;
		myDbHelper = new MyDbHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);
	}

	public DbAdapter open() throws SQLException {
		db = myDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		db.close();
	}
	
	// record the song in the database
	public long pushSong(int song_id) {
		Cursor c = db.query(DATABASE_TABLE_NAME,
							new String[] { KEY_SONG_ID,
										   KEY_SONG_SONG_ID,
										   KEY_SONG_COUNT },
							KEY_SONG_SONG_ID + "=" + song_id, null, null, null, null);

		int count = c.getCount();
		if (count > 0) {
			c.moveToFirst();
			long id = c.getLong(c.getColumnIndex(KEY_SONG_ID));
			//int songcount = c.getInt(c.getColumnIndex(KEY_SONG_COUNT));
			removeSong(id);
		}
		
		c.close();
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_SONG_SONG_ID, song_id);
		contentValues.put(KEY_SONG_COUNT, 0);
			
		Long insert = db.insert(DATABASE_TABLE_NAME, null, contentValues);

		return insert;
	}
	
	public boolean removeSong(long _id) {
		return db.delete(DATABASE_TABLE_NAME, KEY_SONG_ID + "=" + _id, null) > 0;
	}
	
	// get the list of recent songs
	public Cursor getRecentSongs() {
		return db.query(DATABASE_TABLE_NAME,
						new String[] { KEY_SONG_ID,
									   KEY_SONG_SONG_ID,
									   KEY_SONG_COUNT },
						null, null, null, null, KEY_SONG_ID+" DESC LIMIT "+RECENT_SONG_LIMIT);
	}

	// update the song count/ id (not used)
	private int updateSong(long _id, int song_id, int songcount) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_SONG_SONG_ID, song_id);
		contentValues.put(KEY_SONG_COUNT, songcount);
		
		return db.update(DATABASE_TABLE_NAME, contentValues, KEY_SONG_ID
				+ "=" + _id, null);
	}

	// clear the database
	public boolean clearSongs() {
		return db.delete(DATABASE_TABLE_NAME, null, null) > 0;
	}
	
	// the helper for the generated sql queries
	private static class MyDbHelper extends SQLiteOpenHelper {

		public MyDbHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		@Override
		// Only gets called if the database does not exist on the phone
		public void onCreate(SQLiteDatabase _db) {
			_db.execSQL(DATABASE_CREATE);

		}

		public void onUpgrade(SQLiteDatabase _db, int _oldVersion,
				int _newVersion) {
			// Drop old one
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CREATE);
			// Create new one
			onCreate(_db);
		}
	}
}
