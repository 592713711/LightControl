package com.zsg.jx.lightcontrol.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.android.volley.VolleyError;
import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.listener.EditTextFocusListener;
import com.zsg.jx.lightcontrol.listener.EditTextTouchListener;
import com.zsg.jx.lightcontrol.util.BaseTools;
import com.zsg.jx.lightcontrol.util.Config;
import com.zsg.jx.lightcontrol.util.JsonUtil;
import com.zsg.jx.lightcontrol.volley.RequestListener;
import com.zsg.jx.lightcontrol.volley.RequestManager;
import com.zsg.jx.lightcontrol.volley.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "testRegisterActivity";
    private ImageView iv_btn_left;
    private EditText et_phone;
    private EditText et_verCode;
    private Button btn_next;
    private Button btn_getCode;

    private String phone;
    private String code = "";

    private Timer mTimer;   //定时器 用来显示验证码下次发送等待时间

    private Handler timerHandler = new Handler() {
        public void handleMessage(Message msg) {
            int num = msg.what;
            btn_getCode.setText(num
                    + getString(R.string.second));
            if (num == -1) {
                mTimer.cancel();
                btn_getCode.setEnabled(true);
                btn_getCode.setText(R.string.register_button_code);
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //初始化视图
        initView();
        //设置监听器
        setListener();
    }

    private void initView() {
        iv_btn_left = (ImageView) findViewById(R.id.iv_btn_left);
        et_phone = (EditText) findViewById(R.id.et_phone);
        et_verCode = (EditText) findViewById(R.id.et_verCode);
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_getCode = (Button) findViewById(R.id.btn_getCode);

        //判断是否具有读短信权限 （6.0需要）
        et_phone.setText(getLocalPhoneNumber());
        btn_next.setEnabled(false);
    }

    private void setListener() {
        iv_btn_left.setOnClickListener(this);
        btn_next.setOnClickListener(this);
        //设置触摸监听
        et_phone.setOnTouchListener(new EditTextTouchListener(RegisterActivity.this, et_phone));
        //设置焦点监听
        et_phone.setOnFocusChangeListener(new EditTextFocusListener(RegisterActivity.this, et_phone));

        btn_getCode.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_btn_left:
                //结束当前活动,返回到登录界面
                //finish();
                onBackPressed();
                break;
            case R.id.btn_next:
                phone = et_phone.getText().toString().trim();
                if (!BaseTools.isPhoneNumber(phone)) {
                    showShortToast(getString(R.string.phone_format_not_match));
                    return;
                }

                String inputcode = et_verCode.getText().toString().trim();
                if (inputcode.equals("")) {
                    showShortToast(getString(R.string.code_not_empty));
                    return;
                }

                if (!inputcode.equals(code)) {
                    showShortToast(getString(R.string.code_not_right));
                    return;
                }

                finish();
                Intent intent = new Intent(RegisterActivity.this, DetailRegActivity.class);
                intent.putExtra("phone", phone);
                intent.putExtra("code", code);
                startActivity(intent);
                break;
            case R.id.btn_getCode:
                //发送验证码
                phone = et_phone.getText().toString().trim();
                if (!BaseTools.isPhoneNumber(phone)) {
                    showShortToast(getString(R.string.phone_format_not_match));
                    return;
                }
                btn_getCode.setText(getString(R.string.is_sending));
                btn_getCode.setEnabled(false);
                requestLogin();
                break;
        }
    }

    //得到本地号码
    private String getLocalPhoneNumber() {
        //判断是否具有读短信权限 （6.0需要）
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
           return "";
        }
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String phoneId = tm.getLine1Number();
        return phoneId;
    }

    private void requestLogin() {
        RequestParams params = new RequestParams();
        RequestManager.get(Config.URL_CHECKMOBILE + "?mobile=" + phone + "&type=1", this, params, requestListener());
    }

    private RequestListener requestListener() {
        return new RequestListener() {
            @Override
            public void requestSuccess(String result) {
                Log.d(TAG, "json：" + result);
                try {
                    JSONObject json = new JSONObject(result);
                    if (!JsonUtil.getStr(json, Config.STATUS).equals("ok")) {
                        btn_next.setEnabled(false);
                        BaseTools.showToastByLanguage(RegisterActivity.this, json);
                        btn_getCode.setEnabled(true);
                        btn_getCode.setText(R.string.register_button_code);
                    } else {
                        code = String.valueOf(JsonUtil.getInt(json, Config.CODE));
                        btn_next.setEnabled(true);
                        showShortToast(getString(R.string.code_has_send));
                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            int num = 90;

                            @Override
                            public void run() {
                                Message msg = new Message();
                                msg.what = num;
                                timerHandler.sendMessage(msg);
                                num--;
                            }
                        }, 0, 1000);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void requestError(VolleyError e) {
                e.printStackTrace();
                // HttpStatus.SC_UNAUTHORIZED=401  未授权 登录失败
                if (e.networkResponse == null) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.net_fail), Toast.LENGTH_LONG).show();
                    btn_next.setEnabled(false);
                    btn_getCode.setEnabled(true);
                    btn_getCode.setText(R.string.register_button_code);
                    return;
                }

            }
        };
    }


}


