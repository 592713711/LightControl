package com.zsg.jx.lightcontrol.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.zsg.jx.lightcontrol.ICallback;
import com.zsg.jx.lightcontrol.IService;
import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.model.Light;
import com.zsg.jx.lightcontrol.model.WifiDevice;
import com.zsg.jx.lightcontrol.service.WifiConnectService;
import com.zsg.jx.lightcontrol.util.BaseTools;
import com.zsg.jx.lightcontrol.util.Config;
import com.zsg.jx.lightcontrol.util.DialogUtil;
import com.zsg.jx.lightcontrol.util.JsonUtil;
import com.zsg.jx.lightcontrol.util.L;
import com.zsg.jx.lightcontrol.util.Lg;
import com.zsg.jx.lightcontrol.util.NetStatuCheck;
import com.zsg.jx.lightcontrol.volley.RequestListener;
import com.zsg.jx.lightcontrol.volley.RequestManager;
import com.zsg.jx.lightcontrol.volley.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by zsg on 2016/8/12.
 */
public class HomeActivity extends BaseActivity {

    private static final String TAG = "testHomeActivity";
    private BottomNavigationBar bottomNavigationBar;        //底部导航
    private HomeFragment homeFragment;
    private ThemeFragment themeFragment;
    private SelfFragment selfFrament;
    private FragmentManager fm;
    private FragmentTransaction transaction;

    private TextView mtvTitle;
    private TextView connectbtn;
    private Handler mHandler = new Handler();

    public ArrayList<WifiDevice> mListDevice;

    //存放灯泡状态
    public LinkedList<Light> lightList = new LinkedList<>();
    public WifiDevice currentDevice;

    private static final int DEVICE_LIST_REQ = 1;

    private int requestCount = 0;     //请求灯泡状态次数
    private int linkServerCount = 0;

    private Light addLight;
    private int addGroupIndex;
    //存不同编号的灯，用于更新时候进行替换
    public HashMap<Integer, Integer> lightIndex = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();
        initBottom();

        //请求wifi设备列表
        requestWifiList();

