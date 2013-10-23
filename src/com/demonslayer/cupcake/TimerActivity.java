package com.demonslayer.cupcake;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.cupcake.R;

import android.media.MediaPlayer;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class TimerActivity extends ListActivity implements OnClickListener {

	private static final String TAG = "TimerActivity";

	private ImageButton upArrow;
	private ImageButton downArrow;
	private TextView minutesText; 
	private TextView secondsText;
	private Button startButton;
	private ListView tasks;
	private TextView currentTask;
	private ImageView editButton;
	private ImageView deleteButton;
	private ImageView addButton;
	private ImageView clearButton;
	
	private int minutes;
	private int seconds;

	private int lastSetMinutes = 25;
	private String selectedTask;

	private CountDownTimer timer;

	private MediaPlayer player;
	
	private DbHelper helper;
	private SQLiteDatabase db;
	
	private int oldMinutes = 0;
	private int currentPosition;
	private SimpleAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer);

		upArrow = (ImageButton) findViewById(R.id.upArrow);
		downArrow = (ImageButton) findViewById(R.id.downArrow);
		minutesText = (TextView) findViewById(R.id.minute);
		secondsText = (TextView) findViewById(R.id.second);
		startButton = (Button) findViewById(R.id.startbutton);
		tasks = (ListView) findViewById(android.R.id.list);
		currentTask = (TextView) findViewById(R.id.currentTask);
		
		addButton = (ImageView) findViewById(R.id.addButton);
		editButton = (ImageView) findViewById(R.id.editButton);
		deleteButton = (ImageView) findViewById(R.id.deleteButton);
		clearButton = (ImageView) findViewById(R.id.clearButton);

		minutes = 25;
		seconds = 0;

		updateText();

		upArrow.setOnClickListener(this);
		downArrow.setOnClickListener(this);
		startButton.setOnClickListener(this);
		
		addButton.setOnClickListener(this);
		editButton.setOnClickListener(this);
		deleteButton.setOnClickListener(this);
		clearButton.setOnClickListener(this);
		
		editButton.setVisibility(View.GONE);
		deleteButton.setVisibility(View.GONE);
		clearButton.setVisibility(View.GONE);

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
		createList();
		
		tasks.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				
				Map item = (HashMap) parent.getAdapter().getItem(position);
				String taskName = (String) item.get("taskname");
				
				db = helper.getWritableDatabase();
				Cursor cursor = db.query(DbHelper.TABLE, null, 
		                DbHelper.C_TASK_NAME + "=?", new String[] {taskName}, null, null, null);
				int count = cursor.getCount();
				
				if (count > 0) {
					cursor.moveToFirst();
					selectedTask = taskName;
					currentPosition = position;
					minutes = cursor.getInt(cursor.getColumnIndex(DbHelper.C_DEFAULT_TIME));
					oldMinutes = cursor.getInt(cursor.getColumnIndex(DbHelper.C_MINUTES_COMPLETED));
					currentTask.setText("Task " + selectedTask + " is selected.");
					editButton.setVisibility(View.VISIBLE);
					deleteButton.setVisibility(View.VISIBLE);
					clearButton.setVisibility(View.VISIBLE);
					updateText();
				}
				
				cursor.close();
				db.close();
			}
			
		});
		
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
				if (timer != null) {
					timer.cancel(); 
				}
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
		case R.id.addButton:
			Log.d(TAG, "Pressed the add button");
			Intent intent = new Intent(this, TaskEditActivity.class);
			this.startActivity(intent);
			break;
		case R.id.clearButton:
			Log.d(TAG, "Pressed the clear button");
			this.selectedTask = null;
			currentTask.setText(R.string.notask);
			clearButton.setVisibility(View.GONE);
			editButton.setVisibility(View.GONE);
			deleteButton.setVisibility(View.GONE);
			break;
		default:
			Log.wtf(TAG, "That's not even a button");
		}

		updateText();


	}

	private void setViewNotRunning() {
		upArrow.setVisibility(View.VISIBLE);
		downArrow.setVisibility(View.VISIBLE);
		addButton.setVisibility(View.VISIBLE);
		if (selectedTask != null) {
			deleteButton.setVisibility(View.VISIBLE);
			editButton.setVisibility(View.VISIBLE);
			clearButton.setVisibility(View.VISIBLE);
		}
		startButton.setText("Start");
		if (selectedTask != null) {
			currentTask.setText("Task " + selectedTask + " is selected.");
		}
		
		tasks.setVisibility(View.VISIBLE);
	}

	private void setViewToRunning() {
		upArrow.setVisibility(View.GONE);
		downArrow.setVisibility(View.GONE);
		startButton.setText("Cancel");
		addButton.setVisibility(View.GONE);
		if (selectedTask != null) {
			deleteButton.setVisibility(View.GONE);
			editButton.setVisibility(View.GONE);
			clearButton.setVisibility(View.GONE);
		}
		if (selectedTask != null) {
			currentTask.setText("Now working on " + selectedTask);
		}
		
		tasks.setVisibility(View.GONE);
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
				
				if (selectedTask != null) {
					updateMinutesCompleted(lastSetMinutes, selectedTask);
				}

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
	
	protected void updateMinutesCompleted(int minutes,
			String taskName) {
		SQLiteDatabase db = helper.getWritableDatabase();
		
		ContentValues cv = new ContentValues();
		cv.put(DbHelper.C_MINUTES_COMPLETED, minutes + oldMinutes);
		db.update(DbHelper.TABLE, cv, DbHelper.C_TASK_NAME + "=?", new String[] {taskName});
		
		Map<String, String> item = (HashMap) adapter.getItem(currentPosition - 1);
		item.put("minutescompleted", String.valueOf(minutes + oldMinutes) + " minutes completed");
		adapter.notifyDataSetChanged();
				
		db.close();	
				
	}

	private void createList() {
		db = helper.getWritableDatabase();
		
		Cursor cursor = db.query(DbHelper.TABLE, null, null, null, null, null, DbHelper.C_CREATED_AT + " asc");
		
		Log.d(TAG, "cursor has " + cursor.getCount() + " entries.");
				
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(cursor.getCount());

        String[] from = new String[] { "taskname", "minutescompleted" };

        int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

        int nativeLayout = android.R.layout.two_line_list_item;
        
        if (cursor != null ) {
		    if  (cursor.moveToFirst()) {
		        do {
		        	String taskName = cursor.getString(cursor.getColumnIndex(DbHelper.C_TASK_NAME));
					int defaultTime = cursor.getInt(cursor.getColumnIndex(DbHelper.C_DEFAULT_TIME));
					int minutesCompleted = cursor.getInt(cursor.getColumnIndex(DbHelper.C_MINUTES_COMPLETED));
					int id = cursor.getInt(cursor.getColumnIndex(DbHelper.C_ID));
					
					Log.d(TAG, taskName + " has a default time of " + defaultTime + " and id of " + id);
					
					HashMap<String, String> item = new HashMap<String, String>();
					item.put("taskname", taskName);
					item.put("minutescompleted", minutesCompleted + " minutes completed");
					
					list.add(item);
		        }while (cursor.moveToNext());
		    }
		}
		cursor.close();
		db.close();
		
		if (tasks.getHeaderViewsCount() == 0) {
			TextView header = new TextView(this.getBaseContext());
			header.setText("Tasks");
			tasks.addHeaderView(header);
		}

		if (this.getListAdapter() == null) {
			adapter = new SimpleAdapter(this, list, nativeLayout , from, to);
	        this.setListAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}
	}

}
