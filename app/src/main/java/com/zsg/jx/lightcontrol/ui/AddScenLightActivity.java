package com.zsg.jx.lightcontrol.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;

import com.zsg.jx.lightcontrol.ICallback;
import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.adapter.ExbandDiffLightAdapter;
import com.zsg.jx.lightcontrol.adapter.ExbandLightAdapter;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.listener.MyOnClickListener;
import com.zsg.jx.lightcontrol.listener.MySeekBarListener;
import com.zsg.jx.lightcontrol.model.Area;
import com.zsg.jx.lightcontrol.model.Light;
import com.zsg.jx.lightcontrol.model.LightList;
import com.zsg.jx.lightcontrol.model.Listitem;
import com.zsg.jx.lightcontrol.model.Theme;
import com.zsg.jx.lightcontrol.model.WifiDevice;
import com.zsg.jx.lightcontrol.service.WifiConnectService;
import com.zsg.jx.lightcontrol.util.L;
import com.zsg.jx.lightcontrol.util.Lg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AddScenLightActivity extends BaseActivity implements View.OnClickListener,
        ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener
        , ExpandableListView.OnItemLongClickListener, MySeekBarListener, MyOnClickListener {
    private static final String TAG = "AddScenLightActivity";
    private ExpandableListView expandableListView;
    private LinkedList<String> fatherList;
    private List<LinkedList<Light>> childList;
    private ExbandLightAdapter adapter;
    private EditText edit_scenname;
    private Button btn_complete;
    private String scename;

    private WifiDevice device;
    private LinkedList<Light> lightList;

    private Handler mHandler = new Handler();
    private int loadLight_id = 0;
    private Set<Integer> alreadyLoadSet = new HashSet<Integer>();

    private int alter_type;     //用于 区分是添加  还是修改
    private int model_type;     //用于 区分是用于 情景模式    还是区域

    public static int THEME_TYPE = 0;
    public static int AREA_TYPE = 1;

    public static int ADD_TYPE = 1;
    public static int UPDATE_TYPE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scenlight);
        //获取灯泡列表
        getData();
        init();
        try {
            MyApplication.getInstance().mService.registerCallback(mCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void init() {
        edit_scenname = (EditText) findViewById(R.id.edit_scename);
        edit_scenname.setText(scename);
        edit_scenname.setEnabled(false);
        btn_complete = (Button) findViewById(R.id.btn_complete);
        btn_complete.setOnClickListener(this);
        expandableListView = (ExpandableListView) findViewById(R.id.list);
        // 设置默认图标为不显示状态
        expandableListView.setGroupIndicator(null);
        expandableListView.setOnGroupClickListener(this);
        expandableListView.setOnChildClickListener(this);
        expandableListView.setOnItemLongClickListener(this);
        // 设置默认图标为不显示状态
        expandableListView.setGroupIndicator(null);
        fatherList = new LinkedList<>();
        childList = new LinkedList<>();
        adapter = new ExbandLightAdapter(fatherList, childList, AddScenLightActivity.this, expandableListView, this, this);
        expandableListView.setAdapter(adapter);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                //先判断点击的组是否在线  和  有没有被加载过
                Light light = childList.get(groupPosition).get(0);
                if (groupPosition != 0) {
                    if (light.getLightStatu() != 3) {
                        showShortToast(getString(R.string.light_offline_remind));
                        expandableListView.collapseGroup(groupPosition);
                        return;
                    } else {
                        if (!alreadyLoadSet.contains(Integer.parseInt(light.getId()))) {
                            expandableListView.collapseGroup(groupPosition);
                            //请求该灯泡数据
                            showLoadingDialog(getString(R.string.data_loading));
                            try {
                                //MyApplication.getInstance().mService.clearCmdList();
                                MyApplication.getInstance().mService.getBrightChrome(device.getAddress(), Integer.valueOf(light.getId()));
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    //在线灯泡为0
                    if (light.getLightStatu() == 0) {
                        showShortToast(getString(R.string.device_link_no_light));
                        expandableListView.collapseGroup(groupPosition);
                        return;
                    }
                }


                for (int i = 0; i < adapter.getGroupCount(); i++) {
                    if (groupPosition != i) {
                        //关闭其他分组
                        expandableListView.collapseGroup(i);
                    }
                }
            }
        });
        //adapter.AddType(R.layout.con_light_group_item);
        //adapter.AddType(R.layout.con_light_group_item2);
        // 为列表绑定数据源
        expandableListView.setAdapter(adapter);


        initData();
    }

    private void initData() {
        childList.clear();
        fatherList.clear();

        Light total = new Light("0", "0", (byte) lightList.size());
        LinkedList<Light> linklight = new LinkedList<>();
        linklight.add(total);
        childList.add(linklight);
        fatherList.add("1");


        for (Light light : lightList) {
            LinkedList<Light> linklight1 = new LinkedList<>();
            linklight1.add(light);
            childList.add(linklight1);
            fatherList.add("2");
        }

        adapter.notifyDataSetChanged();

    }

    private void getData() {
        Intent intent = getIntent();
        scename = intent.getStringExtra("lightname");
        device = (WifiDevice) intent.getSerializableExtra("device");
        LightList temp = (LightList) intent.getSerializableExtra("lightlist");
        lightList = temp.list;
        alter_type = intent.getIntExtra("alter_type", 0);
        model_type = intent.getIntExtra("model_type", 0);


    }


    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_complete:
                if (model_type == THEME_TYPE) {
                    if (alter_type == ADD_TYPE) {
                        //将信息加入到数据库
                        Theme theme = new Theme();
                        theme.setTheme_name(scename);
                        LightList temp = new LightList();
                        temp.list = lightList;
                        theme.setList(temp);
                        MyApplication.getInstance().getThemeDao().insert(theme);
                    } else {

                    }
                    finish();
                } else {
                    Area area = new Area();
                    area.setArea_name(scename);
                    LightList temp = new LightList();
                    temp.list = lightList;
                    area.setList(temp);
                    L.e(TAG,"alter_type:"+alter_type+" "+area.toString());
                    if (alter_type == ADD_TYPE) {
                        MyApplication.getInstance().getAreaDao().insert(area);
                    } else {
                        area.setAreaId(getIntent().getIntExtra("id", 0));
                        MyApplication.getInstance().getAreaDao().update(area);
                    }
                    L.e(TAG,"alter_type:"+alter_type+" "+area.toString());
                    finish();
                }
                break;
        }

    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        adapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }


    private ICallback.Stub mCallback = new ICallback.Stub() {
        @Override
        public void onConnect(String address) throws RemoteException {

        }

        @Override
        public void onDisconnect(String address) throws RemoteException {
            Lg.i(TAG, TAG + "onDisconnect");
            closeLoadingDialog();
            finish();
            //断网重连

        }

        @Override
        public boolean onRead(String address, byte[] val) throws RemoteException {
            return false;
        }

        @Override
        public boolean onWrite(String address, byte[] val) throws RemoteException {
            return false;
        }

        @Override
        public void onNotify(String imei, int type) throws RemoteException {

        }

        @Override
        public void onSwitchRsp(String imei, final boolean ret) throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //打开 关闭 led中开关  服务器响应   关闭对话框
                    closeLoadingDialog();
                }
            });
        }

        @Override
        public void onGetStatusRsp(String imei, final int ret) throws RemoteException {
            Log.i(TAG, "onGetStatusRsp:" + ret);
        }

        @Override
        public void onCmdTimeout(String cmd, String imei) throws RemoteException {
            Lg.i(TAG, "onCmdTimeout");

            if (cmd.equals(WifiConnectService.GET_BRIGHT_CHROME_CMD)) {
                closeLoadingDialog();
                showShortToast(getString(R.string.net_fail));
                adapter.notifyDataSetChanged();
                Lg.i(TAG, "获取灯泡亮度请求失败");
            }

            if (cmd.equals(WifiConnectService.SET_BRIGHT_CHROME_CMD)) {
                closeLoadingDialog();
                showShortToast(getString(R.string.net_fail));
                adapter.notifyDataSetChanged();
                Lg.i(TAG, "改变灯泡亮度失败");
            }


        }

        @Override
        public void onPingRsp(String imei, int ret) throws RemoteException {

        }

        @Override
        public void onGetLightList(String imei, final byte[] list) throws RemoteException {
            Log.i(TAG, "onGetLightList");


        }

        @Override
        public void onSetBrightChromeRsp(String imei, final int ret) throws RemoteException {
            Lg.i(TAG, "onSetBrightChromeRsp->>" + "imei:" + imei + "   ret:" + ret);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    closeLoadingDialog();
                    if (ret == 0) {
                        showShortToast(getResources().getString(R.string.str_success));
                    } else {
                        showShortToast(getResources().getString(R.string.str_fail));
                    }
//                    if (ret != 0) {
                    try {
                        MyApplication.getInstance().mService.getBrightChrome(device.getAddress(), loadLight_id);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
//                    }
                }
            });

        }

        @Override
        public void onGetBrightChromeRsp(String imei, final int index, final int bright, final int chrome) throws RemoteException {
            //得到灯泡信息
            Log.i(TAG, "onGetBrightChromeRsp->>" + "index:" + index + "bright:" + bright + "chrome:" + chrome);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    closeLoadingDialog();
                    alreadyLoadSet.add(index);
                    for (int i = 0; i < childList.size(); i++) {
                        Light light = childList.get(i).get(0);
                        if (light.getId().equals(Integer.toString(index))) {
                            light.setLightness(bright + "");
                            light.setColor(chrome + "");
                            //展开对应子列表
                            expandableListView.expandGroup(i, true);
                            break;
                        }
                    }
                    initData();


                }
            });
        }

        @Override
        public void onPairLightRsp(final String imei, int ret) throws RemoteException {

        }
    };

    /**
     * 停止滑动进度条回调
     *
     * @param light_id
     * @param lightness
     * @param chrome
     */
    @Override
    public void stopTouch(String light_id, int lightness, int chrome) {
        Log.d(TAG, "xxxxx移动了进度条: " + light_id + " " + lightness + " " + chrome);
        //发送改变亮度色温命令
        showLoadingDialog(getResources().getString(R.string.cmd_sending));
        try {
            if (Integer.parseInt(light_id) != 0) {
                //改变一个灯泡
                loadLight_id = Integer.parseInt(light_id);
                MyApplication.getInstance().mService.setBrightChrome(device.getAddress(), loadLight_id, lightness, 255 - chrome);
            } else {
                //改变所有灯泡
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //开关灯泡
    @Override
    public void onClickSwitch(String light_id, boolean status) {
        Log.d(TAG, "点击开关:" + status);
        int index = Integer.parseInt(light_id);
        try {
            if (alreadyLoadSet.contains(index)) {
                //发送开关命令
                showLoadingDialog(getResources().getString(R.string.cmd_sending));
                loadLight_id = Integer.parseInt(light_id);
                if (status)
                    MyApplication.getInstance().mService.setBrightChrome(device.getAddress(), loadLight_id, 128, 128);
                else
                    MyApplication.getInstance().mService.setBrightChrome(device.getAddress(), loadLight_id, 0, 128);

            } else {
                MyApplication.getInstance().mService.getBrightChrome(device.getAddress(), index);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        Lg.i(TAG, "onDestroy");
        if (mCallback != null) {
            try {
                Lg.i(TAG, "onDestroy->>unregisterCallback");
                MyApplication.getInstance().mService.unregisterCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
