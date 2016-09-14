package com.zsg.jx.lightcontrol.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.model.WifiDevice;

import java.util.ArrayList;
import java.util.HashMap;

public class DeviceListActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = "testDeviceListActivity";
    private ListView device_list;
    private ArrayList<WifiDevice> mListDevice;
    private SimpleAdapter adapter;
    private ArrayList<HashMap<String, Object>> data;

    private static final String KEY_NAME = "device_name";
    private static final String KEY_STATUS = "device_status";
    private static final String KEY_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        mListDevice = (ArrayList<WifiDevice>) getIntent().getSerializableExtra("devicelist");
        Log.d(TAG,mListDevice.toString());
        initView();
    }

    private void initView() {
        device_list = (ListView) findViewById(R.id.device_list);
        data = new ArrayList<>();
        initData();
        String from[] = {KEY_NAME, KEY_STATUS, KEY_ADDRESS};
        int to[] = {R.id.tv_device_name, R.id.tv_device_status, R.id.tv_device_address};
        adapter = new SimpleAdapter(this, data, R.layout.device_item, from, to);
        device_list.setAdapter(adapter);
        device_list.setOnItemClickListener(this);


    }

    private void initData() {
        for (WifiDevice device : mListDevice) {
            HashMap<String, Object> map = new HashMap<>();
            if (device.getStatus() == WifiDevice.LOGIN_STATUS) {
                map.put(KEY_STATUS, "在线");
            } else {
                map.put(KEY_STATUS, "离线");
            }
            int count = 1;
            if (device.getName().trim().isEmpty()) {
                map.put(KEY_NAME, "新网关" + count);
                count++;
            } else {
                map.put(KEY_NAME, device.getName().trim());
            }
            String address=parseAddress(device.getAddress());
            map.put(KEY_ADDRESS, address);
            data.add(map);
        }
    }

    private String parseAddress(String address) {
        address=address.substring(0,12);
        String parseAddress="";
        for(int i=0;i<12;i+=2){
            parseAddress+=address.substring(i,i+2);
            parseAddress+=":";
        }

        parseAddress=parseAddress.substring(0,17);
        return parseAddress;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        WifiDevice device = mListDevice.get(position);
        if (device.getStatus() != WifiDevice.LOGIN_STATUS) {
            //showShortToast(getString(R.string.wifidevice_not_online));
            Toast.makeText(DeviceListActivity.this,getString(R.string.wifidevice_not_online),Toast.LENGTH_SHORT);
            return;
        } else {
            Intent intent = getIntent();// 获取启动这个activity的intent
            intent.putExtra("select_device", device);
            DeviceListActivity.this.setResult(RESULT_OK, intent);
            DeviceListActivity.this.finish();
        }
    }
}