        //  bindTcpConnectService()

    }

    private void initView() {
        mtvTitle = (TextView) findViewById(R.id.tv_title);

        homeFragment = new HomeFragment(this);
        themeFragment = new ThemeFragment(this);
        selfFrament = new SelfFragment();

        fm = getFragmentManager();
        transaction = fm.beginTransaction();
        transaction.add(R.id.content, homeFragment);
        transaction.add(R.id.content, themeFragment);
        transaction.add(R.id.content, selfFrament);
        transaction.commit();

        changeFragment(0);

        connectbtn = (TextView) findViewById(R.id.btn_connect);

        mListDevice = new ArrayList<>();

    }

    /**
     * 绑定服务（IService） 得到远程服务对象
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            L.i(TAG, "onServiceDisconnected");
            MyApplication.getInstance().mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            L.i(TAG,"onServiceConnected");
            MyApplication.getInstance().mService = IService.Stub.asInterface(service);
            L.i(TAG, "MyApplication.getInstance().mService:" + MyApplication.getInstance().mService);
            try {
                MyApplication.getInstance().mService.registerCallback(mCallback);
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
//                    connectSocketDialog();
                    connectLongSocket();
                }
            });
        }
    };

    private ICallback.Stub mCallback = new ICallback.Stub() {
        //收到 服务端发来的登录响应后 回调
        @Override
        public void onConnect(String address) throws RemoteException {
            //只有收到服务端发来的登录响应 才认为是连接成功
            L.i(TAG, "onConnect");
            linkServerCount=0;
            MyApplication.getInstance().longConnected = true;
            mHandler.post(new Runnable() {
                              @Override
                              public void run() {
                                  connectbtn.setText(R.string.has_connect);
                                  closeLoadingDialog();
                                  closeComReminderDialog();
//                  Toast.makeText(DeviceListActivity.this, R.string.str_connect_success, Toast.LENGTH_SHORT).show();
                                  //更新wifi设备状态
                                  if (getTopActivity() != null) {
                                      if (getTopActivity().contains("HomeActivity")) {
                                          //pingWifiDevice();
                                          // homeFragment.requestLightList();
                                          //通过获取灯泡列表 来获取设备状态 有返回则在线  超时离线
                                          getWifiDeviceStatus();
                                      }
                                  }
                              }
                          }

            );
        }

        @Override
        public void onDisconnect(String address) throws RemoteException {
            L.e(TAG, TAG + "onDisconnect");
            MyApplication.getInstance().longConnected = false;
            lightList.clear();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    connectbtn.setText(R.string.no_connect);
                    closeLoadingDialog();
//                    showShortToast(getString(R.string.str_disconnected));
                    if (MyApplication.getInstance().isSocketConnectBreak) {
                        //长连接意外断开重连接
                        //有网络自动连接
                        if (!NetStatuCheck.checkGPRSState(HomeActivity.this).equals("unavailable")) {
                            if (linkServerCount < 4) {
                                linkServerCount++;
                                showLoadingDialog(getString(R.string.reconnect), false);
                                //连接4次
                                try {
                                    MyApplication.getInstance().mService.connect(null);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                linkServerCount=0;
                                showLongToast(getString(R.string.connect_fail));
                            }
                        } else {  //没有网络
                            if (getTopActivity() != null) {
                                if (getTopActivity().contains("HomeActivity")) {
                                    showComReminderDialog();
                                }
                            }

                        }
                    }
                }
            });
        }

        @Override
        public boolean onRead(String address, byte[] val) throws RemoteException {
            L.i("hjq", "onRead called");
            return false;
        }


        @Override
        public boolean onWrite(final String address, byte[] val) throws RemoteException {
            L.i("hjq", "onWrite called");
            return true;
        }

        /**
         * wifi模块当前状态 相当于心跳包
         * @param imei
         * @param type
         * @throws RemoteException
         */
        @Override
        public void onNotify(final String imei, int type) throws RemoteException {
            L.i(TAG, "onNotify called");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //updateWifiDeviceStatus(imei);
                }
            });
        }

        @Override
        public void onSwitchRsp(String imei, boolean ret) throws RemoteException {

        }

        @Override
        public void onGetStatusRsp(String imei, int ret) throws RemoteException {

        }

        /**
         * 命令发送 超时回调
         * @param cmd
         * @param imei
         * @throws RemoteException
         */
        @Override
        public void onCmdTimeout(String cmd, final String imei) throws RemoteException {
            L.i(TAG, "onCmdTimeout");


            if (cmd.equals(WifiConnectService.PAIR_LIGHT_CMD)) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        closeLoadingDialog();
                        showShortToast("关联灯泡失败");
                    }
                });

                return;
            }

            //灯泡列表请求失败
            if (cmd.equals(WifiConnectService.GET_LIGHT_LIST_CMD) && getTopActivity() != null) {
                L.i(TAG, "灯泡列表请求失败");
                if (getTopActivity().contains("HomeActivity")) {
                    //重新请求
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //更新设备状态
                            for (WifiDevice device : mListDevice) {
                                if (device.getAddress().equals(imei)) {
                                    device.setStatus(WifiDevice.LOGOUT_STATUS);
                                    device.list = null;
                                    homeFragment.update(mListDevice, device);
                                    break;
                                }
                            }

                            //showShortToast(getString(R.string.getlightlist_fail));

                        }
                    });

                }
                return;
            }
        }

        @Override
        public void onPingRsp(final String imei, final int ret) throws RemoteException {

        }

        @Override
        public void onGetLightList(final String imei, final byte[] list) throws RemoteException {
            L.e(TAG, "onGetLightList:" + list + " " + imei);
            requestCount = 0;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //    if (list != null) {
                    //       closeLoadingDialog();

                    //更新设备状态
                    for (WifiDevice device : mListDevice) {
                        if (device.getAddress().equals(imei)) {
                            device.setStatus(WifiDevice.LOGIN_STATUS);
                            device.list = list;
                            homeFragment.update(mListDevice, device);
                            break;
                        }
                    }


                    //    } else {
                    //         closeLoadingDialog();
                    //       showShortToast(getString(R.string.device_link_no_light));
                    //    }
                }
            });
        }

        @Override
        public void onSetBrightChromeRsp(String imei, int ret) throws RemoteException {
            L.i(TAG, "onSetBrightChromeRsp");
            if (getTopActivity().contains("HomeActivity")) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        closeLoadingDialog();
                    }
                });
            }
        }

        @Override
        public void onGetBrightChromeRsp(String imei, int index, int bright, int chrome) throws
                RemoteException {

        }

        @Override
        public void onPairLightRsp(final String imei, int ret) throws RemoteException {
            final int res = ret;
            L.i(TAG, "onPairLightRsp:" + ret);
            mHandler.post(new Runnable() {
                              @Override
                              public void run() {
                                  closeLoadingDialog();
                                  if (res != 0) {
                                      showShortToast(getResources().getString(R.string.add_light_error));
                                  } else {
                                      if (addLight != null) {
                                          if (lightIndex.containsKey(Integer.valueOf(addLight.getId()))) {
                                              Lg.i(TAG, "删除灯：" + lightIndex.get(Integer.valueOf(addLight.getId())));
                                              //往服务器发送更新数据的请求
                                              updateLight(imei, addLight.getId(), addLight.getName());
                                          } else {
                                              lightIndex.put(Integer.valueOf(addLight.getId()), lightList.size());
                                              //保存灯泡数据到后台
                                              addLight(imei, addLight.getId(), addLight.getName());
                                          }

                                      }
                                  }
                              }
                          }
            );
        }
    };

    /**
     * 通过获取灯泡列表 来获取设备状态 有返回则在线  超时离线
     */
    private void getWifiDeviceStatus() {
        for (WifiDevice device : mListDevice) {
            if (MyApplication.getInstance().longConnected &&
                    MyApplication.getInstance().mService != null) {
                //获取wifi下的灯泡连接状态
                try {
                    MyApplication.getInstance().mService.getLightList(device.getAddress());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void connectLongSocket() {
        L.i(TAG, "connectLongSocket 进行长连接");
        try {
            if (MyApplication.getInstance().mService != null) {
                MyApplication.getInstance().mService.connect(null);
                MyApplication.getInstance().isFirstLongCon = true;
            } else {
                showShortToast(getString(R.string.link_service_fail));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * 请求wifi设备列表
     */
    public void requestWifiList() {
        //显示等待对话框
        showLoadingDialog(getResources().getString(R.string.waiting));
        RequestParams params = new RequestParams();
        params.put("name", "qin");
        RequestManager.post(Config.URL_GETWIFIDEVICELIST, this, params, requestListener(DEVICE_LIST_REQ));
    }


    /**
     * 根据消息类型 创建请求回调监听器
     *
     * @param type
     * @return
     */
    private RequestListener requestListener(int type) {
        if (type == DEVICE_LIST_REQ)
            return new RequestListener() {
                @Override
                public void requestSuccess(String result) {
                    L.i(TAG, "json：" + result);
                    closeLoadingDialog();
                    try {
                        JSONObject json = new JSONObject(result);
                        if (!"ok".equals(JsonUtil.getStr(json, Config.STATUS))) {
                            BaseTools.showToastByLanguage(HomeActivity.this, json);
                        } else {
                            mListDevice.clear();
                            JSONArray wifilist = json.getJSONArray("wifis");
                            for (int i = 0; i < wifilist.length(); i++) {
                                JSONObject ob = wifilist.getJSONObject(i);
                                String address = ob.getString("imei");
                                String name;
                                int status = WifiDevice.INACTIVE_STATUS;

                                if (ob.has("name")) {
                                    name = ob.getString("name");
                                } else {
                                    name = "unkown";
                                }

                                if (ob.has("status")) {
                                    status = ob.getInt("status");
                                }

                                WifiDevice d = new WifiDevice(null, name, address);
                                d.setStatus(status);
                                mListDevice.add(d);
                            }

                            //homeFragment.update(mListDevice);
                            //只有收到服务器发来的设备列表才进行  绑定服务
                            //绑定wifiConnectService
                            if (MyApplication.getInstance().mService == null) {
                                showLoadingDialog();
                                Intent i = new Intent(getApplicationContext(), WifiConnectService.class);
                                getApplicationContext().bindService(i, mConnection, BIND_AUTO_CREATE);
                            } else {
                                if (MyApplication.getInstance().longConnected) {
                                    //homeFragment.requestLightList();
                                    getWifiDeviceStatus();
                                } else {
                                    connectLongSocket();
                                }
                            }

                        }
                    } catch (JSONException e) {

                    }

                }

                @Override
                public void requestError(VolleyError e) {
                    if (e.networkResponse == null) {
                        closeLoadingDialog();
                        DialogUtil.showComReminderDialog(HomeActivity.this);
                        Toast.makeText(HomeActivity.this, getString(R.string.request_devicelist_fail), Toast.LENGTH_SHORT).show();
                    }

                }
            }

                    ;
        else
            return null;
    }

    /**
     * 初始化底部导航栏
     */
    private void initBottom() {

        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.nav_bottom);


        bottomNavigationBar
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);

        bottomNavigationBar
                .setMode(BottomNavigationBar.MODE_DEFAULT);

        bottomNavigationBar
                .setActiveColor(R.color.theme)
                .setInActiveColor("#a0a0a0")
                .setBarBackgroundColor("#fafafa");

        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.tabhost_home_icon, "主页"))
                .addItem(new BottomNavigationItem(R.drawable.tabhost_scen_icon_up, "情景"))
                .addItem(new BottomNavigationItem(R.drawable.tabhost_self_icon, "设置"))
                .initialise();


        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                changeFragment(position);
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
            }
        });
    }

    /**
     * 根据选择的按钮 切换显示的碎片
     *
     * @param position
     */
    private void changeFragment(int position) {

        transaction = fm.beginTransaction();
        transaction.hide(homeFragment);
        transaction.hide(themeFragment);
        transaction.hide(selfFrament);
        switch (position) {
            case 0:
                mtvTitle.setText("主页");
                transaction.show(homeFragment);
                transaction.commit();
                break;
            case 1:
                mtvTitle.setText("情景");
                transaction.show(themeFragment);
                transaction.commit();
                break;
            case 2:
                mtvTitle.setText("设置");
                transaction.show(selfFrament);
                transaction.commit();
                break;
        }
    }


    @Override
    protected void onDestroy() {
        if (mConnection != null && MyApplication.getInstance().mService != null) {
            try {
                MyApplication.getInstance().mService.unregisterCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        getApplicationContext().unbindService(mConnection);
        super.onDestroy();
        System.exit(0);
    }


    /**
     * 得到新添加的灯泡数据返回
     */
    public void getLightInfo(String light_name, int group_index, String light_num) {
        if (light_name != null && light_name.length() != 0) {
            try {
                Lg.i(TAG, "ADD_LIGHT onActivityResult");
                //待测试
//                 if (childList.size() == 0) {
//                          MyApplication.getInstance().mService.pairLight(mDevice.getAddress(), 1);
//                        } else {
                Lg.i(TAG, "pair_index:" + Integer.valueOf(light_num));
                MyApplication.getInstance().mService.pairLight(currentDevice.getAddress(), Integer.valueOf(light_num));
//                        }
                showLoadingDialog(getResources().getString(R.string.cmd_sending));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Light light = new Light();
            light.setName(light_name);
            light.setId(light_num);
            light.setLightStatu((byte) 3);
            addLight = light;
            addGroupIndex = group_index;

        }

    }

    void updateLight(final String imei, final String index, final String name) {
        RequestParams params = new RequestParams();
        params.put("imei", imei);
        params.put("index", index);
        params.put("name", name);
        RequestManager.post(Config.URL_UPDATELIGHT, HomeActivity.this, params, new RequestListener() {
            @Override
            public void requestSuccess(String result) {
                L.i(TAG, result);
                try {
                    JSONObject json = new JSONObject(result);
                    if (JsonUtil.getInt(json, Config.CODE) != 1) {
                        showLongToast(getResources().getString(R.string.add_light_error));
                    } else {
                        showLongToast(getResources().getString(R.string.add_light_ok));
                        //更新列表
                        MyApplication.getInstance().mService.getLightList(imei);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void requestError(VolleyError e) {
                showComReminderDialog();
            }
        });

    }

    void addLight(final String imei, final String index, final String name) {
        RequestParams params = new RequestParams();
        params.put("imei", imei);
        params.put("index", index);
        params.put("name", name);
        RequestManager.post(Config.URL_ADDLIGHT, HomeActivity.this, params, new RequestListener() {
            @Override
            public void requestSuccess(String result) {
                L.i(TAG, result);
                try {
                    JSONObject json = new JSONObject(result);
                    if (JsonUtil.getInt(json, Config.CODE) != 1) {
                        showLongToast(json.getString("msg"));
                    } else {
                        showLongToast(getResources().getString(R.string.add_light_ok));
                        //更新列表
                        MyApplication.getInstance().mService.getLightList(imei);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void requestError(VolleyError e) {
                showComReminderDialog();
            }
        });

    }

}
