// IService.aidl
package com.zsg.jx.lightcontrol;

// Declare any non-default types here with import statements
import com.zsg.jx.lightcontrol.ICallback;

interface IService {
    void registerCallback(ICallback cb);
    void unregisterCallback(ICallback cb);

    boolean initialize();
    boolean connect(String addr);
    void disconnect(String addr);
    //设置led灯的状态
    void enableLight(String addr, boolean on);

    //获取led灯的状态
    void getLightStatus(String addr);

    void ping(String addr, int val);

    //获取wifi下的灯泡列表
    void getLightList(String address);

    //   设置灯泡的亮度和色温
    void setBrightChrome(String address, int index, int bright, int chrome);

    //获取灯泡的亮度和色温
    void getBrightChrome(String address, int index);

    //添加彩灯
    void pairLight(String address, int index);

    void clearCmdList();
}
