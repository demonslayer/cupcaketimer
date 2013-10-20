package com.example.cupcake;

import java.io.IOException;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
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
				
		makeTimer();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.timer, menu);
		return true;
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
			if (minutes > 0) {
				minutes--;
			}
			break;
		case R.id.startbutton:
			Log.d(TAG, "Pressed the start button");
			timer.start();
			setViewToRunning();
			break;
		default:
			Log.wtf(TAG, "That's not even a button");
		}
		
		makeTimer();
		updateText();
		lastSetMinutes = minutes;
		
	}

	private void setViewNotRunning() {
		upArrow.setVisibility(View.VISIBLE);
		downArrow.setVisibility(View.VISIBLE);
		startButton.setVisibility(View.VISIBLE);
	}

	private void setViewToRunning() {
		upArrow.setVisibility(View.GONE);
		downArrow.setVisibility(View.GONE);
		startButton.setVisibility(View.GONE);
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
