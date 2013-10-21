package com.demonslayer.cupcake;

import java.io.IOException;

import com.example.cupcake.R;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class TimerActivity extends Activity implements OnClickListener {

	private static final String TAG = "TimerActivity";

	private ImageButton upArrow;
	private ImageButton downArrow;
	private TextView minutesText; 
	private TextView secondsText;
	private Button startButton;

	private int minutes;
	private int seconds;

	private int lastSetMinutes = 25;

	private CountDownTimer timer;

	private MediaPlayer player;
	
	private DbHelper helper;
	private SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer);

		upArrow = (ImageButton) findViewById(R.id.upArrow);
		downArrow = (ImageButton) findViewById(R.id.downArrow);
		minutesText = (TextView) findViewById(R.id.minute);
		secondsText = (TextView) findViewById(R.id.second);
		startButton = (Button) findViewById(R.id.startbutton);

		minutes = 25;
		seconds = 0;

		updateText();

		upArrow.setOnClickListener(this);
		downArrow.setOnClickListener(this);
		startButton.setOnClickListener(this);

		setViewNotRunning();

		AssetFileDescriptor afd;
		try {
			afd = getAssets().openFd("tone.mp3");
			player = new MediaPlayer();
			player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
			player.setLooping(true);
			player.prepare();
		} catch (IOException e) {
			Log.e(TAG, "Couldn't open the mp3");
			player = null;
		}
		
		helper = new DbHelper(this.getBaseContext());		
		db = helper.getWritableDatabase();
		
		Cursor cursor = db.query(helper.TABLE, null, null, null, null, null, helper.C_CREATED_AT + " asc");
		
		Log.d(TAG, "cursor has " + cursor.getCount() + " entries.");
		
		if (cursor != null ) {
		    if  (cursor.moveToFirst()) {
		        do {
		        	String taskName = cursor.getString(cursor.getColumnIndex(helper.C_TASK_NAME));
					int defaultTime = cursor.getInt(cursor.getColumnIndex(helper.C_DEFAULT_TIME));
					
					Log.d(TAG, taskName + " has a default time of " + defaultTime);
		        }while (cursor.moveToNext());
		    }
		}
		cursor.close();
		db.close();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.timer, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		AlertDialog.Builder abuilder = new Builder(this);
		abuilder.setMessage("Quit Cupcake Timer?");
		abuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				timer.cancel(); 
				finish();           
			}
		});
		abuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();            
			}
		});
		AlertDialog alert = abuilder.create();
		alert.show();
	}

	@Override
	public void onClick(View v) {

		switch(v.getId()) {
		case R.id.upArrow:
			Log.d(TAG, "Pressed the up arrow");
			minutes++;
			break;
		case R.id.downArrow:
			Log.d(TAG, "Pressed the down arrow");
			if (minutes > 1) {
				minutes--;
			}
			break;
		case R.id.startbutton:
			Log.d(TAG, "Pressed the start button");
			if (startButton.getText().equals("Start")) {
				makeTimer();
				timer.start();
				setViewToRunning();
				lastSetMinutes = minutes;
			} else {
				timer.cancel();
				seconds = 0;
				minutes = lastSetMinutes;
				setViewNotRunning();
			}
			break;
		default:
			Log.wtf(TAG, "That's not even a button");
		}

		updateText();


	}

	private void setViewNotRunning() {
		upArrow.setVisibility(View.VISIBLE);
		downArrow.setVisibility(View.VISIBLE);
		startButton.setText("Start");
	}

	private void setViewToRunning() {
		upArrow.setVisibility(View.GONE);
		downArrow.setVisibility(View.GONE);
		startButton.setText("Cancel");
	}

	private void updateText() {
		secondsText.setText(String.format("%02d", seconds));
		minutesText.setText(String.format("%02d",minutes));
	}

	private void notifyFinished() {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = "Time's Up!";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

		Context context = getApplicationContext();
		CharSequence contentTitle = "Cupcake Timer";
		CharSequence contentText = "Your task is finished!";
		Intent notificationIntent = this.getIntent();
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		final int HELLO_ID = 1;

		mNotificationManager.notify(HELLO_ID, notification);

	}

	private void makeTimer() {

		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Timer Finished!");
		alertDialog.setMessage("The timer is finished! Take a well-earned break :)");
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog,int which) 
			{
				alertDialog.dismiss();
				if (player != null && player.isPlaying()) {
					player.stop();

					try {
						player.prepare();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		this.timer = new CountDownTimer(minutes*60*1000, 1000) {

			@Override
			public void onFinish() {
				Log.d(TAG, "Timer done");
				seconds = 0;
				minutes = lastSetMinutes;

				updateText();
				setViewNotRunning();

				if (player != null) {
					player.start();
				}

				notifyFinished();

				alertDialog.show();

			}

			@Override
			public void onTick(long millisUntilFinished) {

				Log.d(TAG, "tick");

				if (seconds <= 0) {
					seconds = 59;
					minutes--;
				} else {
					seconds--;
				}

				updateText();
			}
		};
	}

}
