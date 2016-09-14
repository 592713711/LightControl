package com.zsg.jx.lightcontrol.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLiteOpenHelper是一个辅助类，用来管理数据库的创建和版本他，它提供两个方面的功能
 * 第一，getReadableDatabase()、getWritableDatabase
 * ()可以获得SQLiteDatabase对象，通过该对象可以对数据库进行操作
 * 第二，提供了onCreate()、onUpgrade()两个回调函数，允许我们再创建和升级数据库时，进行自己的操作
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	private static final int VERSION = 4;
	private String DROP_SEARCH = "drop table if exists search ";

	public DatabaseHelper(Context context) {
		super(context, "cache.db", null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub;
		db.execSQL("create table if not exists user("
				+ "id varchar(25) PRIMARY KEY," + "name varchar(20),"
				+ "phone varchar(15)," + "sex varchar(10),"
				+ "password varchar(15)," + "create_time varchar(20),"
				+ "image_thumb varchar(30)," + "image varchar(30)" + ", token varchar(24))");

		db.execSQL("create table if not exists WifiDevices("
				+ "id varchar(25) PRIMARY KEY," + "name varchar(20),"
				+ "anitiLostSwitch integer," + "alertDistance integer,"
				+ "lostAlertSwitch integer," + "alertVolume integer,"
				+ "thumbnail varchar(30)," + "alertRingtone integer,"
                + "alertFindSwitch integer," + "findAlertVolume integer,"
                + "findAlertRingtone integer)");

		db.execSQL(ThemeDao.SQL_CREATE_TABLE);

		db.execSQL(AreaDao.SQL_CREATE_TABLE);


	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// TODO Auto-generated method stub
		//db.execSQL(DROP_SEARCH);
        //db.execSQL("drop table if exists user");
        //db.execSQL("drop table if exists WifiDevices");
		//db.execSQL(ThemeDao.SQL_DROP_TABLE);
		onCreate(db);
	}
}
