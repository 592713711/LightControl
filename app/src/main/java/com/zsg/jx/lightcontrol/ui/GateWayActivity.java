package com.zsg.jx.lightcontrol.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.model.DeviceGroup;
import com.zsg.jx.lightcontrol.model.WifiDevice;
import com.zsg.jx.lightcontrol.util.BaseTools;
import com.zsg.jx.lightcontrol.util.Config;
import com.zsg.jx.lightcontrol.util.JsonUtil;
import com.zsg.jx.lightcontrol.volley.RequestListener;
import com.zsg.jx.lightcontrol.volley.RequestManager;
import com.zsg.jx.lightcontrol.volley.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GateWayActivity extends BaseActivity implements View.OnClickListener {
    private TextView text_name;
    private TextView text_address;
    private TextView text_lightsnum;
    private TextView text_switchnum;
    private TextView text_sersonnum;
    private Button btn_search;
    private Button btn_share;
    private Button btn_delete;

    public ArrayList<DeviceGroup> datas;
    private WifiDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_way);
        initView();
        initData();

    }

    private void initData() {
        device = (WifiDevice) getIntent().getSerializableExtra("device");
        datas = (ArrayList<DeviceGroup>) getIntent().getSerializableExtra("groups");
        if (device.getName().trim().isEmpty()) {
            text_name.setText("未命名");
        } else {
            text_name.setText(device.getName().trim());
        }
        text_address.setText(device.getAddress());

        for (DeviceGroup group : datas) {
            switch (group.getDeviceType()) {
                case DeviceGroup.LIGHT_TYPE:
                    text_lightsnum.setText(group.getOnLineCount()+"");
                    break;
                case DeviceGroup.SWITCH_TYPE:
                    text_switchnum.setText(group.getOnLineCount()+"");
                    break;
                case DeviceGroup.SENSOR_TYPE:
                    text_sersonnum.setText(group.getOnLineCount()+"");
                    break;
            }
        }
    }

    private void initView() {

        text_name = (TextView) findViewById(R.id.text_seq);
        text_address = (TextView) findViewById(R.id.text_version);
        text_lightsnum = (TextView) findViewById(R.id.text_lights);
        text_switchnum = (TextView) findViewById(R.id.text_switchnum);
        text_sersonnum = (TextView) findViewById(R.id.text_sensornum);

        btn_delete = (Button) findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_delete) {
            //删除设备
            deleteDevice();
        }
    }

    private void deleteDevice() {
        showLoadingDialog(getString(R.string.waiting));
        RequestParams params = new RequestParams();
        params.put("imei", device.getAddress());
        RequestManager.post(Config.URL_UNLINKWIFIDEVICE, GateWayActivity.this, params, new RequestListener() {
            @Override
            public void requestSuccess(String result) {
                closeLoadingDialog();
                JSONObject json;
                try {
                    json = new JSONObject(result);
                    if (!"ok".equals(JsonUtil.getStr(json, Config.STATUS))) {
                        BaseTools.showToastByLanguage(GateWayActivity.this,json);
                    }else {
                        showShortToast(getString(R.string.delete_device_suc));
                        Intent intent = new Intent();
                        intent.putExtra("device",device);
                        setResult(RESULT_OK,intent);
                        finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void requestError(VolleyError e) {
                closeLoadingDialog();
                showComReminderDialog();
            }
        });
    }

    public void doBack(View v){
        finish();
    }
}

