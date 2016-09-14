package com.zsg.jx.lightcontrol.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.bin.User;
import com.zsg.jx.lightcontrol.dao.UserDao;
import com.zsg.jx.lightcontrol.util.Config;
import com.zsg.jx.lightcontrol.util.PreferenceUtil;
import com.zsg.jx.lightcontrol.util.UpdateUserInfo;
import com.zsg.jx.lightcontrol.volley.RequestListener;

import org.json.JSONException;
import org.json.JSONObject;


public class NickNameActivity extends BaseActivity implements View.OnClickListener{
    private static final String TAG ="NickNameActivity" ;
    private boolean D=true;
    private ImageView btn_back;
    private TextView text_save;
    private EditText text_nickname;
    private String nickname;
    private User user;
    private UserDao userDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nick_name);
        userDao=MyApplication.getInstance().getUserDao();
        user= userDao.queryById(MyApplication.getInstance().getPreferenceUtil().getUid());
        initView();
        initData();
    }

    private void initView(){
        btn_back= (ImageView) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);

        text_save= (TextView) findViewById(R.id.text_save);
        text_save.setOnClickListener(this);

        text_nickname= (EditText) findViewById(R.id.edit_nickname);

    }

    private void initData(){
        //获得焦点
        text_nickname.setFocusable(true);
        text_nickname.setFocusableInTouchMode(true);
        nickname= user.getName();
        if(nickname==null||nickname.equals("")){
            //不做任何处理
        }else {
            text_nickname.setText(nickname);
            text_nickname.setSelection(nickname.length());
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_back:
               finish();
                break;
            case R.id.text_save:
                if(D) Log.d(TAG,"click nickname save");
                String newNickname=text_nickname.getText().toString().trim();
                if(newNickname.length()>16){
                    Toast.makeText(this, R.string.nickname_tolong,Toast.LENGTH_SHORT).show();
                }else {
                    if(newNickname.equals(nickname)){
                        //不做任何的处理
                    }else {
                        if(D) Log.d(TAG,newNickname);
                        user.setName(newNickname);
                        showLoadingDialog();
                        UpdateUserInfo.UpdateUserInfo(user,requestListener(),NickNameActivity.this);
                    }
                }
                break;
        }

    }

    private RequestListener requestListener() {
        return new RequestListener() {
            @Override
            public void requestSuccess(String result) {
                closeLoadingDialog();
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getInt(Config.CODE) == 1) {
                        userDao.update(user);
                        showLongToast(getString(R.string.updateinfo_success));
                        finish();
                    } else {
                        showLongToast(json.getString(Config.MSG));
                        user = userDao.queryById(user.getId());
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void requestError(VolleyError e) {
                closeLoadingDialog();
                if(e.networkResponse==null){
                    showShortToast(getString(R.string.network_error));
                }
            }
        };
    }
}
