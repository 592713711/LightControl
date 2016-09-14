package com.zsg.jx.lightcontrol.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.model.Light;
import com.zsg.jx.lightcontrol.model.LightList;
import com.zsg.jx.lightcontrol.model.Theme;
import com.zsg.jx.lightcontrol.util.DialogUtil;
import com.zsg.jx.lightcontrol.util.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * 情景模式碎片
 * Created by zsg on 2016/8/12.
 */
@SuppressLint("ValidFragment")
public class ThemeFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private static final String TAG = "testThemeFragment";
    private GridView gview;
    private HomeActivity context;

    private ArrayList<Map<String, Object>> mapArrayList;
    private ArrayList<Theme> themeList;
    private SimpleAdapter sim_adapter;
    // 图片封装为一个数组
    private int[] normal_icons = {R.drawable.quiet_icon1, R.drawable.casual_icon1,
            R.drawable.warm_icon1, R.drawable.work_icon1,
            R.drawable.defines_icon};
    private int[] select_icons = {R.drawable.quiet_icon2, R.drawable.casual_icon2,
            R.drawable.warm_icon2, R.drawable.work_icon2,
            R.drawable.defines_icon};
    private String[] iconName = {"安静", "休闲", "温馨", "工作", "自定义"};

    private Handler handler = new Handler();

    public ThemeFragment(HomeActivity context) {
        this.context = context;
    }

    public ThemeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_theme, container, false);
        initView(view);

        return view;
    }


    private void initView(View v) {
        gview = (GridView) v.findViewById(R.id.theme_gridview);
        mapArrayList = new ArrayList<>();
        themeList = new ArrayList<>();
        //获取数据
        getThemeList();
        //新建适配器
        String[] from = {"image", "text"};
        int[] to = {R.id.image, R.id.text};
        sim_adapter = new SimpleAdapter(
                context, mapArrayList, R.layout.theme_list_item, from, to);
        //配置适配器
        gview.setAdapter(sim_adapter);
        gview.setOnItemClickListener(this);
        gview.setOnItemLongClickListener(this);
    }

    //获取主题列表
    private void getThemeList() {
        themeList.clear();
        mapArrayList.clear();
        themeList.addAll(MyApplication.getInstance().getThemeDao().getAll());

        for (int i = 0; i < normal_icons.length - 1; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("image", normal_icons[i]);
            map.put("text", iconName[i]);
            mapArrayList.add(map);
        }

        for (Theme theme : themeList) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("image", R.drawable.custom_icon1);
            map.put("text", theme.getTheme_name());
            mapArrayList.add(map);
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("image", normal_icons[normal_icons.length - 1]);
        map.put("text", iconName[iconName.length - 1]);
        mapArrayList.add(map);
    }

    //点击图标切换
    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if (position != mapArrayList.size() - 1) {

            //将所有图标置为未选中状态
            for (int i = 0; i < gview.getCount(); i++) {
                int resId = 0;
                if (i < 4)
                    resId = normal_icons[i];
                else {
                    if (i != mapArrayList.size() - 1)
                        resId = R.drawable.custom_icon1;
                    else
                        resId = R.drawable.defines_icon;
                }
                ImageView imageView = (ImageView) gview.getChildAt(i).findViewById(R.id.image);
                imageView.setImageResource(resId);
            }
            int resId = 0;
            if (position < 4)
                resId = select_icons[position];
            else {

                resId = R.drawable.custom_icon2;
            }
            //应用情景  改变图标
            L.e(TAG, "..........点击了：" + position);
            ImageView selectImage = (ImageView) view.findViewById(R.id.image);
            selectImage.setImageResource(resId);

            //发送数据
            if (position < 4) {
                final int a = 50;
                final int b = 50;
                //目前只能控制一盏灯
                for (final Light light : context.lightList) {
                    if (light.getLightStatu() == 3) {

                        //context.showLoadingDialog("请稍等...");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    MyApplication.getInstance().mService.setBrightChrome(context.currentDevice.getAddress(), Integer.parseInt(light.getId())
                                            , a * (position + 1), 255 - b * (position + 1));
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 1000);


                    }

                }

            } else {
                Theme theme = themeList.get(position - 4);
                LinkedList<Light> list = theme.getList().list;
                //目前一次只能控制一盏灯
                for (Light light : list) {
                    try {
                        //context.showLoadingDialog("请稍等...");
                        MyApplication.getInstance().mService.setBrightChrome(context.currentDevice.getAddress(), Integer.parseInt(light.getId())
                                , Integer.parseInt(light.getLightness()), Integer.parseInt(light.getColor()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }


        } else {
            if (isOpen()) {
                Intent intent = new Intent(context, AddScenActivity.class);
                LightList lightlist = new LightList();
                lightlist.list = context.lightList;
                intent.putExtra("lightlist", lightlist);
                intent.putExtra("type", 1);      //1为添加  2为修改
                intent.putExtra("device", context.currentDevice);
                startActivity(intent);
            }

        }

    }

    //长按编辑
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == mapArrayList.size() - 1) return false;

        final AlertDialog dialog = DialogUtil.getSelectDialog(context, true);

        View v = LayoutInflater.from(context).inflate(
                R.layout.select_dialog, null);
        TextView textView1 = (TextView) v.findViewById(R.id.edit_btn);
        textView1.setText("编辑");
        textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "编辑", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        TextView textView2 = (TextView) v.findViewById(R.id.rename_btn);
        textView2.setText("重命名");
        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "重命名", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.setView(v);
        dialog.show();
        return false;
    }


    //是否可以打开情景模式
    public boolean isOpen() {
        //添加情景
        //打开灯控活动
        if (!MyApplication.getInstance().longConnected) {
            context.showShortToast(context.getString(R.string.str_connect_first));
            return false;
        }

        if (context.currentDevice == null) {
            context.showShortToast(context.getString(R.string.nowifidevice_use));
            return false;
        }

        //更新灯泡列表
        int lineCount = 0;
        for (Light light : context.lightList) {
            if (light.getLightStatu() == 3)
                lineCount++;
        }
        if (lineCount == 0) {
            context.showShortToast(context.getString(R.string.device_link_no_light));
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        getThemeList();
        sim_adapter.notifyDataSetChanged();
    }

}
