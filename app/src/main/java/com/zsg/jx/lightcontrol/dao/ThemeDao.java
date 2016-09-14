package com.zsg.jx.lightcontrol.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.zsg.jx.lightcontrol.model.LightList;
import com.zsg.jx.lightcontrol.model.Theme;

import java.util.ArrayList;

/**
 * 情景模式表
 * Created by zsg on 2016/8/24.
 */
public class ThemeDao {
    //列名
    private static final String TABLE_NAME = "theme";      //表名
    public static final String COL_THEME_ID = "theme_id";    //id       自动增长
    public static final String COL_THEME_NAME = "theme_name";    //情景名
    public static final String COL_THEME_LIST = "theme_list";    //情景列表（json字符串）

    public static final String[] ALLCOL = {COL_THEME_ID, COL_THEME_NAME, COL_THEME_LIST};

    public static final String SQL_CREATE_TABLE = String.format(
            "CREATE table IF NOT EXISTS %s(%s integer PRIMARY KEY,%s text,%s text)",
            TABLE_NAME,
            COL_THEME_ID,
            COL_THEME_NAME,
            COL_THEME_LIST
    );

    //删除表语句
    public static final String SQL_DROP_TABLE = String.format(
            "drop table if exists %s",
            TABLE_NAME
    );

    private SQLiteDatabase db;
    private DatabaseHelper helper;
    private Gson gson;

    public ThemeDao(Context context) {
        helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
        gson = new Gson();
    }

    public void insert(Theme theme) {
        String json = gson.toJson(theme.getList());
        ContentValues values = new ContentValues();
        values.put(COL_THEME_NAME, theme.getTheme_name());
        values.put(COL_THEME_LIST, json);
        db.insert(TABLE_NAME, null, values);
    }

    public void update(Theme theme){
        String json = gson.toJson(theme.getList());
        ContentValues values = new ContentValues();
        values.put(COL_THEME_NAME, theme.getTheme_name());
        values.put(COL_THEME_LIST, json);
        db.update(TABLE_NAME,values,COL_THEME_ID+"="+theme.getThemeId(),null);
    }

    public ArrayList<Theme> getAll() {
        ArrayList<Theme> themeList = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, ALLCOL, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Theme theme = new Theme();
            theme.setThemeId(cursor.getInt(0));
            theme.setTheme_name(cursor.getString(1));
            String json = cursor.getString(2);
            theme.setList(gson.fromJson(json, LightList.class));
            themeList.add(theme);
        }
        cursor.close();

        return themeList;
    }


}
