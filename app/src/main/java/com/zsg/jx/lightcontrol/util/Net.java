package com.zsg.jx.lightcontrol.util;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
/**
 * Volley访问网络请求操作类
 * Created by zsg on 2016/8/16.
 */
public class Net {

    /**
     * 发布版ip
     */
    public static String IP = "112.74.23.39:4000";
    public static String SERVER = "http://" + IP + "/";
    public static String URL_LOGIN = "http://" + IP + "/server/User/login";
    public static String URL_REGISTER = "http://" + IP + "/server/User/register";
    public static String URL_CHANGPASSWORD = "http://" + IP + "/server/User/changePassword";
    public static String URL_CHECKMOBILE = "http://" + IP + "/server/User/getcheckmobile";
    public static String URL_EDITPROFILE = "http://" + IP + "/server/User/eidtprofile";
    public static String URL_FORGETPASSWORD = "http://" + IP + "/server/User/forgetPassword";
    public static String URL_RESETPASSWORD = "http://" + IP + "/server/User/resetPassword";
    public static String URL_STATICPAGE = "http://" + IP + "/server/User/staticPage";
    public static String URL_UPLOADUSERIMAGE = "http://" + IP + "/server/User/uploadUserImage";
    public static String URL_ANDROIDUPDATE = "http://" + IP + "/ble/updateSoftware";
    public static String URL_LINKWIFIDEVICE = "http://" + IP + "/server/User/linkWifiDevice";
    public static String URL_GETWIFIDEVICE = "http://" + IP + "/server/User/getWifiDevice";
    public static String URL_UNLINKWIFIDEVICE = "http://" + IP + "/server/User/unlinkWifiDevice";
    public static String URL_GETWIFIDEVICELIST = "http://" + IP + "/server/User/getWifiDeviceList";
    public static String URL_UPDATEWIFILOGINSTATUS = "http://" + IP + "/server/User/updateWifiLoginStatus";
    public static String URL_GETLIGHTLIST = "http://" + IP + "/server/User/getLightList";
    public static String URL_ADDLIGHT = "http://" + IP + "/server/User/addLight";
    public static String URL_DELETELight = "http://" + IP + "/server/User/deleteLight";
    public static String URL_UPDATELIGHT = "http://" + IP + "/server/User/updateLight";

    private static Net instance;
    private RequestQueue queen;



    private Net(Context context) {
        //磁盘缓存
        Cache cache = new DiskBasedCache(context.getCacheDir(), 10 * 1024 * 1024);
        //网络栈
        Network network = new BasicNetwork(new HurlStack());
        //创建请求队列
        queen = new RequestQueue(cache, network);
        queen.start();
    }

    public RequestQueue getQueen() {
        return queen;
    }

    public void addRequestToQueen(Request request) {
        queen.add(request);
    }

    /**
     * 获得唯一实例
     *
     * @return
     */
    public static synchronized Net getInstance(Context context) {
        if (instance == null) {
            instance = new Net(context);
        }
        return instance;
    }
}

