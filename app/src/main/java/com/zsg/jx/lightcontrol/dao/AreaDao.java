package com.zsg.jx.lightcontrol.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.zsg.jx.lightcontrol.model.Area;
import com.zsg.jx.lightcontrol.model.LightList;
import com.zsg.jx.lightcontrol.model.Theme;

import java.util.ArrayList;

/**
 * 区域划分表
 * Created by zsg on 2016/9/13.
 */
public class AreaDao {
    //列名
    private static final String TABLE_NAME = "area";      //表名
    public static final String COL_AREA_ID = "area_id";    //id       自动增长
    public static final String COL_AREA_NAME = "area_name";    //区域名
    public static final String COL_AREA_LIST = "area_list";    //区域灯泡列表（json字符串）

    public static final String[] ALLCOL = {COL_AREA_ID, COL_AREA_NAME, COL_AREA_LIST};

    public static final String SQL_CREATE_TABLE = String.format(
            "CREATE table IF NOT EXISTS %s(%s integer PRIMARY KEY,%s text,%s text)",
            TABLE_NAME,
            COL_AREA_ID,
            COL_AREA_NAME,
            COL_AREA_LIST
    );

    //删除表语句
    public static final String SQL_DROP_TABLE = String.format(
            "drop table if exists %s",
            TABLE_NAME
    );

    private SQLiteDatabase db;
    private DatabaseHelper helper;
    private Gson gson;

    public AreaDao(Context context) {
        helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
        gson = new Gson();
        if(getAll().size()==0){
            //开始时新建3个默认区域
            Area area1=new Area();
            area1.setArea_name("我的房间");

            Area area2=new Area();
            area2.setArea_name("宝贝的房间");

            Area area3=new Area();
            area3.setArea_name("客厅");

            insert(area1);
            insert(area2);
            insert(area3);
        }
    }

    public void insert(Area area) {
        String json = gson.toJson(area.getList());
        ContentValues values = new ContentValues();
        values.put(COL_AREA_NAME, area.getArea_name());
        values.put(COL_AREA_LIST, json);
        db.insert(TABLE_NAME, null, values);
    }

    public void update(Area area){
        String json = gson.toJson(area.getList());
        ContentValues values = new ContentValues();
        values.put(COL_AREA_NAME, area.getArea_name());
        values.put(COL_AREA_LIST, json);
        db.update(TABLE_NAME,values,COL_AREA_ID+"="+area.getAreaId(),null);
    }

    public ArrayList<Area> getAll() {
        ArrayList<Area> areaList = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, ALLCOL, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Area area = new Area();
            area.setAreaId(cursor.getInt(0));
            area.setArea_name(cursor.getString(1));
            String json = cursor.getString(2);
            area.setList(gson.fromJson(json, LightList.class));
            areaList.add(area);
        }
        cursor.close();

        return areaList;
    }




}
