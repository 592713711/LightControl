package com.zsg.jx.lightcontrol.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.model.DeviceGroup;

import java.util.ArrayList;


/**
 * 显示全部设备的适配器
 * Created by zsg on 2016/8/15.
 */
public class AllDeviceAdapter extends BaseAdapter {
    private ArrayList<DeviceGroup> datas;
    private Context context;
    private LayoutInflater inflater;

    public AllDeviceAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        datas = new ArrayList<>();
    }


    public void updateData(ArrayList<DeviceGroup> newdata) {
        datas.clear();
        datas.addAll(newdata);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holderView;

        if (convertView == null) {
            holderView = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.devicegroup_item, null);
            holderView.image = (ImageView) convertView.findViewById(R.id.iv_device_image);
            holderView.type = (TextView) convertView.findViewById(R.id.tv_device_type);
            holderView.onlineCount = (TextView) convertView.findViewById(R.id.tv_online_count);
            holderView.allCount = (TextView) convertView.findViewById(R.id.tv_all_count);
            convertView.setTag(holderView);
        } else {
            holderView = (ViewHolder) convertView.getTag();
        }


        DeviceGroup group = datas.get(position);

        //设置设备组图标
        holderView.type.setText(DeviceGroup.getTypeToString(group.getDeviceType()));
        int drawable_id=0;
        switch (group.getDeviceType()) {
            case DeviceGroup.LIGHT_TYPE:
                if (group.getOnLineCount() > 0)
                    drawable_id=R.drawable.lighton;
                else
                    drawable_id=R.drawable.lightoff;
                break;
            case DeviceGroup.SWITCH_TYPE:
                if (group.getOnLineCount() > 0)
                    drawable_id=R.drawable.switchon;
                else
                    drawable_id=R.drawable.switchoff;
                break;
            case DeviceGroup.SENSOR_TYPE:
                if (group.getOnLineCount() > 0)
                    drawable_id=R.drawable.sensoron;
                else
                    drawable_id=R.drawable.sensoroff;
                break;
        }
        holderView.image.setImageResource(drawable_id);
        holderView.onlineCount.setText("在线"+group.getOnLineCount());
        holderView.allCount.setText("设备"+group.getDeviceCount());
        return convertView;
    }

    private final static class ViewHolder {
        public ImageView image;
        public TextView type;
        public TextView onlineCount;
        public TextView allCount;
    }
}
