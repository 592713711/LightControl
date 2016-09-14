package com.zsg.jx.lightcontrol.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.TextView;

import com.zsg.jx.lightcontrol.R;

public class HelpFadeCallbackActivity extends AppCompatActivity {
    private TextView text_showone;
    private TextView text_showtwo;
    private TextView text_showthree;
    private TextView text_showfour;
    private TextView text_showfive;
    private TextView text_showsix;
    private TextView text_showseven;
    private TextView text_showeight;
    private TextView text_shownine;
    private String oneStr="1、第一次联接设备如何操作。\n\t第一步 将Wifi桥接器联接路由器，插上mini USB电源；\n\t第二步 打开手机" +
            "APP，搜索wifi桥接器；搜索到则手机APP软件提示\"已连接桥接器\"；\n\t第三步 在APP-更多界面，打开允许新灯添入，按提示重启职能灯" +
            "，并等待灯连接wifi桥接器，下拉刷新主页灯列表查看灯具是否已经连接。";
    private String twoStr="2、打开手机APP后显示\"当前网络没有找到桥接器，请检查重试。\"\n\t□手机没有连接WIFI；\n\t□桥接器没有连接路由器的LAN接口；" +
            "\n\t□桥接器和手机没有连接到同一个路由器的信号，需要在手机重新选择正确的wifi信号；\n\t□修正以上选项，重启桥接器电源，打开APP，屏幕上方" +
            "出现\"已连接上wifi桥接器\"，即可成功。";
    private String threeStr="3、如何判断wifi桥接器与路由器联接不成功的常见原因。\n\t□wifi桥接器的网线错插到路由器的输入接口WAN，应该插到LAN输出接口；\n" +
            "\t□LE盒子的网线出口，有两个指示灯，如果无显示，表明供电不良或者联接线松动，没插好，拔出重新插好；\n\t□连接线或mini USB电源故障，换新的。";
    private String fourStr="4、APP首页显\"已联接桥接器\",下拉刷新灯主页没有搜索到智能灯信号\n\t原因一：桥接器没有收到灯的信号。\n\t□打开APP" +
            "\"更多-允许新灯添入\"，按提示10分钟之内重启智能灯，等待1-3秒后，在APP\"主页\"下拉刷新，即可看到显示灯。如果刷新几次后仍未成功显示" +
            "重启wifi桥接器和APP，重新以上动作既可。\n \t原因二：灯信号恰巧被附近wifi桥接器抢先联接上。\n\t□用遥控器将您家的灯进行清码。打开APP\"更多-允许新灯添入\"，" +
            "按提示10分钟之内重启智能灯，等待1-3秒后，在APP\"主页\"下拉刷新，即可看到显示灯。";
    private String fiveStr="5、一个灯能不能同时又多个用户手机控制？\n \t 可以，一个灯只链接一个wifi桥接器，一个控制器可以连多部手机。";
    private String sixStr="6、wifi桥接器不见了，智能灯需绑定另外一个wifi桥接器，一个灯只能联接一个wifi桥接器。\n" +
            "\t□用遥控器先给智能灯和原wifi桥接器清码；\n\t□用新wifi桥接器重新联接灯与手机APP。";
    private String sevenStr="7、添加新灯时，灯没有闪烁，在APP灯列表内无灯具添加代表新灯没有添加成功。\n" +
            "\t请进行两项检查，第一首先仔细查看说明书，重复说明书介绍的连接步骤，如果还没有解决就是灯具出厂的时候，未完全将灯具处于标准的出厂状态。\n" +
            "\t这时就需用遥控器对灯具进行清码，在重新进入您APP上允许新灯添入，按提示操作既可。";
    private String eightStr="8、遥控器对码与清码。\n\t□对码：使用墙壁开关先关闭智能灯，5秒之后重新给智能灯通电，然后快速按3-5次遥控器【开灯】键，看到" +
            "灯光闪动，即对码成功。\n\t□清码：使用已经对码的遥控器，断电，5秒之后重新对智能灯通电，然后快速按3-5次遥控器【关灯】键，看到灯光闪动，即清码成功。";
    private String nineStr="如果以上答案仍未解决您的问题，请关注我司微信公众号寻求帮助。";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_fade_callback);
        text_showone=(TextView)findViewById(R.id.text_showone);
        text_showone.setText(oneStr);

        text_showtwo= (TextView) findViewById(R.id.text_showtwo);
        text_showtwo.setText(twoStr);

        text_showthree=(TextView)findViewById(R.id.text_showthree);
        text_showthree.setText(threeStr);

        text_showfour=(TextView)findViewById(R.id.text_showfour);
        text_showfour.setText(fourStr);

        text_showfive=(TextView)findViewById(R.id.text_showfive);
        text_showfive.setText(fiveStr);

        text_showsix=(TextView)findViewById(R.id.text_showsix);
        text_showsix.setText(sixStr);

        text_showseven=(TextView)findViewById(R.id.text_showseven);
        text_showseven.setText(sevenStr);

        text_showeight=(TextView)findViewById(R.id.text_showeight);
        text_showeight.setText(eightStr);

        text_shownine=(TextView)findViewById(R.id.text_shownine);
        text_shownine.setText(nineStr);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
            return false;
        }
        return super.onKeyDown(keyCode,event);
    }
}
