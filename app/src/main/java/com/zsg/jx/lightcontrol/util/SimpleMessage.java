package com.zsg.jx.lightcontrol.util;

/**
 * 普通消息
 * Created by zsg on 2016/8/18.
 */
public class SimpleMessage {
    public int tag;
    public String json;

    public SimpleMessage(int tag, String json) {
        this.tag = tag;
        this.json = json;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
