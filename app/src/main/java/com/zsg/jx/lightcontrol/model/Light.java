package com.zsg.jx.lightcontrol.model;

import java.io.Serializable;

/**
 * Created by lenovo001 on 2016/5/10.
 */
public class Light implements Serializable {
    private String id;
    private String name;
    public boolean is_on=false;
    private String lightness;
    private String color;
    //    private boolean is_line;
    private byte lightStatu = 0;      //10离线   11在线



    public Light(String id, String name, byte lightStatu) {
        this.id = id;
        this.name = name;
        this.lightStatu = lightStatu;
        this.lightness = "0";
        this.color = "0";
    }

    public Light() {
        this.lightness = "0";
        this.color = "0";
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean is_on() {
        return is_on;
    }

    public void setIs_on(boolean is_on) {
        this.is_on = is_on;
    }

    public String getLightness() {
        return lightness;
    }

    public void setLightness(String lightness) {
        this.lightness = lightness;
    }

    public String getColor() {
        return color;
    }

    public byte getLightStatu() {
        return lightStatu;
    }

    public void setLightStatu(byte lightStatu) {
        this.lightStatu = lightStatu;
    }
    //    public boolean is_line() {
//        return is_line;
//    }
//
//    public void setIs_line(boolean is_line) {
//        this.is_line = is_line;
//    }

    public void setColor(String color) {
        this.color = color;
    }


    @Override
    public String toString() {
        return "Light{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", is_on=" + is_on +
                ", lightness='" + lightness + '\'' +
                ", color='" + color + '\'' +
                ", lightStatu=" + lightStatu +
                '}';
    }
}
