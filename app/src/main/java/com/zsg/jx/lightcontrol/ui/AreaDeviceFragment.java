package com.zsg.jx.lightcontrol.ui;

import android.annotation.SuppressLint;
import android.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.adapter.AreaDeviceAdapter;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.dao.AreaDao;
import com.zsg.jx.lightcontrol.model.Area;
import com.zsg.jx.lightcontrol.model.Light;
import com.zsg.jx.lightcontrol.model.LightList;
import com.zsg.jx.lightcontrol.util.L;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * 区域碎片
 * Created by zsg on 2016/8/15.
 */
@SuppressLint("ValidFragment")
public class AreaDeviceFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "testAreaDeviceFragment";
    private HomeActivity context;
    private ArrayList<Area> datas;
    private GridView mgridView;
    private AreaDeviceAdapter adapter;
    private AreaDao areaDao;

    public Handler handler = new Handler();

    public AreaDeviceFragment(HomeActivity context) {
        this.context = context;
        areaDao = MyApplication.getInstance().getAreaDao();
    }

    public AreaDeviceFragment() {
        this(null);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_area_device, container, false);
        initView(v);
        return v;
    }

    private void initView(View v) {
        mgridView = (GridView) v.findViewById(R.id.area_list);
        adapter = new AreaDeviceAdapter(context, listener);
        initData();

        mgridView.setAdapter(adapter);

        mgridView.setOnItemClickListener(this);


    }

    private void initData() {
        datas = areaDao.getAll();
        //添加自定义视图项  名字为空
        Area deviceArea = new Area();
        datas.add(deviceArea);

        adapter.updateData(datas);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Area area = datas.get(position);
        if (position != datas.size() - 1) {
            //进入设置灯界面
            if (area.getList().getSize() == 0) {
                //打开选择灯组
                if (isOpen()) {
                    Intent intent = new Intent(context, AddAreaActivity.class);
                    LightList lightlist = new LightList();
                    lightlist.list = context.lightList;
                    intent.putExtra("lightlist", lightlist);
                    intent.putExtra("type", AddAreaActivity.UPDATE_TYPE);
                    intent.putExtra("device", context.currentDevice);
                    intent.putExtra("area",area);
                    startActivity(intent);
                }
            } else {
                //打开控制灯组
            }
        } else {
            //添加新区域
        }
    }

    public OnAreaClickListener listener = new OnAreaClickListener() {
        @Override
        public void onAreaClick(int position) {

            final Area area = datas.get(position);
            L.e(TAG, "点击，" + area.isOpen + "  " + position);

            if (area.getList().getSize() == 0) {
                Toast.makeText(context, "该区域尚未添加灯泡", Toast.LENGTH_SHORT).show();
                return;
            }
            LinkedList<Light> lights = area.getList().list;
            for (int i = 0; i < lights.size(); i++) {
                final Light light = lights.get(i);
                if (area.isOpen)
                    area.isOpen = false;
                else
                    area.isOpen = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //context.showLoadingDialog("请稍等...");
                            int lightness = 0;
                            if (area.isOpen)
                                lightness = Integer.parseInt(light.getLightness());
                            MyApplication.getInstance().mService.setBrightChrome(context.currentDevice.getAddress(), Integer.parseInt(light.getId())
                                    , lightness, Integer.parseInt(light.getColor()));
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }, 500);
            }

            adapter.notifyDataSetChanged();

        }
    };

    //用于监听点击区域开关的接口
    public interface OnAreaClickListener {
        public void onAreaClick(int position);
    }

    //是否可以打开情景模式
    public boolean isOpen() {
        //添加情景
        //打开灯控活动
        if (!MyApplication.getInstance().longConnected) {
            context.showShortToast(context.getString(R.string.str_connect_first));
            return false;
        }

        if (context.currentDevice == null) {
            context.showShortToast(context.getString(R.string.nowifidevice_use));
            return false;
        }

        //更新灯泡列表
        int lineCount = 0;
        for (Light light : context.lightList) {
            if (light.getLightStatu() == 3)
                lineCount++;
        }
        if (lineCount == 0) {
            context.showShortToast(context.getString(R.string.device_link_no_light));
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }
}
