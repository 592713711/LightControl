package com.zsg.jx.lightcontrol.model;

import java.io.Serializable;

/**
 * Created by zsg on 2016/9/13.
 */
public class Area implements Serializable{
    public int areaId;
    public String area_name;
    public LightList list;
    public boolean isOpen=false;

    public Area(){
        list=new LightList();
    }

    public String getArea_name() {
        return area_name;
    }

    public void setArea_name(String area_name) {
        this.area_name = area_name;
    }

    public LightList getList() {
        return list;
    }

    public void setList(LightList list) {
        this.list = list;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

}
