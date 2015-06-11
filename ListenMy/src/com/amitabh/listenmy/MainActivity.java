package com.amitabh.listenmy;

import java.io.FileDescriptor;
import java.io.IOException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnClickListener,
		OnCheckedChangeListener {

	private Button next;
	private Button previous;
	private ToggleButton playPause;
	private SeekBar seekBar;
	private ImageView albumArt;
	private long songId;
	private MediaPlayer mediaPlayer;
	private int thisAlbumId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getAllViews();
		setAllListener();
	}

	private void setAllListener() {
		int i = 0;
		next.setOnClickListener(this);
		previous.setOnClickListener(this);
		playPause.setOnCheckedChangeListener(this);

	}

	private void getAllViews() {
		next = (Button) findViewById(R.id.next);
		previous = (Button) findViewById(R.id.previous);
		playPause = (ToggleButton) findViewById(R.id.playPause);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		albumArt = (ImageView) findViewById(R.id.albumArt);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.next:
			getFilesUri();

			break;

		default:
			break;
		}

	}

	private void getFilesUri() {
		ContentResolver contentResolver = getContentResolver();
		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		if (cursor == null) {
			// query failed, handle error.

			Log.e("No media", "Cursor is null");
		} else if (!cursor.moveToFirst()) {
			Log.e("Media found", "Coursor size" + cursor.getCount());
		} else {
			int titleColumn = cursor
					.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = cursor
					.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int albumIdColumn = cursor
					.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM_ID);
			int durationColumn = cursor
					.getColumnIndex((android.provider.MediaStore.Audio.Media.DURATION));
			int sizeColumn = cursor
					.getColumnIndex((android.provider.MediaStore.Audio.Media.SIZE));
			// int
			// abs=cursor.getColumnIndex((android.provider.MediaStore.Audio.Media.);
			do {
				songId = cursor.getLong(idColumn);
				String thisTitle = cursor.getString(titleColumn);
				double thisDuration = cursor.getDouble(durationColumn);
				double thisSize = cursor.getDouble(sizeColumn);
				thisAlbumId = cursor.getInt(sizeColumn);

				System.out.println("id = " + songId);
				System.out.println("Title = " + thisTitle);
				System.out.println("Duration = " + thisDuration);
				System.out.println("Size = " + thisSize);
			} while (cursor.moveToNext());
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			// play
			playMusic(isChecked);
		} else {
			// pause
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
		}

	}

	private void playMusic(boolean isChecked) {
		long id = songId;
		Uri contentUri = ContentUris.withAppendedId(
				android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				id);
		getAlbumArt(getApplicationContext(), thisAlbumId);

		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mediaPlayer.setDataSource(getApplicationContext(), contentUri);
			mediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (isChecked) {
			mediaPlayer.start();
		}

	}

	private Bitmap getAlbumArt(Context context, long album_id) {

		Bitmap bm = null;
		try {
			final Uri sArtworkUri = Uri
					.parse("content://media/external/audio/albumart");

			Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

			ParcelFileDescriptor pfd = context.getContentResolver()
					.openFileDescriptor(uri, "r");

			if (pfd != null) {
				FileDescriptor fd = pfd.getFileDescriptor();
				bm = BitmapFactory.decodeFileDescriptor(fd);
			}
		} catch (Exception e) {
			Log.e("getAlbumArt Error", e.toString());
		}
		return bm;

	}
}
