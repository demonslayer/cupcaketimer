package com.demonslayer.cupcake;

import com.example.cupcake.R;
import com.example.cupcake.R.layout;
import com.example.cupcake.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class TaskEditActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "TaskEditActivity";
	EditText taskName;
	EditText defaultTime;
	Button saveButton;
	DbHelper helper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_edit);
		
		taskName = (EditText) findViewById(R.id.taskNameField);
		defaultTime = (EditText) findViewById(R.id.defaultTimeField);
		saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(this);
		helper = new DbHelper(this.getBaseContext());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.task_edit, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.saveButton:
			Log.d(TAG, "pressed save");
			Log.d(TAG, "task name: " + taskName.getText());
			Log.d(TAG, "default time: " + defaultTime.getText());
			updateEntry(taskName.getText().toString(), defaultTime.getText().toString());
			break;
		}
		
	}

	private void updateEntry(String taskName, String timeString) {
		SQLiteDatabase db = helper.getWritableDatabase();
		
		int time = Integer.parseInt(timeString);
		
		ContentValues newTask = new ContentValues();
		newTask.put(DbHelper.C_TASK_NAME, taskName);
		newTask.put(DbHelper.C_DEFAULT_TIME, time);
		
		db.insert(DbHelper.TABLE, null, newTask);
		
		
		db.close();
	}

}
