package com.zsg.jx.lightcontrol.ui;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.adapter.AllDeviceAdapter;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.model.DeviceGroup;
import com.zsg.jx.lightcontrol.model.Light;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * 所有设备分组碎片
 * Created by zsg on 2016/8/15.
 */
@SuppressLint("ValidFragment")
public class AllDeviceFragment extends Fragment implements AdapterView.OnItemClickListener {
    private Context context;
    private ListView mlistView;
    private AllDeviceAdapter adapter;
    public ArrayList<DeviceGroup> datas;
    private HomeFragment homeFragment;

    public AllDeviceFragment(Context context, HomeFragment fragment) {
        this.context = context;
        this.homeFragment = fragment;
    }

    public AllDeviceFragment() {

    }

    public ArrayList<DeviceGroup> getDatas(){
        return datas;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_all_device, container, false);
        initView(v);
        return v;
    }


    private void initView(View v) {
        mlistView = (ListView) v.findViewById(R.id.device_list);
        datas = new ArrayList<>();
        initData();
        adapter = new AllDeviceAdapter(context);
        adapter.updateData(datas);
        mlistView.setAdapter(adapter);
        mlistView.setOnItemClickListener(this);
    }

    private void initData() {

        DeviceGroup group = new DeviceGroup(1, 0, 0);
        DeviceGroup group2 = new DeviceGroup(2, 0, 0);
        DeviceGroup group3 = new DeviceGroup(3, 0, 0);

        datas.add(group);
        datas.add(group2);
        datas.add(group3);
    }

    public void updateDeviceList(LinkedList<Light> temlist) {
        datas.clear();

        //更新灯泡列表
        int lineCount = 0;
        for (Light light : temlist) {
            if (light.getLightStatu() == 3)
                lineCount++;
        }
        //灯泡列表
        DeviceGroup group = new DeviceGroup(1, lineCount, temlist.size());

        DeviceGroup group2 = new DeviceGroup(2, 0, 0);
        DeviceGroup group3 = new DeviceGroup(3, 0, 0);

        datas.add(group);
        datas.add(group2);
        datas.add(group3);

        adapter.updateData(datas);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            DeviceGroup group=datas.get(position);
            if(group.getOnLineCount()==0) {
                Toast.makeText(context,"暂无在线灯泡",Toast.LENGTH_SHORT).show();
                return;
            }

                //打开灯控活动
            if (homeFragment.currentDevice != null && MyApplication.getInstance().longConnected) {
                Intent intent = new Intent(context, ControlLightActivity.class);
                intent.putExtra("device", homeFragment.currentDevice);
                startActivity(intent);
            }

        }
    }


}
