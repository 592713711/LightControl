package com.zsg.jx.lightcontrol.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.zsg.jx.lightcontrol.ICallback;
import com.zsg.jx.lightcontrol.IService;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.model.ServerMsg;
import com.zsg.jx.lightcontrol.util.L;
import com.zsg.jx.lightcontrol.util.Lg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WifiConnectService extends Service {
    private static final String TAG = "testWifiConnectService";
    private Handler myHandler;
    //private static final String HOST = "112.74.23.39";
    private static final String HOST = "120.25.100.110";
    private static final int PORT = 9899;
    //private static final int PORT = 7777;

    private static final String CLIENT = "QC";
    private static final String SEP = "@";
    private static final String END = "$";
    private static final String LOGIN_CMD = "010";      //长连接登录指令
    private static final String LOGIN_RSP_CMD = "011";  //登录返回
    private static final String HEART_CMD = "014";
    private static final String HEART_RSP_CMD = "015";
    private static final String NOTIFY_CMD = "016";
    private static final String NOTIFY_RSP = "017";

    private static final String SWITCH_CMD = "018";
    private static final String SWITCH_RSP = "019";

    public static final String GET_STATUS_CMD = "020";
    public static final String GET_STATUS_RSP = "021";

    public static final String PING_CMD = "022";
    public static final String PING_RSP = "023";

    public static final String GET_LIGHT_LIST_CMD = "024";
    public static final String GET_LIGHT_LIST_RSP = "025";

    public static final String SET_BRIGHT_CHROME_CMD = "026";
    public static final String SET_BRIGHT_CHROME_RSP = "027";

    public static final String GET_BRIGHT_CHROME_CMD = "028";
    public static final String GET_BRIGHT_CHROME_RSP = "029";

    public static final String PAIR_LIGHT_CMD = "030";
    public static final String PAIR_LIGHT_RSP = "031";

    private static final String ON = "1";
    private static final String OFF = "0";

    private static final String IMEI_PATTERN = "([0-9a-fA-F]+)";

    //每个WIFI模块具有一个IMEI唯一标识   这个是手机wifi模块的默认IMEI  在长连接登录时需要
    final static String IMEI = "123456789012345";


    private String mToken = null;

    Map<String, Socket> mSocketMap = new HashMap<String, Socket>();

    Handler mHandler = new Handler();

    // 命令发送队列  发送命令收到该命令响应后即为发送成功
    // 发送成功或发送超时（一定时间服务端未响应）  则移除收个命令 发送下条命令
    //List<String> mCmdList = new LinkedList<String>();
    // 命令发送队列
    List<ServerMsg> mCmdList = new ArrayList<>();
    final Object mLock = new Object();
    final int mInterval = 10;       // 15s 内响应命令

    public final static long MS_POLL_INTERVAL = 3000; // 3S

    private RemoteCallbackList<ICallback> mCallbacks = new RemoteCallbackList<ICallback>();
    private HandlerThread mHandlerThread;

    public class LocalBinder extends Binder {
        public WifiConnectService getService() {
            return WifiConnectService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        mHandlerThread = new HandlerThread("LongConnThread");
        mHandlerThread.start();
        myHandler = new Handler();

        return mBinder;
    }

    /* cmd, imei
     * 将命令字符串解析 得到cmd  和 imei
      * */
    public static String[] getCommand(String s) {
        Pattern p = Pattern.compile(CLIENT + SEP + "(\\d+)" + SEP + "([A-Za-z0-9]+)" + SEP + IMEI_PATTERN + SEP + "([0-9A-Fa-f]+)\\$");
        Matcher m = p.matcher(s);

        if (m.find()) {
            return new String[]{m.group(1), m.group(3)};
        }

        return null;
    }

    void sendCmdTimeout(String[] cmds) {
        if (cmds == null) {
            return;
        }

        String cmd = cmds[0];
        String imei = cmds[1];
        L.e(TAG, "cmd '" + cmd + "', imei = " + imei + " timeout");
        synchronized (mCallbacks) {
            //开始回调  得到注册的数量
            int n = mCallbacks.beginBroadcast();
            try {
                int i;
                for (i = 0; i < n; i++) {
                    mCallbacks.getBroadcastItem(i).onCmdTimeout(cmd, imei);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "remote call exception", e);
            }
            //关闭回调
            mCallbacks.finishBroadcast();
        }
    }

    byte charToBin(char c) {
        byte v;

        if (c >= '0' && c <= '9') {
            v = (byte) (c - (int) '0');
        } else {
            v = (byte) (10 + (int) c - (int) 'a');
        }

        return v;
    }

    int charToInt(char c) {
        int v;

        if (c >= '0' && c <= '9') {
            v = (int) (c - (int) '0');
        } else {
            v = (int) (10 + (int) c - (int) 'a');
        }

        return v;
    }

    /**
     * 16进制的两字符转换为整型
     *
     * @param c1
     * @param c2
     * @return
     */
    byte hex2bin(char c1, char c2) {    //待确认结果
        byte v;
        byte b1;
        byte b2;

        b1 = charToBin(c1);
        b2 = charToBin(c2);

        v = b1;
        v *= 16;
        v += b2;

        return v;
    }


    int hex2int(char c1, char c2) {
        int v;
        int b1;
        int b2;

        b1 = charToBin(c1);
        b2 = charToBin(c2);

        v = b1;
        v *= 16;
        v += b2;

        return v;
    }


    /*
    * 第一个字节为灯的总数
     */
    int[] getLightArray(String s) {
        byte[] v = new byte[256];
        int i;
        int j;

        s = s.toLowerCase();

        int len = s.length();
        if (len % 2 != 0) {
            L.e(TAG, "the length is odd, something error + '" + len + "'");
            len--;
        }

        for (i = 0, j = 0; i < len; i += 2) {
            v[j++] = hex2bin(s.charAt(i), s.charAt(i + 1));
        }

        int n = v[0];       // 灯的总数
        L.i(TAG, "n数组：" + n);
        int[] ret = new int[n];

        for (i = 1, j = 0; i < v.length && j < n; i++) {
            byte element = v[i];
            for (int k = 7; k >= 0 && j < n; k--) {
                if ((element & (1 << k)) != 0) {
                    ret[j++] = 1;
                } else {
                    ret[j++] = 0;
                }
            }
        }
        return ret;
    }


    /*
   * 第一个字节为灯的总数
    */
    int[] getLightIntArray(String s) {
        int[] v = new int[256];
        int i;
        int j;

        s = s.toLowerCase();

        int len = s.length();
        if (len % 2 != 0) {
            L.e(TAG, "the length is odd, something error + '" + len + "'");
            len--;
        }

        for (i = 0, j = 0; i < len; i += 2) {
            v[j++] = hex2int(s.charAt(i), s.charAt(i + 1));
        }

        int n = v[0];       // 灯的总数
        L.e(TAG, "n数组：" + n);
        int[] ret = new int[n];

        for (i = 1, j = 0; i < v.length && j < n; i++) {
            int element = v[i];
            for (int k = 7; k >= 0 && j < n; k--) {
                if ((element & (1 << k)) != 0) {
                    ret[j++] = 1;
                } else {
                    ret[j++] = 0;
                }
            }
        }
        return ret;
    }


    /**
     * 获得灯泡状态列表
     *
     * @param s
     * @return
     */
    byte[] getLightByteArrayPro(String s) {
        if (s != null && s.trim().length() != 0) {
            s = s.toLowerCase();
            int len = s.length();
            int lightLength = 0;
            if (len <= 2) {
                return null;
            } else {
                lightLength = Integer.parseInt(s.substring(0, 2), 16);  //16进制转换为10进制
            }
            byte[] ret = new byte[lightLength];
            L.e(TAG, "灯泡个数：" + lightLength);
            //16进制的灯状态
            String lightStatus = s.substring(2, s.length());
//            Lg.i(TAG, "灯泡status：" + lightStatus);
//            Lg.i(TAG, "灯泡status的长度：" + lightStatus.length());
            for (int i = 0, j = 0; i < lightStatus.length() && j < lightLength; i++, j = j + 2) {
                ret[j] = (byte) ((charToBin(lightStatus.charAt(i)) >> 2) & 0x03);
//                Lg.i(TAG, "j-->" + j + "   " + ret[j]);
                if (j + 1 == lightLength) {
                    break;
                }
                ret[j + 1] = (byte) (charToBin(lightStatus.charAt(i)) & 0x03);
//                Lg.i(TAG, "j+1-->" + (j + 1) + "  " + ret[j + 1]);
            }
            return ret;
        } else {
            return null;
        }
    }


    /**
     * 将字符串转换成二进制字符串
     */
    private String StrToBinstr(String str) {
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            //补全字符串
            result += Integer.toBinaryString(Integer.parseInt(str.substring(i, i + 1), 16)) + "";
        }
        return result;
    }


    /**
     * 解析16进制的字符串,长度为6
     *
     * @param s 返回的16进制的字符串
     * @return
     */
    int[] getRetValue(String s) {
        int[] ret = new int[256];
        int i;
        int j;

        s = s.toLowerCase();
        int len = s.length();
        if (len != 6) {
            L.e(TAG, "the length is odd, something error + '" + len + "'");
            return null;
        }
        for (i = 0, j = 0; i < len; i += 2) {
            ret[j++] = hex2bin(s.charAt(i), s.charAt(i + 1)) & 0xff;
        }

        return ret;
    }

    /**
     * 解析16进制的字符串,长度为4
     *
     * @param s 返回的16进制的字符串
     * @return
     */
    int getRetValueByLength(String s, int length) {
        s = s.toLowerCase();
        int len = s.length();
        if (len != length) {
            L.e(TAG, "the length is odd, something error + '" + len + "'");
            return 255;
        }
        int ret = hex2bin(s.charAt(length - 2), s.charAt(length - 1)) & 0xff;
        return ret;
    }

    /*
    * cmdPack 指令队列中的指令和参数
    * cmd, imei 收到的返回指令和参数
    *
    * 判断是否只相对应的命令 如果是 则移除命令队列、解同步锁、移除超时、发送下一条指令
     */
    void doWithCmdList(String cmd, String imei) {

        //mHandler.removeCallbacks(mTimeoutProc);
        synchronized (mLock) {
            for (int i = 0; i < mCmdList.size(); i++) {
                ServerMsg msg = mCmdList.get(i);
                if (msg.getCmd().equals(cmd) && msg.getImei().equals(imei)) {
                    mCmdList.remove(i);
                    i--;
                }
            }

        }

    }

    /**
     * 解析返回结果
     * <p>
     * CLIENT + SEP(@) + CMD + SEP + TOKEN + SEP + IMEI +SEP + value + '$'
     *
     * @param s
     */
    void parseResponse(String s) {

        Pattern p = Pattern.compile(CLIENT + SEP + "(\\d+)" + SEP + "([A-Za-z0-9]+)" + SEP + IMEI_PATTERN + SEP + "([0-9A-Fa-f]+)\\$");
        Matcher m = p.matcher(s);
        //String[] cmdPack = null;


        while (m.find()) {
            String cmd = m.group(1);
            String token = m.group(2);
            String imei = m.group(3);
            String value = m.group(4);

            //比较token是否一致
            if (token.equals(mToken)) {
                L.e(TAG, "token match!");
            }

            //根据响应类型  做出不同处理
            if (LOGIN_RSP_CMD.equals(cmd)) {
                //登录响应
                L.e(TAG, "get login rsp cmd");
                //判断发送的指令和该响应是否一致 并做出下一步处理
                doWithCmdList( LOGIN_CMD, imei);

                //回调 所有注册了的回调
                synchronized (mCallbacks) {
                    int n = mCallbacks.beginBroadcast();
                    try {
                        int i;
                        for (i = 0; i < n; i++) {
                            //当登录成功后才发送  已连接回调
                            mCallbacks.getBroadcastItem(i).onConnect(imei);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "remote call exception", e);
                    }
                    mCallbacks.finishBroadcast();
                }
            }

            if (HEART_CMD.equals(cmd)) {
                sendHeartRspCmd();
            }

            if (NOTIFY_CMD.equals(cmd)) {
                L.e(TAG, "Get notify cmd here: token =" + token + ", imei = " + imei);

                synchronized (mCallbacks) {
                    int n = mCallbacks.beginBroadcast();
                    try {
                        int i;
                        for (i = 0; i < n; i++) {
                            mCallbacks.getBroadcastItem(i).onNotify(imei, Integer.parseInt(value));
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "remote call exception", e);
                    }
                    mCallbacks.finishBroadcast();
                }
            }

            if (SWITCH_RSP.equals(cmd)) {
                L.e(TAG, "Get switch rsp here: token =" + token + ", imei = " + imei + ", ret = " + value);
                doWithCmdList( SWITCH_CMD, imei);
                synchronized (mCallbacks) {
                    int n = mCallbacks.beginBroadcast();
                    try {
                        int i;
                        boolean ret;

                        if ("1".equals(value)) {
                            ret = true;
                        } else {
                            ret = false;
                        }

                        for (i = 0; i < n; i++) {
                            mCallbacks.getBroadcastItem(i).onSwitchRsp(imei, ret);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "remote call exception", e);
                    }
                    mCallbacks.finishBroadcast();
                }
            }

            if (PING_RSP.equals(cmd)) {
                //ping命令回调

                L.e(TAG, "Get ping rsp here: token =" + token + ", imei = " + imei + ", ret = " + value);
                doWithCmdList( PING_CMD, imei);

                synchronized (mCallbacks) {
                    int n = mCallbacks.beginBroadcast();
                    try {
                        int i;
                        int ret = Integer.parseInt(value);

                        for (i = 0; i < n; i++) {
                            mCallbacks.getBroadcastItem(i).onPingRsp(imei, ret);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "remote call exception", e);
                    }
                    mCallbacks.finishBroadcast();
                }
            }

            if (GET_STATUS_RSP.equals(cmd)) {
                L.e(TAG, "Get status rsp here: token =" + token + ", imei = " + imei + ", ret = " + value);
                doWithCmdList( GET_STATUS_CMD, imei);
                synchronized (mCallbacks) {
                    int n = mCallbacks.beginBroadcast();
                    try {
                        int i;
                        int ret = Integer.parseInt(value);

                        for (i = 0; i < n; i++) {
                            mCallbacks.getBroadcastItem(i).onGetStatusRsp(imei, ret);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "remote call exception", e);
                    }
                    mCallbacks.finishBroadcast();
                }
            }

            if (GET_LIGHT_LIST_RSP.equals(cmd)) {
                L.e(TAG, "Get light list rsp here: token =" + token + ", imei = " + imei + ", ret = " + value);
                doWithCmdList( GET_LIGHT_LIST_CMD, imei);
//                int[] lightArray = getLightIntArray(value);
                byte[] lightArray = getLightByteArrayPro(value);
                synchronized (mCallbacks) {
                    int n = mCallbacks.beginBroadcast();
                    Lg.i("hjq", "n->>>" + n);
                    try {
                        int i;

                        for (i = 0; i < n; i++) {
                            mCallbacks.getBroadcastItem(i).onGetLightList(imei, lightArray);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "remote call exception", e);
                    }
                    mCallbacks.finishBroadcast();
                }
            }

            if (GET_BRIGHT_CHROME_RSP.equals(cmd)) {
                //得到
                L.e(TAG, "Get bright and chrome rsp here: token =" + token + ", imei = " + imei + ", ret = " + value);
                doWithCmdList( GET_BRIGHT_CHROME_CMD, imei);
                int[] retArray = getRetValue(value);
                synchronized (mCallbacks) {
                    int n = mCallbacks.beginBroadcast();
                    try {
                        int i;
                        Lg.i("hjq", "n->>>" + n);
                        for (i = 0; i < n; i++) {
                            mCallbacks.getBroadcastItem(i).onGetBrightChromeRsp(imei, retArray[0], retArray[1], retArray[2]);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "remote call exception", e);
                    }
                    mCallbacks.finishBroadcast();
                }
            }

            if (SET_BRIGHT_CHROME_RSP.equals(cmd)) {
                L.e(TAG, "Set bright and chrome rsp here: token =" + token + ", imei = " + imei + ", ret = " + value);
                doWithCmdList(SET_BRIGHT_CHROME_CMD, imei);
                synchronized (mCallbacks) {
                    int n = mCallbacks.beginBroadcast();
                    L.e(TAG, "n->>>" + n);
                    try {
                        int i;

                        for (i = 0; i < n; i++) {
                            mCallbacks.getBroadcastItem(i).onSetBrightChromeRsp(imei, getRetValueByLength(value, 4));
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "remote call exception", e);
                    }
                    mCallbacks.finishBroadcast();
                }
            }

            if (PAIR_LIGHT_RSP.equals(cmd)) {
                L.i(TAG, "pair light device rsp here: token =" + token + ", imei = " + imei + ", ret = " + value);
                doWithCmdList( PAIR_LIGHT_CMD, imei);
                synchronized (mCallbacks) {
                    int n = mCallbacks.beginBroadcast();
                    try {
                        int i;

                        for (i = 0; i < n; i++) {
                            mCallbacks.getBroadcastItem(i).onPairLightRsp(imei, getRetValueByLength(value, 2));
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "remote call exception", e);
                    }
                    mCallbacks.finishBroadcast();
                }
            }
        }
    }

    /**
     * 连接service服务
     *
     * @param s
     */
    void connectServer(final String s) {
        L.i(TAG, "connectServer");
        InputStream in = null;

        Socket socket = mSocketMap.get(IMEI);

        try {
            if (socket == null) {
                //建立长连接
                socket = new Socket(HOST, PORT);
                mSocketMap.put(IMEI, socket);
            }

            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    //发送登录命令
                    sendCmd(LOGIN_CMD, IMEI, ON);
                }
            });

            //获取服务端返回结果
            in = socket.getInputStream();
            byte[] b = new byte[1024];
            int len;

            while ((len = in.read(b)) != -1) {
                String line = new String(b, 0, len);
                L.e(TAG, "line = " + line);
                parseResponse(line);
            }
        } catch (Exception e) {
            L.e(TAG, "连接断开");
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    L.e(TAG, "关闭通道");
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mSocketMap.remove(IMEI);
            }

            synchronized (mCallbacks) {
                int n = mCallbacks.beginBroadcast();
                try {
                    int i;
                    for (i = 0; i < n; i++) {
                        mCallbacks.getBroadcastItem(i).onDisconnect(IMEI);
                    }
                } catch (RemoteException re) {
                    Log.e(TAG, "remote call exception", re);
                }
                mCallbacks.finishBroadcast();
            }
        }
    }

    void sendHeartRspCmd() {
        Iterator<String> iterator = mSocketMap.keySet().iterator();
        final String imei = iterator.next();
        if (imei == null) {
            L.e(TAG, "no valid imei");
            return;
        }

        myHandler.post(new Runnable() {
            @Override
            public void run() {
                sendCmdDirect(HEART_RSP_CMD, imei, "120");
            }
        });
    }

 /*   void sendNextPack() {
        if (mCmdList.size() > 0) {
            final String pack = mCmdList.get(0);
            if (pack != null) {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        doSend(pack);
                    }
                });
            }
        }
    }*/

  /*  Runnable mTimeoutProc = new Runnable() {
        @Override
        public void run() {
            String s;
            synchronized (mLock) {
                if (mCmdList.size() == 0) {
                    return;
                }

                s = mCmdList.remove(0);
                sendNextPack();
            }

            Log.e(TAG, "cmd '" + s + "' res timeout");
            String[] cmds = getCommand(s);
            if (cmds != null) {
                sendCmdTimeout(cmds);
            }
        }
    };*/

    protected boolean doSend(String pack) {
        Socket socket = mSocketMap.get(IMEI);
        if (socket == null) {
            L.e(TAG, "no socket for server");
            return false;
        }

        L.e(TAG, "cmd string = " + pack);
        OutputStream os = null;
        ServerMsg msg = new ServerMsg(pack);
        mCmdList.add(msg);

        //mHandler.postDelayed(mTimeoutProc, mInterval * 1000);
        startPollTimer();
        try {
            os = socket.getOutputStream();
            byte[] strbyte = pack.getBytes("UTF-8");
            os.write(strbyte, 0, strbyte.length);
        } catch (IOException e2) {
            e2.printStackTrace();
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return false;
        }

        return true;
    }

    void startPollTimer() {
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkCmdTimeout(mCmdList);  //检查是否超时

                if (!mCmdList.isEmpty()) {
                    myHandler.postDelayed(this, MS_POLL_INTERVAL);
                }
            }
        }, MS_POLL_INTERVAL);
    }

    void checkCmdTimeout(List<ServerMsg> list) {
        ServerMsg msg;
        boolean timeout = false;

        synchronized (mLock) {
            if (list.isEmpty()) {
                return;
            }
            msg = list.get(0);
            if (msg.isTimeout()) {
                L.e(TAG, "msg: " + msg + "is timeout");
                timeout = true;
                list.remove(0);
            }
        }
        if (!timeout) {
            return;
        }

        String[] array = getCommand(msg.getPack());
        synchronized (mCallbacks) {
            int n = mCallbacks.beginBroadcast();
            try {
                int i;
                for (i = 0; i < n; i++) {
                    mCallbacks.getBroadcastItem(i).onCmdTimeout(array[0], array[1]);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "remote call exception", e);
            }
            mCallbacks.finishBroadcast();
        }
    }

    boolean sendCmdDirect(String cmd, String imei, String value) {
        String pack;

        mToken = MyApplication.getInstance().mToken;

        StringBuilder sb = new StringBuilder(CLIENT);
        sb.append(SEP);
        sb.append(cmd);
        sb.append(SEP);
        sb.append(mToken);
        sb.append(SEP);
        sb.append(imei);
        sb.append(SEP);
        if (value != null) {
            sb.append(value);
        } else {
            sb.append(OFF);
        }
        sb.append(END);
        pack = sb.toString();

        return doSend(pack);
    }

    /**
     * 向服务器发送命令
     *
     * @param cmd   命令标识
     * @param imei  指定家具的WIFI模块 IMEI    WIFI模块具有一个IMEI唯一标识
     * @param value
     * @return
     */
    boolean sendCmd(String cmd, String imei, String value) {
        String pack;

        mToken = MyApplication.getInstance().mToken;

        StringBuilder sb = new StringBuilder(CLIENT);
        sb.append(SEP);
        sb.append(cmd);
        sb.append(SEP);
        sb.append(mToken);
        sb.append(SEP);
        sb.append(imei);
        sb.append(SEP);
        if (value != null) {
            sb.append(value);
        } else {
            sb.append(OFF);
        }
        sb.append(END);
        pack=sb.toString();
        return doSend(pack);
    }

    @Override
    public void onDestroy() {
        L.i(TAG, "onDestroy");
        for (Socket s : mSocketMap.values()) {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        mCallbacks.kill();
        mHandlerThread.quit();
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        L.i(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    String bin2hex(byte v) {
        StringBuffer sb = new StringBuffer();
        char[] array = "0123456789ABCDEF".toCharArray();

        int x = (v >> 4) & 0xf;
        sb.append(array[x]);
        x = v & 0xf;
        sb.append(array[x]);

        return sb.toString();
    }

    private IService.Stub mBinder = new IService.Stub() {
        @Override
        public boolean initialize() throws RemoteException {
            L.i(TAG, "initialize");
            return false;
        }

        //长连接服务器
        @Override
        public boolean connect(final String addr) throws RemoteException {
            L.i(TAG, "connect");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //长连接服务器
                    connectServer(addr);
                }
            }).start();

            return true;
        }

        //断开长连接
        @Override
        public void disconnect(final String addr) throws RemoteException {
            L.i(TAG, "disconnect");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Socket s = mSocketMap.get(IMEI);
                    mSocketMap.remove(IMEI);
                    if (s != null) {
                        try {
                            s.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        /**
         * 打开关闭LED灯
         * @param addr
         * @param on
         * @throws RemoteException
         */
        @Override
        public void enableLight(final String addr, final boolean on) throws RemoteException {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendCmd(SWITCH_CMD, addr, on ? ON : OFF);
                }
            });
        }

        @Override
        public void getLightStatus(final String addr) throws RemoteException {
            L.e(TAG, "getLightStatus");
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendCmd(GET_STATUS_CMD, addr, "0");
                }
            });
        }

        @Override
        public void ping(final String addr, final int val) throws RemoteException {
            L.i(TAG, "ping");
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendCmd(PING_CMD, addr, Integer.toString(val));
                }
            });
        }

        @Override
        public void getLightList(final String address) throws RemoteException {
            L.e(TAG, "getLightList");
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendCmd(GET_LIGHT_LIST_CMD, address, "0");
                }
            });
        }

        @Override
        public void setBrightChrome(final String address, int index, int bright, int chrome) throws RemoteException {
            if (index < 0 || index > 255) {
                L.e(TAG, "index parameter error " + index);
                return;
            }
            if (bright < 0 || bright > 255 || chrome < 0 || chrome > 255) {
                L.e(TAG, "bright or chrome parameter error " + bright + ":" + chrome);
                return;
            }

            StringBuffer sb = new StringBuffer();
            sb.append(bin2hex((byte) index));
            sb.append(bin2hex((byte) bright));
            sb.append(bin2hex((byte) chrome));
            final String s = sb.toString();

            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendCmd(SET_BRIGHT_CHROME_CMD, address, s);
                }
            });
        }

        /**
         * //获取灯泡的亮度和色温
         * @param address      设备地址
         * @param index        设备中灯泡序号
         * @throws RemoteException
         */
        @Override
        public void getBrightChrome(final String address, int index) throws RemoteException {
            if (index < 0 || index > 255) {
                L.e(TAG, "index parameter error " + index);
                return;
            }
            L.i(TAG,"getBrightChrome");
            StringBuffer sb = new StringBuffer();
            sb.append(bin2hex((byte) index));
            final String s = sb.toString();

            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendCmd(GET_BRIGHT_CHROME_CMD, address, s);
                }
            });
        }

        @Override
        public void pairLight(final String address, int index) throws RemoteException {
            if (index < 0 || index > 255) {
                L.e(TAG, "index parameter error " + index);
                return;
            }
            StringBuffer sb = new StringBuffer();
            sb.append(bin2hex((byte) index));
            final String s = sb.toString();

            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendCmd(PAIR_LIGHT_CMD, address, s);
                }
            });
        }

        //清空消息队列
        @Override
        public void clearCmdList() {
            //mHandler.removeCallbacks(mTimeoutProc);
            mCmdList.clear();
        }


        public void unregisterCallback(ICallback cb) {
            if (cb != null) {
                mCallbacks.unregister(cb);
            }
        }

        public void registerCallback(ICallback cb) {
            if (cb != null) {
                mCallbacks.register(cb);
            }
        }
    };


}
