package com.demonslayer.cupcake;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
	
	private static final String TAG = "DbHelper";
	private static final String DB_NAME = "tasks.db";
	private static final int DB_VERSION = 1;
	static final String TABLE = "task";
	private static final String C_ID = BaseColumns._ID;
	static final String C_CREATED_AT = "created_at";
	static final String C_TASK_NAME = "task_name";
	static final String C_DEFAULT_TIME = "default_time";
	
	private Context context;
	
	

	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table " + TABLE + " (" + C_ID + " int primary key, " + C_CREATED_AT + " int, " + C_TASK_NAME + " text, " 
				+ C_DEFAULT_TIME + " text)";
		db.execSQL(sql);
		ContentValues work = new ContentValues();
		work.put(C_TASK_NAME, "Work");
		work.put(C_DEFAULT_TIME, 25);
		
		db.insert(TABLE, null, work);
		
		ContentValues rest = new ContentValues();
		rest.put(C_TASK_NAME, "Rest");
		rest.put(C_DEFAULT_TIME, 5);
		
		db.insert(TABLE, null, rest);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + TABLE);
		Log.d(TAG, "onUpdated");
		onCreate(db);		
	}

}
