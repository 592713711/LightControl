package com.zsg.jx.lightcontrol.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.model.Area;
import com.zsg.jx.lightcontrol.ui.AreaDeviceFragment;

import java.util.ArrayList;

/**
 * Created by zsg on 2016/8/15.
 */
public class AreaDeviceAdapter extends BaseAdapter {
    private ArrayList<Area> datas;
    private Context context;
    private LayoutInflater inflater;
    private AreaDeviceFragment.OnAreaClickListener listener;

    public AreaDeviceAdapter(Context context, AreaDeviceFragment.OnAreaClickListener listener) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        datas = new ArrayList<>();
        this.listener = listener;
    }


    public void updateData(ArrayList<Area> newdata) {
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
            convertView = inflater.inflate(
                    R.layout.devicearea_item, null);
            holderView.name1 = (TextView) convertView.findViewById(R.id.tv_areaname1);
            holderView.name2 = (TextView) convertView.findViewById(R.id.tv_areaname2);
            convertView.setTag(holderView);
        } else {
            holderView = (ViewHolder) convertView.getTag();
        }

        //判断是否是最后一个
        if (position == datas.size() - 1) {
            holderView.name1.setText("");
            holderView.name1.setBackground(context.getResources().getDrawable(R.drawable.defines_icon2));
            holderView.name2.setText("自定义");
            return convertView;
        }

        Area area = datas.get(position);

        if (area.getArea_name().length() > 2)
            holderView.name1.setText(area.getArea_name().substring(0, 2));
        else
            holderView.name1.setText(area.getArea_name());
        holderView.name2.setText(area.getArea_name());
        holderView.name1.setOnClickListener(holderView);
        holderView.position = position;

        if (area.isOpen)
            holderView.name1.setBackgroundResource(R.drawable.circle_1);
        else
            holderView.name1.setBackgroundResource(R.drawable.circle_2);
        return convertView;
    }

    private class ViewHolder implements View.OnClickListener {
        public TextView name1;
        public TextView name2;
        public int position;


        @Override
        public void onClick(View v) {
            listener.onAreaClick(position);

        }
    }
}

