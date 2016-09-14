package com.zsg.jx.lightcontrol.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.listener.EditTextTouchListener;
import com.zsg.jx.lightcontrol.listener.PwdTextWatcher;
import com.zsg.jx.lightcontrol.util.BaseTools;
import com.zsg.jx.lightcontrol.util.Config;
import com.zsg.jx.lightcontrol.util.JsonUtil;
import com.zsg.jx.lightcontrol.util.L;
import com.zsg.jx.lightcontrol.util.Lg;
import com.zsg.jx.lightcontrol.volley.RequestListener;
import com.zsg.jx.lightcontrol.volley.RequestManager;
import com.zsg.jx.lightcontrol.volley.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicReference;

public class WifiConnectionActivity extends BaseActivity {
    private static final String TAG = "testWifiConnectionActivity";
    private static final int OUT_TIME = 80;
    private EditText et_wifiNum, et_password;
    private Button btn_login;
    //密码的状态是否是可见状态,默认为不可见
    private boolean isShow = false;


    Thread sendUdpThread;
    Thread tcpThread;
    boolean exitProcess = false;
    InetAddress address;
    Random rand = new Random();
    StringBuffer ipData;
    int cmdNumber = 3;
    StringBuffer[] packetData = new StringBuffer[cmdNumber];
    StringBuffer[] seqData = new StringBuffer[cmdNumber];
    int testDataRetryNum = 150;
    String retryNumber[] = {"10", "10", "5"};
    String magicNumber = "iot";
    String rc4Key = "Key";
    String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    int[] stable = new int[256];
    int[] tempPacket = new int[256];
    int[] tempSeq = new int[256];
    int[] sonkey = new int[256];
    ServerSocket serv;
    String PASS;        //wifi密码

