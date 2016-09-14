package com.zsg.jx.lightcontrol.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 记录用户名，密码之类的首选项
 */
public class PreferenceUtil {
    private static final String SP_KEY_FIRST_APP_START_FLAG = "firststart";

    private static PreferenceUtil preference = null;
    private SharedPreferences sharedPreference;
    private String packageName = "";
    public static final String USERNAME = "username"; // 登录名
    public static final String PASSWORD = "password"; // 密码
    public static final String REMINDWORD = "remindword"; // 是否保留密码
    public static final String AUTOLOGIN = "autologin";
    public static final String TIMES = "times";
    public static final String LOCATION = "location";
    public static final String UID = "uid";
    public static final String CITY = "city";
    public static final String CITYID = "cityid";
    public static final String LON = "lon";
    public static final String LAT = "lat";
    public static final String SHIBI = "shibi";
    public static final String PHONE = "phone";
    public static final String TOKEN = "token";


    public PreferenceUtil(Context context) {
        packageName = context.getPackageName() + "_preferences";
        sharedPreference = context.getSharedPreferences(packageName,
                Context.MODE_PRIVATE);
    }

    public String getUid() {
        String value = sharedPreference.getString(UID, "");
        return value;
    }

    public void setUid(String value) {
        Editor edit = sharedPreference.edit();
        edit.putString(UID, value);
        edit.commit();
    }

    public String getToken() {
        String value = sharedPreference.getString(TOKEN, "");
        return value;
    }

    public void setToken(String value) {
        Editor edit = sharedPreference.edit();
        edit.putString(TOKEN, value);
        edit.commit();
    }

    public String getString(String name, String defValue) {
        String value = sharedPreference.getString(name, defValue);
        return value;
    }

    public void setString(String name, String value) {
        Editor edit = sharedPreference.edit();
        edit.putString(name, value);
        edit.commit();
    }

    public int getInt(String name, int defValue) {
        int value = sharedPreference.getInt(name, defValue);
        return value;
    }

    public void setInt(String name, int value) {
        Editor edit = sharedPreference.edit();
        edit.putInt(name, value);
        edit.commit();
    }

    public float getFloat(String name, float defValue) {
        float value = sharedPreference.getFloat(name, defValue);
        return value;
    }

    public void setFloat(String name, float value) {
        Editor edit = sharedPreference.edit();
        edit.putFloat(name, value);
        edit.commit();
    }

    public boolean getFirstAppStartFlag() {
        boolean value = sharedPreference.getBoolean(SP_KEY_FIRST_APP_START_FLAG, true);
        return value;
    }

    public void setFirstAppStartFlag(boolean v) {
        SharedPreferences.Editor ed = sharedPreference.edit();
        ed.putBoolean(SP_KEY_FIRST_APP_START_FLAG, v);
        ed.commit();
    }

    //得到上次保存的wifi密码信息
    public String getWifiPassward() {

       String PASS = sharedPreference.getString("pass", "");
        return PASS;
    }

    /**
     * 将用户的基本信息从Share中清空
     */
    public void clearShared() {
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.clear();
        editor.commit();
    }

    public void setWifiPassward(String wifiPassward) {
        SharedPreferences.Editor ed = sharedPreference.edit();
        ed.putString("pass", wifiPassward);
        ed.commit();
    }
}
