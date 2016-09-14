package com.zsg.jx.lightcontrol.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.zsg.jx.lightcontrol.ICallback;
import com.zsg.jx.lightcontrol.IService;
import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.adapter.ExbandDiffLightAdapter;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.listener.MyOnClickListener;
import com.zsg.jx.lightcontrol.listener.MySeekBarListener;
import com.zsg.jx.lightcontrol.model.Light;
import com.zsg.jx.lightcontrol.model.WifiDevice;
import com.zsg.jx.lightcontrol.service.WifiConnectService;
import com.zsg.jx.lightcontrol.util.Config;
import com.zsg.jx.lightcontrol.util.JsonUtil;
import com.zsg.jx.lightcontrol.util.L;
import com.zsg.jx.lightcontrol.util.Lg;
import com.zsg.jx.lightcontrol.volley.RequestListener;
import com.zsg.jx.lightcontrol.volley.RequestManager;
import com.zsg.jx.lightcontrol.volley.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ControlLightActivity extends BaseActivity implements View.OnClickListener,
        ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener
        , ExpandableListView.OnItemLongClickListener, MySeekBarListener, MyOnClickListener {
    private static final String TAG = "testControlLightActivity";
    private WifiDevice device;

    //只有都得到了 状态和列表 才显示
    private boolean isGetLightStatus = false;       //是否得到了各个灯的状态   （长连接得到）
    private boolean isGetLightList = false;         //是否得到了各个灯的列表    （web得到）
    private byte[] lightStatuList;      //各个灯的状态
    private HashMap<Integer, Integer> lightIndex = new HashMap<>();     //储存所有灯的列表
    private String result = "";

    private int requestCount = 0;

    private ExpandableListView expandableListView;
    private LinkedList<String> fatherList;
    private List<LinkedList<Light>> childList;
    private ExbandDiffLightAdapter adapter;
    private LinkedList<Light> lightData;

    private int loadLight_id = 0;
    private Set<Integer> alreadyLoadSet = new HashSet<Integer>();


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_light);

        device = (WifiDevice) getIntent().getSerializableExtra("device");
        if (device == null)
            finish();

        showLoadingDialog(getString(R.string.data_loading));
        Intent intent = new Intent(this, WifiConnectService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

        initView();
        requestLightList();
        initView();

    }

    private void initView() {
        expandableListView = (ExpandableListView) findViewById(R.id.list);
        // 设置默认图标为不显示状态
        expandableListView.setGroupIndicator(null);
        expandableListView.setOnGroupClickListener(this);
        expandableListView.setOnChildClickListener(this);
        expandableListView.setOnItemLongClickListener(this);

        childList = new LinkedList<LinkedList<Light>>();
        fatherList = new LinkedList<String>();
        lightData = new LinkedList<>();

        // 设置默认图标为不显示状态
        expandableListView.setGroupIndicator(null);
        adapter = new ExbandDiffLightAdapter(fatherList, childList, ControlLightActivity.this,
                expandableListView, this, this);
        expandableListView.setAdapter(adapter);
        //获取灯泡列表
        initData(lightData);

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

        // 为列表绑定数据源
        expandableListView.setAdapter(adapter);

    }

    public void initData(LinkedList<Light> temlist) {
        childList.clear();
        fatherList.clear();
        fatherList.add(getResources().getString(R.string.use_light));


        int onLineCount = 0;
        for (Light light : temlist) {
            if (light.getLightStatu() == 3)
                onLineCount++;
        }

        Light total = new Light("0", "0", (byte) onLineCount);
        LinkedList<Light> linklight = new LinkedList<>();
        total.setLightness("1");
        linklight.add(total);
        childList.add(linklight);

        for (Light light : temlist) {
            LinkedList<Light> linklight1 = new LinkedList<>();
            linklight1.add(light);
            childList.add(linklight1);
            fatherList.add(getResources().getString(R.string.useless_light));
        }

        adapter.notifyDataSetChanged();

   /*     Light light3=new Light("1","1", (byte) 3);
        LinkedList<Light> linklight2=new LinkedList<>();
        linklight2.add(light3);
        childList.add(linklight2);

        Light light5=new Light("1","1", (byte) 3);
        LinkedList<Light> linklight3=new LinkedList<>();
        linklight3.add(light5);
        childList.add(linklight3);  */

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            MyApplication.getInstance().mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyApplication.getInstance().mService = IService.Stub.asInterface(service);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Lg.i(TAG, " MyApplication.getInstance().mService:" + MyApplication.getInstance().mService);
                        MyApplication.getInstance().mService.clearCmdList();
                        MyApplication.getInstance().mService.registerCallback(mCallback);
                        MyApplication.getInstance().mService.getLightList(device.getAddress());

                    } catch (RemoteException e) {
                        Lg.i(TAG, "" + e);
                    }
                }
            });
        }

    };


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
       /*     if (NetStatuCheck.checkGPRSState(ControlLightActivity.this).equals("unavailable") &&
                    MyApplication.getInstance().isSocketConnectBreak) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (getTopActivity() != null) {
                            if (getTopActivity().contains("GroupLightActivity")) {
                                showComReminderDialog();
                            }
                        }
                    }
                });

            }*/
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

            //灯泡列表请求失败
            if (cmd.equals(WifiConnectService.GET_LIGHT_LIST_CMD) && getTopActivity() != null) {
                L.i(TAG, "灯泡列表请求失败");
                if (getTopActivity().contains("ControlLightActivity")) {
                    L.i(TAG, "灯泡列表请求");
                    //重新请求
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (MyApplication.getInstance().mService != null && device != null) {
                                    if (requestCount > 2) {
                                        requestCount = 0;
                                        closeLoadingDialog();
                                        showLongToast("获取灯泡数据失败，检查网络");
                                        return;
                                    }
                                    requestCount++;
                                    MyApplication.getInstance().mService.clearCmdList();

                                    //获取wifi下的灯泡列表
                                    MyApplication.getInstance().mService.getLightList(device.getAddress());
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }

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
            L.i(TAG, "onGetLightList");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (list != null) {
                        isGetLightList = true;
                        lightStatuList = list;
                        if (isGetLightList && isGetLightStatus) {
                            updateData(result);
                        }
                    } else {
                        showShortToast(getString(R.string.device_link_no_light));
                    }
                }
            });

        }

        @Override
        public void onSetBrightChromeRsp(String imei, final int ret) throws RemoteException {
            L.i(TAG, "onSetBrightChromeRsp->>" + "imei:" + imei + "   ret:" + ret);
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
            L.i(TAG, "onGetBrightChromeRsp->>" + "index:" + index + "bright:" + bright + "chrome:" + chrome);
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
                            expandableListView.expandGroup(i,true);
                            break;
                        }
                    }
                    initData(lightData);


                }
            });
        }

        @Override
        public void onPairLightRsp(final String imei, int ret) throws RemoteException {

        }
    };


    //更新灯泡数据
    private void updateData(String result) {
        closeLoadingDialog();
        requestCount = 0;
        //isGetLightStatus = false;
        //isGetLightList = false;
        if (result != null && (result.trim()).length() != 0) {
            try {
                JSONObject json = new JSONObject(result);
                if (JsonUtil.getInt(json, Config.CODE) != 1) {
                    ControlLightActivity.this.showLongToast(JsonUtil.getStr(json, Config.ERRORCN));
                } else {
                    JSONArray lightsList = json.getJSONArray("lights");
                    lightData = new LinkedList<>();
                    int index = 1;
                    String name = "";
                    Lg.i(TAG, "lightStatuList.length-->>" + lightStatuList.length);
                    for (int i = 0; i < lightsList.length(); i++) {
                        JSONObject ob = lightsList.getJSONObject(i);
                        if (ob.has("index")) {
                            index = ob.getInt("index");
                        }
                        if (ob.has("name")) {
                            name = ob.getString("name");
                        } else {
                            name = "unkown";
                        }
                        Light light = new Light();
                        light.setId("" + index);
                        Lg.i(TAG, "light_index-->>" + index + "   lightStatuList.length:" + lightStatuList.length);
                        if (index <= lightStatuList.length) {
                            light.setLightStatu(lightStatuList[index - 1]);
                            light.setName(name);
                        }
                        //在线或者离线状态
                        if (light.getLightStatu() != 0) {
                            lightIndex.put(index, lightData.size());
                            Lg.e(TAG, "lightIndex.put(index, temlist.size()->>" + index + "  " + lightData.size());
                            lightData.add(light);
                        }
                    }
                    //childList.add(temlist);
                    initData(lightData);

                    //更新视图项
                    Log.e(TAG, "xxxxxxxxxxxxxxxxxxxxtemlist:" + lightData.toString());

                }


                //adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Lg.i(TAG, "" + e.toString());
                e.printStackTrace();
            }
        }

    }


    /**
     * 获取灯泡列表
     */
    public void requestLightList() {

        if (device == null)
            return;


        //得到该设备在web服务器上的的灯泡列表
        RequestParams params = new RequestParams();
        params.put("imei", device.getAddress());
        RequestManager.post(Config.URL_GETLIGHTLIST, ControlLightActivity.this, params, new RequestListener() {
            @Override
            public void requestSuccess(String json) {
                Log.d(TAG, json);
                isGetLightStatus = true;
                result = json;
                if (isGetLightList && isGetLightStatus) {
                    updateData(result);
                }
            }

            @Override
            public void requestError(VolleyError e) {
                if (e.networkResponse == null) {
                    Toast.makeText(ControlLightActivity.this, getString(R.string.getlightlist_fail), Toast.LENGTH_SHORT).show();
                    ControlLightActivity.this.showComReminderDialog();
                }
            }
        });


    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        return false;
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


    @Override
    protected void onDestroy() {
        Lg.i(TAG, "onDestroy");
        if (mConnection != null) {
            try {
                L.i(TAG, "onDestroy->>unregisterCallback");
                MyApplication.getInstance().mService.unregisterCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        Lg.i(TAG, "onDestroy->>unbindService");
        super.onDestroy();
    }


    /**
     * 停止滑动进度条回调
     *
     * @param light_id
     * @param lightness
     * @param chrome
     */
    @Override
    public void stopTouch(String light_id, int lightness, int chrome) {
        L.i(TAG, "xxxxx移动了进度条: " + light_id + " " + lightness + " " + chrome);
        //发送改变亮度色温命令
        //showLoadingDialog(getResources().getString(R.string.cmd_sending));
        try {
            if (Integer.parseInt(light_id) != 0) {
                //改变一个灯泡
                loadLight_id = Integer.parseInt(light_id);
                MyApplication.getInstance().mService.setBrightChrome(device.getAddress(), loadLight_id, lightness, 255-chrome);
            } else {
                //改变所有灯泡
                for (Light light : lightData) {
                    if (light.getLightStatu() == 3)
                        MyApplication.getInstance().mService.setBrightChrome(device.getAddress()
                                , Integer.parseInt(light.getId()), lightness, 255-chrome);

                }
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
                //请求获取灯泡亮度值
                MyApplication.getInstance().mService.getBrightChrome(device.getAddress(), index);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void doBack(View v){
        finish();
    }
}
