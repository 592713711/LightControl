package com.zsg.jx.lightcontrol.model;

import com.zsg.jx.lightcontrol.service.WifiConnectService;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 16-7-11.
 */
public class ServerMsg implements Serializable {
    //String msgId;
    String imei;
    String cmd;
    long datetime;
    String pack;

    public final static int MS_DEVICE_TIMEOUT = 12 * 1000;       // 12s 设备回应的超时时间


    public ServerMsg(String imei, String cmd) {
        this.imei = imei;
        this.cmd = cmd;
        datetime = System.currentTimeMillis();
    }

    public boolean isTimeout() {
        long current = new Date().getTime();

        if (current - datetime > MS_DEVICE_TIMEOUT) {
            return true;
        } else {
            return false;
        }
    }

    public ServerMsg() {
        datetime = System.currentTimeMillis();
    }

    public ServerMsg(String pack) {
        this.pack=pack;
        String cmds[]=WifiConnectService.getCommand(pack);
        this.cmd = cmds[0];
        this.imei = cmds[1];
        datetime = System.currentTimeMillis();
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    public String getPack() {
        return pack;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public String getImei() {
        return imei;
    }

    public String getCmd() {
        return cmd;
    }


    public void setImei(String imei) {
        this.imei = imei;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }



    @Override
    public String toString() {
        return "ServerMsg{" +
                ", imei='" + imei + '\'' +
                ", cmd='" + cmd + '\'' +
                ", datetime='" + datetime + '\'' +
                '}';
    }
}
