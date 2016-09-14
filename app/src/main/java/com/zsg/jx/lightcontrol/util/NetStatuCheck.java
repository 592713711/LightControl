package com.zsg.jx.lightcontrol.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;

public class NetStatuCheck {
    public static ConnectivityManager manager;

    public static String checkGPRSState(Context context) {
        boolean flag = false;
        manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager.getActiveNetworkInfo() != null) {
            flag = manager.getActiveNetworkInfo().isAvailable();
        }
        if (flag) {
            State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    .getState();

            if (wifi == State.CONNECTED || wifi == State.CONNECTING) {
                return "wifi";
            }
            if (manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null) {
                State gprs = manager
                        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
                if (gprs == State.CONNECTED || gprs == State.CONNECTING) {
                    return "GPRS";
                }
            }
        } else {
            return "unavailable";
        }
        return "unavailable";
    }

}
