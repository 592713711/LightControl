package com.zsg.jx.lightcontrol.model;

import java.io.Serializable;

/**
 * Created by Administrator on 16-3-7.
 */
public class WifiDevice implements Serializable {
   // private String name;
    private String thumbnail;       //缩略图
    public byte[] list;     //保存灯泡状态


    public WifiDevice copy() {

        WifiDevice d = new WifiDevice();
        d.setStatus(this.getStatus());
        d.setThumbnail(this.getThumbnail());
        d.setName(this.getName());

        return d;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WifiDevice device = (WifiDevice) o;
        if (getThumbnail() != null ? !getThumbnail().equals(device.getThumbnail()) : device.getThumbnail() != null)
            return false;
        if (getName() != null ? !getName().equals(device.getName()) : device.getName() != null)
            return false;
        return !(getAddress() != null ? !getAddress().equals(device.getAddress()) : device.getAddress() != null);
    }

    @Override
    public int hashCode() {
        int result = getThumbnail() != null ? getThumbnail().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getAddress() != null ? getAddress().hashCode() : 0);
        result = 31 * result + getStatus();
        return result;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private String address;

    public String getAddress() {
        return address;
    }

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    private int status;         // WIFI 设备是否连接网络

    public static final int INACTIVE_STATUS = 0;
    public static final int LOGOUT_STATUS = 1;
    public static final int LOGIN_STATUS = 2;
    public static final int UNKOWN_STATUS = 3;

    private int linkStatus; // 手机是否连接网络

    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 2;


    public void setThumbnail(String image) { thumbnail = image;}
    public String getName() { return name; }
    public String getThumbnail() { return thumbnail; }

    @Override
    public String toString() {
        return "WifiDevice{" +
                "thumbnail='" + thumbnail + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", status=" + status +
                ", linkStatus=" + linkStatus +
                '}';
    }

    public String getId() { return address; }

    public WifiDevice(String thumbnail, String name, String address) {
        this.thumbnail = thumbnail;
        this.name = name;
        this.address = address;
        status = INACTIVE_STATUS;
        linkStatus = DISCONNECTED;

        if (name == null) {
            this.name = "unkown";
        }
    }

    public WifiDevice()
    {
        thumbnail = "";
        name = "";
        address = "";
        status = INACTIVE_STATUS;
        linkStatus = DISCONNECTED;
    }

    public int getLinkStatus() {
        return linkStatus;
    }

    public void setLinkStatus(int linkStatus) {
        this.linkStatus = linkStatus;
    }
}
