package com.zsg.jx.lightcontrol.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zsg.jx.lightcontrol.bin.User;

import java.util.ArrayList;


/**
 * 任务列表的数据库操作类
 * 
 * @author 黄家强
 */
public class UserDao {
	private DatabaseHelper dbHelper;
	private static final String TABLE_NAME = "user";

	public UserDao(Context context) {
		super();
		dbHelper = new DatabaseHelper(context);
	}

	public boolean isExist(User user) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, "id = ?",
				new String[] { user.getId()}, null, null, null);
		if (cursor.getCount() > 0) {
			db.close();
			cursor.close();
			return true;
		}
		db.close();
		cursor.close();
		return false;
	}

	public int insert(User user) {
		User bef = queryById(user.getId());
		if (bef != null) {
			deleteById(bef.getId());
		}
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id", user.getId());
		values.put("name", user.getName());
		values.put("phone", user.getPhone());
		values.put("sex", user.getSex());
		values.put("password", user.getPassword());
		values.put("create_time", user.getCreate_time());
		values.put("image_thumb", user.getImage_thumb());
		values.put("image", user.getImage());
		values.put("token", user.getToken());
		int id = (int) db.insert(TABLE_NAME, null, values);
		db.close();
		Log.e("hjq", "insert id = " + id);
		return id;
	}

	public int updatePassWordById(String password,String id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("password", password);
		int index = (int) db.update(TABLE_NAME, values, "id = ?", new String[]{id});
		db.close();
		return index;
	}

    public int updateTokenById(String token, String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("token", token);
        int index = (int) db.update(TABLE_NAME, values, "id = ?", new String[]{id});
        db.close();
        return index;
    }

	public int update(User user) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("password", user.getPassword());
		values.put("name", user.getName());
		values.put("sex", user.getSex());
        values.put("token", user.getToken());
		int index = (int) db.update(TABLE_NAME, values, "id = ?",new String[]{user.getId()});

        Log.e("hjq", "index = " + index);
		db.close();
		return index;
	}

	public void deleteAll() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(TABLE_NAME, null, null);
		db.close();
	}

	public void deleteById(String id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(TABLE_NAME, "id = ?", new String[] {id });
		db.close();
	}

	public ArrayList<User> queryAll() {
		ArrayList<User> ss = new ArrayList<User>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db
				.query(TABLE_NAME, null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			String id = cursor.getString(cursor.getColumnIndex("id"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String phone = cursor.getString(cursor.getColumnIndex("phone"));
			String sex = cursor.getString(cursor.getColumnIndex("sex"));
			String password = cursor.getString(cursor.getColumnIndex("password"));
			String create_time = cursor.getString(cursor.getColumnIndex("create_time"));
			String image_thumb = cursor.getString(cursor.getColumnIndex("image_thumb"));
			String image = cursor.getString(cursor.getColumnIndex("image"));
            String token = cursor.getString(cursor.getColumnIndex("token"));
			ss.add(new User(id, name, phone, sex, password, create_time, image_thumb, image, token));
		}
		cursor.close();
		db.close();
		return ss;
	}

	public User queryById(String id) {
		User user = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Log.e("hjq", "id = " + id);
		Cursor cursor = db.query(TABLE_NAME, null, "id=?",
				new String[] { id }, null, null, null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String phone = cursor.getString(cursor.getColumnIndex("phone"));
			String sex = cursor.getString(cursor.getColumnIndex("sex"));
			String password = cursor.getString(cursor.getColumnIndex("password"));
			String create_time = cursor.getString(cursor.getColumnIndex("create_time"));
			String image_thumb = cursor.getString(cursor.getColumnIndex("image_thumb"));
			String image = cursor.getString(cursor.getColumnIndex("image"));
            String token = cursor.getString(cursor.getColumnIndex("token"));
			user = new User(id, name, phone, sex, password, create_time, image_thumb, image, token);
		}
		cursor.close();
		db.close();
        Log.e("hjq", "db user = " + user);
		return user;
	}

	public int getcount() {
		return queryAll().size();
	}
}
