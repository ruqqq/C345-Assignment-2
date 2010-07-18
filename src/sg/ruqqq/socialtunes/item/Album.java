/*
 * Stores Album details
 * ...also contains static methods for retrieving album art (from AOSP code)
 */
package sg.ruqqq.socialtunes.item;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import sg.ruqqq.socialtunes.R;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

public class Album {
	private int id;
	private String title;
	private String artist;
	private int artist_id;
	private int total;
	private boolean playing = false;
	private BitmapDrawable albumArt = null;
	
	public Album(Context ctx, int id, String title, String artist, int total) {
		this(id, title, artist, total);
		
		Bitmap aa = Album.getArtwork(ctx, -1, id, false);
		Bitmap aaUnknown = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.albumart_mp_unknown_list);
		if (aa == null) {
			aa = aaUnknown;
		} else {
			Bitmap b = Bitmap.createBitmap(aaUnknown.getWidth(), aaUnknown.getHeight(),
	                Bitmap.Config.RGB_565);
	        Canvas canvas = new Canvas(b);
	        canvas.drawARGB(255, 0, 255, 0);
	        Rect src = new Rect(0, 0, aa.getWidth(), aa.getHeight());
	        Rect dst = new Rect(0, 0, aaUnknown.getWidth(), aaUnknown.getHeight());
			canvas.drawBitmap(aa, src, dst, new Paint());
			aa = b;
		}
		
		albumArt = new BitmapDrawable(ctx.getResources(), aa);
		albumArt.setFilterBitmap(false);
		albumArt.setDither(false);
	}
	
	public Album(int id, String title, String artist, int total) {
		this.id = id;
		this.title = title;
		this.artist = artist;
		this.total = total;
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

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
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
	
	public boolean getPlaying() {
		return playing;
	}
	
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}
	
	public BitmapDrawable getAlbumArt() {
		return albumArt;
	}
	
	@Override
	public String toString() {
		return "Album [id=" + id + ", album=" + title + ", total=" + total
				+ "]";
	}
	
    
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

    /** Get album art for specified album. You should not pass in the album id
     * for the "unknown" album here (use -1 instead)
     * This method always returns the default album art icon when no album art is found.
     */
    public static Bitmap getArtwork(Context context, long song_id, long album_id) {
        return getArtwork(context, song_id, album_id, true);
    }

    /** Get album art for specified album. You should not pass in the album id
     * for the "unknown" album here (use -1 instead)
     */
    public static Bitmap getArtwork(Context context, long song_id, long album_id,
            boolean allowdefault) {

        if (album_id < 0) {
            // This is something that is not in the database, so get the album art directly
            // from the file.
            if (song_id >= 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            if (allowdefault) {
                return getDefaultArtwork(context);
            }
            return null;
        }

        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, sBitmapOptions);
            } catch (FileNotFoundException ex) {
                // The album art thumbnail does not actually exist. Maybe the user deleted it, or
                // maybe it never existed to begin with.
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefault) {
                            return getDefaultArtwork(context);
                        }
                    }
                } else if (allowdefault) {
                    bm = getDefaultArtwork(context);
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                }
            }
        }
        
        return null;
    }
    
    // get album art for specified file
    private static final String sExternalMediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();
    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;
        byte [] art = null;
        String path = null;

        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }

        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch (FileNotFoundException ex) {
            //
        }
        
        return bm;
    }
    
    private static Bitmap getDefaultArtwork(Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeStream(
                context.getResources().openRawResource(R.drawable.albumart_mp_unknown), null, opts);
    }
}
