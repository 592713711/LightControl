package com.zsg.jx.lightcontrol.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zsg on 2016/8/17.
 */
public class JsonUtil {
    /**
     * 从json对象中得到，对应的String数据
     *
     * @param json
     *            json对象
     * @param keyname
     *            对应的键名
     * @return 对应的String类型的键值
     */
    public static String getStr(JSONObject json, String keyname) {
        String result = "";
        try {
            result = json.getString(keyname);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 从json对象中得到，对应的integer数据
     *
     * @param json
     *            json对象
     * @param keyname
     *            对应的键名
     * @return 对应的integer类型的键值
     */
    public static int getInt(JSONObject json, String keyname) {
        // TODO Auto-generated method stub
        int result = 0;
        try {
            result = json.getInt(keyname);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 从json对象中得到，对应的long数据
     *
     * @param json
     *            json对象
     * @param keyname
     *            对应的键名
     * @return 对应的long类型的键值
     */
    public static Long getLong(JSONObject json, String keyname) {
        // TODO Auto-generated method stub
        Long result = null;
        try {
            result = json.getLong(keyname);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }
}
