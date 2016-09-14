package com.zsg.jx.lightcontrol.model;

import java.io.Serializable;

/**
 * 设备类型分组信息
 * 照明 开关  传感器
 * Created by zsg on 2016/8/15.
 */
public class DeviceGroup implements Serializable{
    public static final  int LIGHT_TYPE=1;      //照明
    public static final  int SWITCH_TYPE=2;     //开关
    public static final  int SENSOR_TYPE=3;     //传感器

    private int deviceType;     //类型
    private int onLineCount;    //在线数目
    private int deviceCount;    //设备总数

    public boolean is_on=false;        //用于情景模式  是否选中

    public DeviceGroup(int deviceType, int onLineCount, int deviceCount) {
        this.deviceType = deviceType;
        this.onLineCount = onLineCount;
        this.deviceCount = deviceCount;
    }

    public DeviceGroup() {
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getOnLineCount() {
        return onLineCount;
    }

    public void setOnLineCount(int onLineCount) {
        this.onLineCount = onLineCount;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public static String getTypeToString(int type){
        if(type==LIGHT_TYPE)
            return "照明";
        if(type==SWITCH_TYPE)
            return "开关";
        if(type==SENSOR_TYPE)
            return "传感器";

        return "未知";
    }
}