    private boolean isSendFinished = false;     //wifi配置消息是否已经发送完成
    private Timer timer;
    private static final int CONFIGUREOK = 10;
    private static final int CONFIGUREFAIL = 11;
    private static final int LINKWIFI = 12;
    private int ret = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_connection);
        initView();
        initData();

        //设置监听器
        setListener();
    }

    private void initData() {
        //得到当前连接的wifi信息
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo != null) {
            et_wifiNum.setText(whetherToRemoveTheDoubleQuotationMarks(wifiInfo.getSSID()));
        }

        PASS = MyApplication.getInstance().getPreferenceUtil().getWifiPassward();
        if (!(PASS.equals(""))) {
            et_password.setText(PASS);
        }

        savePhoneIp(wifiInfo.getIpAddress());
        serv = null;
        sendUdpThread = null;
        tcpThread = null;
    }

    private void initView() {
        et_wifiNum = (EditText) findViewById(R.id.et_wifiName);
        et_password = (EditText) findViewById(R.id.et_Password);
        btn_login = (Button) findViewById(R.id.btn_confirm);
    }

    private void setListener() {
        //设置文本改变监听器
        et_password.addTextChangedListener(new PwdTextWatcher(WifiConnectionActivity.this, et_password));
        //触摸监听器
        et_wifiNum.setOnTouchListener(new EditTextTouchListener(WifiConnectionActivity.this, et_wifiNum));
        //触摸监听器
        et_password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //获得密码输入框右边的图片
                Drawable drawable = et_password.getCompoundDrawables()[2];
                Drawable hidePwd = ContextCompat.getDrawable(WifiConnectionActivity.this, R.drawable.ic_visibility_off_24dp);
                Drawable displayPwd = ContextCompat.getDrawable(WifiConnectionActivity.this, R.drawable.ic_visibility_24dp);
                hidePwd.setBounds(0, 0, hidePwd.getIntrinsicWidth(), hidePwd.getIntrinsicHeight());
                displayPwd.setBounds(0, 0, displayPwd.getIntrinsicWidth(), displayPwd.getIntrinsicHeight());
                if (drawable == null) {
                    return false;
                }
                if (event.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }
                if (event.getX() > et_password.getWidth() - drawable.getIntrinsicWidth()) {

                    if (!isShow) {
                        //作密码可见处理
                        et_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        //改变图片
                        et_password.setCompoundDrawables(null, null, hidePwd, null);
                        et_password.setSelection(et_password.getText().toString().length());
                    } else {
                        et_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        et_password.setCompoundDrawables(null, null, displayPwd, null);
                        et_password.setSelection(et_password.getText().toString().length());
                    }
                    isShow = !isShow;

                }
                return false;
            }
        });

        btn_login.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == R.id.btn_confirm) {


            if (et_wifiNum.getText().toString() == null || et_wifiNum.getText().toString().trim().length() == 0) {
                showShortToast(getString(R.string.wifi_id_not_empty));
                return;
            }


            PASS=et_password.getText().toString().trim();
            MyApplication.getInstance().getPreferenceUtil().setWifiPassward(PASS);
            if (!isSendFinished) {
                isSendFinished = true;
                showLoadingDialog(getString(R.string.wait_wifi_confim), false);
                //80S后关闭对话框  配置失败
                handler.sendEmptyMessageDelayed(CONFIGUREFAIL, OUT_TIME * 1000);
                //有用
                enableThread();
            } else {
                sendFinish();
            }


        }
    }

    public String whetherToRemoveTheDoubleQuotationMarks(String ssid) {
        int deviceVersion;
        deviceVersion = Build.VERSION.SDK_INT;
        if (deviceVersion >= 17) {
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
        }
        return ssid;
    }

    //得到当前ip地址
    void savePhoneIp(int ipAddress) {
        ipData = new StringBuffer();
        ipData.append((char) (ipAddress & 0xff));
        ipData.append((char) (ipAddress >> 8 & 0xff));
        ipData.append((char) (ipAddress >> 16 & 0xff));
        ipData.append((char) (ipAddress >> 24 & 0xff));
    }

    void enableThread() {
        Lg.i(TAG, "enableThread");
        exitProcess = false;
        if (sendUdpThread == null) {
            sendUdpThread = new sendUdpThread();
            sendUdpThread.start();
        }
        if (tcpThread == null) {
            tcpThread = new tcpThread();
            tcpThread.start();
        }
    }

    /**
     * 将wifi信息 和 ip地址通过udp发送到周围
     */
    public class sendUdpThread extends Thread {

        public void run() {
            Lg.i(TAG, "sendUdpThread->>>run()");
            KSA();
            PRGA();
            while (!exitProcess) {
                Lg.i(TAG, "sendUdpThread->>>run()--->!exitProcess");
                SendbroadCast();
            }
        }
    }

    /**
     * 自身开启服务器  等待接受到udp信息的模块进行长连接，从而得到模块mac地址
     */
    public class tcpThread extends Thread {
        int port = 8209;
        Socket s1 = null;

        public void run() {
            L.i(TAG, "tcpThread");
            while (!exitProcess) {
                try {
                    serv = new ServerSocket(port, 10);
                } catch (Exception se) {
                    L.i(TAG, "Init ServerSocker Error!!");
                }

                try {
                    s1 = serv.accept();
                    L.i(TAG, "new tcpReceThread(s1)");
                    tcpReceThread mt = new tcpReceThread(s1);
                } catch (IOException e) {
                    L.i(TAG, "tcpReceThread  Error");
                    e.printStackTrace();
                }
            }
            try {
                if (serv != null) {
                    L.i(TAG, "tcpReceThread  Close");
                    serv.close();
                    serv = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public class tcpReceThread extends Thread {
        private Socket socket = null;
        private InputStream in;
        private OutputStream out;
        private DataOutputStream streamWriter;
        private InputStreamReader streamReader;

        public tcpReceThread(Socket socket) {
            super("tcpReceThread");
            this.socket = socket;
            start();
            L.i(TAG, "tcpReceThread.start()");
        }

        public void run() {
            L.i(TAG, "tcpReceThread->>>run()");
            try {
                char[] tmpbuffer = new char[1024];
                in = socket.getInputStream();
                out = socket.getOutputStream();
                streamWriter = new DataOutputStream(out);
                streamReader = new InputStreamReader(in, "UTF-8");
                int len = streamReader.read(tmpbuffer, 0, 1024);
                L.i(TAG, "tcpReceThread->>>run()-->>>len----->>" + len);
                if (len > 0) {
                    char[] buffer = Arrays.copyOf(tmpbuffer, len);
//                    String macMessage = socket.getInetAddress().getHostAddress() + "/" + new String(buffer);
                    String macMessage = new String(buffer);
                    Lg.i(TAG, "message->>>" + macMessage);
                    Message message = new Message();
                    message.what = CONFIGUREOK;
                    message.obj = macMessage;
                    handler.sendMessage(message);
                }
                String message = "ok";
                byte[] midbytes = message.getBytes("UTF8");
                streamWriter.write(midbytes, 0, midbytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                in.close();
                out.close();
                streamWriter.close();
                streamReader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送广播给周围
     * 标识符
     * wifi名称长度+wifi密码长度+ip地址
     * wifi名称+wifi密码
     */
    public void SendbroadCast() {
        L.i(TAG, "SendbroadCast()");
        char crcDdata;

        sendTestData();

        for (int z = 0; z < cmdNumber; z++) {
            packetData[z] = new StringBuffer();
            if (z == 0)
                packetData[0].append(magicNumber);
            else if (z == 1)
                packetData[1].append((char) et_wifiNum.length()).append((char) et_password.length()).append(ipData.charAt(0)).append(ipData.charAt(1)).append(ipData.charAt(2)).append(ipData.charAt(3));
            else
                packetData[2].append(et_wifiNum.getText()).append(et_password.getText());
            crcDdata = crc8_msb((char) 0x1D, packetData[z].length(), z);
            packetData[z].append(crcDdata);
            addSeqPacket(z);
            if (exitProcess)
                return;
        }

        for (int i = 0; i < cmdNumber; i++) {
            cmdCryption(i);
            for (int j = 0; j < Integer.valueOf(retryNumber[i]); j++) {
                for (int k = 0; k < packetData[i].length(); k++) {
                    AtomicReference<StringBuffer> sendPacketData = new AtomicReference<StringBuffer>(new StringBuffer());
                    AtomicReference<StringBuffer> sendPacketSeq = new AtomicReference<StringBuffer>(new StringBuffer());

                    for (int v = 0; v < tempPacket[k] + 1; v++) {
                        sendPacketData.get().append(AB.charAt(rand.nextInt(AB.length())));
                    }
                    for (int g = 0; g < (tempSeq[k] + 1 + 256); g++) {
                        sendPacketSeq.get().append(AB.charAt(rand.nextInt(AB.length())));
                    }

                    try {
                        DatagramSocket clientSocket = new DatagramSocket();
                        clientSocket.setBroadcast(true);
                        address = InetAddress.getByName("255.255.255.255");
                        DatagramPacket sendPacketSeqSocket = new DatagramPacket(sendPacketSeq.get().toString().getBytes(), sendPacketSeq.get().length(), address, 8300);
                        clientSocket.send(sendPacketSeqSocket);
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        DatagramPacket sendPacketDataSocket = new DatagramPacket(sendPacketData.get().toString().getBytes(), sendPacketData.get().length(), address, 8300);
                        clientSocket.send(sendPacketDataSocket);
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        clientSocket.close();
                        if (exitProcess)
                            return;
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (exitProcess)
                        return;
                }
            }
            if (exitProcess)
                return;
        }
    }

    void sendTestData() {
        int[] testData = new int[]{1, 2, 3, 4};
        for (int j = 0; j < testDataRetryNum; j++) {
            for (int k = 0; !(k >= testData.length); k++) {
                AtomicReference<StringBuffer> sendTestData = new AtomicReference<StringBuffer>(new StringBuffer());

                for (int v = 0; v < testData[k]; v++) {
                    sendTestData.get().append(AB.charAt(rand.nextInt(AB.length())));
                }

                try {
                    DatagramSocket clientSocket = new DatagramSocket();
                    clientSocket.setBroadcast(true);
                    address = InetAddress.getByName("255.255.255.255");
                    DatagramPacket sendPacketSeqSocket = new DatagramPacket(sendTestData.get().toString().getBytes(), sendTestData.get().length(), address, 8300);
                    clientSocket.send(sendPacketSeqSocket);
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    clientSocket.close();
                    if (exitProcess)
                        return;
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (exitProcess)
                return;
        }
    }

    void KSA() {
        int i, j = 0, temp;
        for (i = 0; i < 256; i++)
            stable[i] = i;
        for (i = 0; i < 256; i++) {
            j = (j + stable[i] + rc4Key.charAt(i % rc4Key.length())) % 256;
            temp = stable[i];
            stable[i] = stable[j];
            stable[j] = temp;
        }
    }

    void PRGA() {
        int m = 0, i = 0, j = 0, t, l, temp;
        l = 256;
        while (l > 0) {
            i = (i + 1) % 256;
            j = (j + stable[i]) % 256;
            temp = stable[i];
            stable[i] = stable[j];
            stable[j] = temp;
            t = (stable[j] + stable[i]) % 256;
            sonkey[m++] = stable[t];
            l--;
        }
    }

    char crc8_msb(char poly, int size, int cmdNum) {
        char crc = 0x00, tmp;
        int bit;
        int i = 0;
        while (size > 0) {
            crc ^= packetData[cmdNum].charAt(i);
            for (bit = 0; bit < 8; bit++) {
                if ((0x0ff & (crc & 0x80)) != 0x00) {
                    tmp = (char) (0x0ff & (crc << 1));
                    crc = (char) (tmp ^ poly);
                } else {
                    crc <<= 1;
                }
            }
            size--;
            i++;
        }
        return crc;
    }

    void cmdCryption(int cmdUum) {
        int i;
        for (i = 0; i < packetData[cmdUum].length(); i++) {
            tempPacket[i] = packetData[cmdUum].charAt(i) ^ sonkey[i];
            tempSeq[i] = seqData[cmdUum].charAt(i) ^ sonkey[0];
        }
        tempPacket[i] = '\n';
        tempSeq[i] = '\n';
    }

    void addSeqPacket(int cmdNum) {
        int i;
        char value;

        seqData[cmdNum] = new StringBuffer(packetData[cmdNum]);

        for (i = 0; i < seqData[cmdNum].length(); i++) {
            if (cmdNum == 0)
                value = (char) ((0x0ff & (i)));
            else if (cmdNum == 1)
                value = (char) ((0x0ff & (i << 1) | 0x01));
            else
                value = (char) ((0x0ff & (i << 2) | 0x02));
            seqData[cmdNum].setCharAt(i, value);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == CONFIGUREFAIL) {

                L.i(TAG, "wifi配置失败>>>");
                closeLoadingDialog();
                sendFinish();
                showShortToast(getString(R.string.configure_route_fail));
                return;
            }

            String mac = (String) msg.obj;
            if (mac != null && mac.length() != 0) {
                if (mac.matches("Connection to .* refused") || mac.matches("Connect to.*timed out")) {
                    showComReminderDialog();
                    return;
                }
                switch (msg.what) {
                    case CONFIGUREOK:
                        sendFinish();
                        closeLoadingDialog();
                        handler.removeMessages(CONFIGUREFAIL);
                        //处理mac---前面不足两位，前面加0,后面添加0
                        String str[] = mac.split("\\:");
                        String realMac = "";
                        for (int i = 0; i < str.length; i++) {
                            if (str[i].length() == 1) {
                                str[i] = "0" + str[i];
                            }
                            realMac = realMac + str[i];
                        }
                        Lg.i(TAG, "realMac->>>" + realMac);
                        //确认扫描mac与连接mac是否相等
                        if (realMac.length() < 15) {
                            for (int j = realMac.length() + 1; j <= 15; j++) {
                                realMac = realMac + "0";
                            }
                        } else if (realMac.length() > 15) {
                            showShortToast(getString(R.string.get_wifi_mac_error));
                            return;
                        }
                        final String stableMac = realMac;
                        Lg.i(TAG, "stableMac>>>" + stableMac);

                        sendLinkWifiRequest(stableMac);

                        break;
                    default:
                        break;
                }
            } else {
                showShortToast(getString(R.string.check_net_config));
            }
        }

    };

    /**
     * 向web服务端发送链接 wifi模块请求
     *
     * @param stableMac
     */
    private void sendLinkWifiRequest(String stableMac) {
        RequestParams params = new RequestParams();
        params.put(Config.IMEI, stableMac);
        RequestManager.post(Config.URL_LINKWIFIDEVICE, WifiConnectionActivity.this, params, new RequestListener() {
            @Override
            public void requestSuccess(String result) {
                Lg.i(TAG, "HttpUtil.URL_LINKWIFIDEVICE->>>>>" + result);
                JSONObject json = null;
                try {
                    json = new JSONObject(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!"ok".equals(JsonUtil.getStr(json, Config.STATUS))) {
                    BaseTools.showToastByLanguage(WifiConnectionActivity.this, json);
                } else {
                    BaseTools.showToastByLanguage(WifiConnectionActivity.this, json);
                    ret = 1;
                }

                showShortToast(getString(R.string.configure_route_ok));

                Intent intent = new Intent();
                Bundle b = new Bundle();
                b.putInt("ret", ret);
                intent.putExtras(b);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void requestError(VolleyError e) {
                showShortToast(getString(R.string.check_net_config));
            }
        });

    }

    private void sendFinish() {
        isSendFinished = false;
        closeLoadingDialog();
        exitThread();
    }

    void exitThread() {
        exitProcess = true;
        if (sendUdpThread != null) {
            sendUdpThread.interrupt();
            sendUdpThread = null;
        }
        if (tcpThread != null) {
            if (serv != null) {
                try {
                    serv.close();
                    serv = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            tcpThread.interrupt();
            tcpThread = null;
        }
    }

    public void doBack(View v){
        finish();
    }
}
