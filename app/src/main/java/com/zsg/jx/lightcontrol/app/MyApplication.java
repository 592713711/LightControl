package com.zsg.jx.lightcontrol.app;

import android.app.Activity;
import android.app.Application;

import com.zsg.jx.lightcontrol.IService;
import com.zsg.jx.lightcontrol.dao.AreaDao;
import com.zsg.jx.lightcontrol.dao.ThemeDao;
import com.zsg.jx.lightcontrol.dao.UserDao;
import com.zsg.jx.lightcontrol.util.L;
import com.zsg.jx.lightcontrol.util.Net;
import com.zsg.jx.lightcontrol.util.PreferenceUtil;

import java.util.LinkedList;

public class MyApplication extends Application {

    public LinkedList<Activity> activityList = new LinkedList<Activity>();
    private static MyApplication mApplication;
    public static String mToken = "";

    public boolean isSocketConnectBreak = true;

    public boolean isFirstLongCon = false;
    public IService mService;
    //长连接 是否已经登录成功
    public boolean longConnected=false;


    //工具类
    private UserDao userDao;
    private ThemeDao themeDao;
    private AreaDao areaDao;
    private PreferenceUtil preferenceUtil;

    private Net net;

    private boolean DEBUG=true;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mApplication = this;
        L.init(DEBUG,"zsg");
        //异常重启
//        AppCrashHandler ch = AppCrashHandler.getInstance();
//        ch.init(getApplicationContext());
    }

    public static MyApplication getInstance() {
        return mApplication;
    }

    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void removeActivity(Activity activity) {
        activityList.remove(activity);
    }

    public void exit() {
        for (Activity activity : activityList) {
            activity.finish();
        }
        //PreferenceUtil.getInstance(this).setString(PreferenceUtil.CITYID, "0");
        System.exit(0);
    }

    @Override
    public void onTerminate() {
        // TODO Auto-generated method stub
        //ImageLoaderUtil.stopload(instance);
        super.onTerminate();
    }

    public synchronized UserDao getUserDao(){
        if(userDao==null){
            userDao=new UserDao(this);
        }
        return userDao;
    }

    public synchronized PreferenceUtil getPreferenceUtil(){
        if(preferenceUtil==null)
            preferenceUtil=new PreferenceUtil(this);
        return preferenceUtil;
    }

    public synchronized Net getNet(){
        if(net==null)
            net=Net.getInstance(this);
        return net;
    }

    public synchronized ThemeDao getThemeDao(){
        if(themeDao==null)
            themeDao=new ThemeDao(this);
        return themeDao;
    }

    public synchronized AreaDao getAreaDao(){
        if(areaDao==null)
            areaDao=new AreaDao(this);
        return areaDao;
    }

}
