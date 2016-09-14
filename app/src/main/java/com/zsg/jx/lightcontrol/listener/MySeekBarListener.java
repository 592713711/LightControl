package com.zsg.jx.lightcontrol.listener;

/**
 * Created by zsg on 2016/8/23.
 */
public interface MySeekBarListener {
    //停止滑动回调
    public void stopTouch(String light_id,int lightness,int chrome);
}
