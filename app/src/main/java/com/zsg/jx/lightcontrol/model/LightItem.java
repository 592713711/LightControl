package com.zsg.jx.lightcontrol.model;

import java.util.LinkedList;

/**
 * 作者：GONGXI on 2016/8/29 15:26
 * 邮箱：gongxi@uascent.com
 */
public class LightItem {
    public int mType;
    private LinkedList<String> fatherList;

    public int getmType() {
        return mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }

    public LinkedList<String> getFatherList() {
        return fatherList;
    }

    public void setFatherList(LinkedList<String> fatherList) {
        this.fatherList = fatherList;
    }

    public LightItem(int type, LinkedList<String> mfatherList)
    {
        mType = type;
        fatherList = mfatherList;
    }
}
