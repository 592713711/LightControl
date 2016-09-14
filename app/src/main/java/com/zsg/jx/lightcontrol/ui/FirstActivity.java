package com.zsg.jx.lightcontrol.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.bin.User;
import com.zsg.jx.lightcontrol.dao.UserDao;
import com.zsg.jx.lightcontrol.util.BaseTools;
import com.zsg.jx.lightcontrol.util.Config;
import com.zsg.jx.lightcontrol.util.JsonUtil;
import com.zsg.jx.lightcontrol.util.PreferenceUtil;
import com.zsg.jx.lightcontrol.view.ComReminderDialog;
import com.zsg.jx.lightcontrol.volley.RequestListener;
import com.zsg.jx.lightcontrol.volley.RequestManager;
import com.zsg.jx.lightcontrol.volley.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 程序进入时 欢迎界面
 * Created by zsg on 2016/8/16.
 */
public class FirstActivity extends BaseActivity {

    private static final String TAG = "testFirstActivity";
    private String phone;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(isFirstLoad()) {
            Intent intent = new Intent(FirstActivity.this, SplashActivity.class);
            FirstActivity.this.startActivity(intent);
            MyApplication.getInstance().getPreferenceUtil().setFirstAppStartFlag(false);
            finish();
            return;
        }
        setContentView(R.layout.activity_first);

        initUser();
    }

    private void initUser() {
        ArrayList<User> list = MyApplication.getInstance().getUserDao().queryAll();

        User user = null;

        if (list != null && !list.isEmpty()) {
            user = list.get(0);
        }


        if (user != null) {
            phone = user.getPhone();
            password = user.getPassword();
            requestLogin();

        } else {
            //进入登录页面
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    startActivity(new Intent(FirstActivity.this, LoginActivity.class));
                }
            }, 2000);
        }
    }


    private void requestLogin() {
        RequestParams params = new RequestParams();
        Log.d(TAG,"登录："+phone+" "+password);
        params.put(Config.PHONE, phone);
        params.put(Config.PASSWORD, password);
        RequestManager.post(Config.URL_LOGIN, this, params, requestListener());
    }

    private RequestListener requestListener() {
        return new RequestListener() {

            @Override
            public void requestSuccess(String result) {
                Log.d(TAG, "json：" + result);
                try {
                    JSONObject json = new JSONObject(result);
                    if (!"ok".equals(JsonUtil.getStr(json, Config.STATUS))) {
                        //登录失败
                        BaseTools.showToastByLanguage(FirstActivity.this, json);
                        startActivity(new Intent(FirstActivity.this, LoginActivity.class));
                    } else {
                        //登录成功
                        JSONObject msgobj = json.getJSONObject("msg");
                        String token = msgobj.getString("token");

                        JSONObject userobj = json.getJSONObject("user");
                        String id = userobj.getString(Config.ID);
                        String name = userobj.getString(Config.NAME);
                        String phone = userobj.getString(Config.PHONE);
                        String sex = userobj.getString(Config.SEX);
                        //String password = userobj.getString(JsonUtil.PASSWORD);
                        String create_time = userobj.getString(Config.CREATE_TIME);

                        String image_thumb = "";
                        String image = "";
                        try {
                            image_thumb = userobj.getString(Config.IMAGE_THUMB);
                            image = userobj.getString(Config.IMAGE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        User user = new User(id, name, phone, sex, password, create_time, image_thumb, image, token);
                        UserDao userDao=new UserDao(FirstActivity.this);
                        userDao.deleteAll();
                        userDao.insert(user);
                        PreferenceUtil preferenceUtil = MyApplication.getInstance().getPreferenceUtil();
                        preferenceUtil.setUid(user.getId());
                        //*******************************
                        preferenceUtil.getString(PreferenceUtil.PHONE, user.getPhone());
                        preferenceUtil.setToken(user.getToken());
                        MyApplication.getInstance().mToken = user.getToken();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(FirstActivity.this, HomeActivity.class));
                                finish();
                            }
                        }, 1000);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void requestError(VolleyError e) {
                e.printStackTrace();
                closeLoadingDialog();
                closeComReminderDialog();
                if (e.networkResponse == null) {
                    final ComReminderDialog comReminderDialog = new ComReminderDialog(FirstActivity.this,
                            getResources().getString(R.string.net_has_breaked)
                            , getResources().getString(R.string.cancel), getResources().getString(R.string.ensure));
                    comReminderDialog.setCanceledOnTouchOutside(false);
                    comReminderDialog.show();
                    comReminderDialog.dialog_cancel.setOnClickListener(new View.OnClickListener() {
                                                                           @Override
                                                                           public void onClick(View v) {
                                                                               comReminderDialog.cancel();
                                                                               finish();
                                                                           }
                                                                       }
                    );
                    comReminderDialog.dialog_submit.setOnClickListener(new View.OnClickListener() {
                                                                           @Override
                                                                           public void onClick(View v) {
                                                                               //进入系统网络开关界面
                                                                               comReminderDialog.cancel();
                                                                               if (android.os.Build.VERSION.SDK_INT > 13) {
                                                                                   startActivity(new Intent(
                                                                                           android.provider.Settings.ACTION_SETTINGS));
                                                                               } else {
                                                                                   startActivity(new Intent(
                                                                                           android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                                                               }
                                                                               finish();
                                                                           }
                                                                       }
                    );
                    return;
                }
                if (e.networkResponse.statusCode == 401) {
                    //登录失败
                    Toast.makeText(FirstActivity.this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(FirstActivity.this, LoginActivity.class));
                    return;
                }


            }
        };
    }

    /**
     * 判断是否是第一次登陆
     */
    private boolean isFirstLoad() {
        return MyApplication.getInstance().getPreferenceUtil().getFirstAppStartFlag();
    }
}
