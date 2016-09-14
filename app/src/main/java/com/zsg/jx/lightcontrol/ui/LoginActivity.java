package com.zsg.jx.lightcontrol.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import com.zsg.jx.lightcontrol.view.TextURLView;
import com.zsg.jx.lightcontrol.volley.RequestListener;
import com.zsg.jx.lightcontrol.volley.RequestManager;
import com.zsg.jx.lightcontrol.volley.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "testLoginActivity";
    private RelativeLayout rl_user;
    private Button mLogin;
    private Button register;
    private TextURLView mTextViewURL;
    private EditText etAccount;
    private EditText etPassword;
    private String phone;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //初始化视图
        initView();
        initTvUrl();
        init();
    }

    public void initView() {
        rl_user = (RelativeLayout) findViewById(R.id.rl_user);
        mLogin = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
        mTextViewURL = (TextURLView) findViewById(R.id.tv_forget_password);
        etAccount = (EditText) findViewById(R.id.account);
        etPassword = (EditText) findViewById(R.id.password);
    }

    private void init() {
        Animation anim = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.login_anim);
        anim.setFillAfter(true);
        rl_user.startAnimation(anim);
        mLogin.setOnClickListener(this);
        register.setOnClickListener(this);
        mTextViewURL.setOnClickListener(this);
    }

    private void initTvUrl() {
        mTextViewURL.setText(R.string.forget_password);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                if (checkdata()) {

                    requestLogin();
                    showLoadingDialog();
                }
                break;
            case R.id.register:
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_forget_password:
                // Intent intent2=new Intent(LoginActivity.this,ForgetPwdActivity.class);
                // startActivity(intent2);
                break;

        }
    }

    private boolean checkdata() {
        boolean isright = false;
        phone = etAccount.getText().toString().trim();
        // +86
        if (phone.length() == 14) {
            phone = phone.substring(3);
        }

        password = etPassword.getText().toString().trim();

        if (phone == null || phone.equals("")) {
            showLongToast(getString(R.string.phone_not_empty));
        } else if (phone.length() != 11) {
            showLongToast(getString(R.string.phone_format_not_match));
        } else if (password == null || password.equals("")) {
            showLongToast(getString(R.string.pwd_not_empty));
        } else {
            isright = true;
        }

        return isright;
    }

    private void requestLogin() {
        RequestParams params = new RequestParams();
        params.put(Config.PHONE, phone);
        params.put(Config.PASSWORD, password);
        RequestManager.post(Config.URL_LOGIN, this, params, requestListener());
    }

    private RequestListener requestListener() {
        return new RequestListener() {
            @Override
            public void requestSuccess(String result) {
                Log.d(TAG, "json：" + result);
                closeLoadingDialog();
                try {
                    JSONObject json = new JSONObject(result);
                    if (!"ok".equals(JsonUtil.getStr(json, Config.STATUS))) {
                        //登录失败
                        BaseTools.showToastByLanguage(LoginActivity.this, json);
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
                        UserDao userDao=new UserDao(LoginActivity.this);
                        userDao.deleteAll();
                        userDao.insert(user);
                        showLongToast(getString(R.string.login_success));
                        PreferenceUtil preferenceUtil=MyApplication.getInstance().getPreferenceUtil();
                        preferenceUtil.setUid(user.getId());
                        preferenceUtil.getString(PreferenceUtil.PHONE, user.getPhone());
                        preferenceUtil.setToken(user.getToken());
                        MyApplication.getInstance().mToken = user.getToken();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));

                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void requestError(VolleyError e) {
                e.printStackTrace();
                closeLoadingDialog();

                if(e.networkResponse==null) {
                    showComReminderDialog();
                    return;
                }
                // HttpStatus.SC_UNAUTHORIZED=401  未授权 登录失败
                if(e.networkResponse.statusCode==401) {
                    Toast.makeText(LoginActivity.this,getString(R.string.login_fail) , Toast.LENGTH_SHORT).show();
                    return;
                }



            }
        };
    }


}



