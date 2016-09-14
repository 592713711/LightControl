package com.zsg.jx.lightcontrol.listener;

/**
 * Created by zsg on 2016/8/23.
 */
public interface MyOnClickListener{
    //status：需要灯泡开启的状态
    public void onClickSwitch(String light_id,boolean status);
}
