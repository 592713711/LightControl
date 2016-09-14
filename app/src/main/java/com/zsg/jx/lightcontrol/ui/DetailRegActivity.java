package com.zsg.jx.lightcontrol.ui;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.util.BaseTools;
import com.zsg.jx.lightcontrol.util.Config;
import com.zsg.jx.lightcontrol.util.JsonUtil;
import com.zsg.jx.lightcontrol.volley.RequestListener;
import com.zsg.jx.lightcontrol.volley.RequestManager;
import com.zsg.jx.lightcontrol.volley.RequestParams;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailRegActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "testDetailRegActivity";
    private Button registerbtn;
    private EditText nameedit;
    private EditText passwordedit;
    private EditText passwordagainedit;
    private RadioGroup sexgroup;
    //    private TextView protocoltv;
    private String phone;
    private String code = "";
    private String name;
    private String password;
    private String password1;
    private String sex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_reg);

        initView();
    }

    private void initView() {
        nameedit = (EditText) findViewById(R.id.et_name);
        passwordedit = (EditText) findViewById(R.id.et_password);
        passwordagainedit = (EditText) findViewById(R.id.et_sure_password);
        sexgroup = (RadioGroup) findViewById(R.id.register_sex);
        registerbtn = (Button) findViewById(R.id.btn_complete);

        registerbtn.setOnClickListener(this);
        phone = getIntent().getStringExtra("phone");
        code = getIntent().getStringExtra("code");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_complete) {
            if (checkdata()) {
                showLoadingDialog();
                requestRegister();

            }
        }


    }

    public void requestRegister() {
        RequestParams params = new RequestParams();
        params.put(Config.NAME, phone);
        params.put(Config.PASSWORD, password);
        params.put(Config.PHONE, phone);
        params.put(Config.SEX, sex);
        params.put(Config.PASSWORD, password);
        params.put(Config.REPASSWORD, password1);
        params.put("checknum", code);
        RequestManager.post(Config.URL_REGISTER, this, params, requestRegisterListener());
    }

    private boolean checkdata() {
        // +86
        if (phone.length() == 14) {
            phone = phone.substring(3);
        }

        name = nameedit.getText().toString().trim();
        password = passwordedit.getText().toString().trim();
        password1 = passwordagainedit.getText().toString().trim();

        sex = sexgroup.getCheckedRadioButtonId() == R.id.radioMale ? "1" : "0";

        if (phone.equals("")) {
            showShortToast(getString(R.string.phone_not_empty));
            return false;
        } else if (name.equals("")) {
            showShortToast(getString(R.string.name_not_empty));
            return false;
        } else if (password.equals("")) {
            showShortToast(getString(R.string.pwd_not_empty));
            return false;
        } else if (password1.equals("")) {
            showShortToast(getString(R.string.ensure_pwd_not_empty));
            return false;
        } else if (phone.length() != 11) {
            showShortToast(getString(R.string.phone_format_not_match));
            return false;
        } else if (!password.equals(password1)) {
            showShortToast(getString(R.string.pwd_ensurepwd_not_match));
            return false;
        } else if (password.length() < 6 || password.length() > 16) {
            showShortToast(getString(R.string.pwd_length_not_right));
            return false;
        }
        return true;
    }


    private RequestListener requestRegisterListener() {
        return new RequestListener() {
            @Override
            public void requestSuccess(String result) {
                Log.d(TAG, "json：" + result);
                closeLoadingDialog();
                try {
                    JSONObject json = new JSONObject(result);
                    if (JsonUtil.getStr(json, Config.STATUS) != null && !"ok".equals(JsonUtil.getStr(json, Config.STATUS))) {
                        BaseTools.showToastByLanguage(DetailRegActivity.this, json);
                    } else {
                        showShortToast(getString(R.string.resgister_ok));
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
                // HttpStatus.SC_UNAUTHORIZED=401  未授权 登录失败
                if (e.networkResponse == null) {
                    Toast.makeText(DetailRegActivity.this, getString(R.string.net_fail), Toast.LENGTH_LONG).show();
                    return;
                }

            }
        };
    }

    public void doBack(View v){
        onBackPressed();
    }
}
