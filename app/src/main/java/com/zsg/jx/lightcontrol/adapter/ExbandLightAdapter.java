package com.zsg.jx.lightcontrol.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.listener.MyOnClickListener;
import com.zsg.jx.lightcontrol.listener.MySeekBarListener;
import com.zsg.jx.lightcontrol.model.Light;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * 作者：GONGXI on 2016/8/19 17:51
 * 邮箱：gongxi@uascent.com
 */
public class ExbandLightAdapter extends BaseExpandableListAdapter {
    private static String TAG = "ConLightAdapter";
    private LayoutInflater inflater;
    private LinkedList<String> fatherList;
    private List<LinkedList<Light>> childList;
    private Context mcontext;
    private ExpandableListView expandableListView;
    private MySeekBarListener mySeekBarListener;
    private MyOnClickListener myOnClickListener;

    private HashMap<Integer, View> groupViewMap;

    public ExbandLightAdapter(LinkedList<String> fatherList, List<LinkedList<Light>> childList, Context mcontext) {
        this.fatherList = fatherList;
        this.childList = childList;
        //Log.i(TAG, "childList.size:" + this.childList.size());
        this.mcontext = mcontext;
        inflater = LayoutInflater.from(mcontext);
        groupViewMap = new HashMap<>();
    }

    public ExbandLightAdapter(LinkedList<String> fatherList, List<LinkedList<Light>> childList, Context mcontext
            , ExpandableListView expandableListView, MySeekBarListener mySeekBarListener, MyOnClickListener myOnClickListener) {
        this(fatherList, childList, mcontext);
        this.expandableListView = expandableListView;
        this.mySeekBarListener = mySeekBarListener;
        this.myOnClickListener = myOnClickListener;
    }

    // 返回父列表个数
    @Override
    public int getGroupCount() {
        return fatherList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return fatherList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return (childList.get(groupPosition)).get(childPosition);
    }

    // 返回子列表个数
    @Override
    public int getChildrenCount(int groupPosition) {
        if (childList.size() == 0) {
            return 0;
        }
        return childList.get(groupPosition).size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {

        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    class GroupHolder implements View.OnClickListener {
        ImageView iv_alllight;
        TextView alllight_number;
        TextView equipment_number;
        TextView light_brightness;
        View group_bottomline;
        View group_topline;
        int groupPosition;

        @Override
        public void onClick(View v) {
            Light light = childList.get(groupPosition).get(0);
            if (light.getLightStatu() == 3) {
                //只有在线状态才能改变
                if (Integer.parseInt(light.getLightness()) > 0)
                    //关灯
                    myOnClickListener.onClickSwitch(light.getId(), false);
                else
                    //开灯
                    myOnClickListener.onClickSwitch(light.getId(), true);


            }

        }
    }

    class ChildHolder implements SeekBar.OnSeekBarChangeListener {
        SeekBar brightBar;
        SeekBar chromeBar;
        int groupPosition;

        public ChildHolder() {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar.getId() == R.id.brightBar) {
                TextView tv_progress = ((GroupHolder) groupViewMap.get(groupPosition).getTag()).light_brightness;
                int percent = (int) (((float) progress) / 255 * 100);
                String format = mcontext.getResources().getString(R.string.lightness_per);
                tv_progress.setText(String.format(format, percent));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            Light light = childList.get(groupPosition).get(0);
            mySeekBarListener.stopTouch(light.getId(), brightBar.getProgress(), chromeBar.getProgress());
        }
    }


    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        GroupHolder groupHolder = null;
        Light light = childList.get(groupPosition).get(0);
        if (convertView == null) {
            groupHolder = new GroupHolder();
            convertView = inflater.inflate(R.layout.con_light_group_item,
                    null);
            groupHolder.alllight_number = (TextView) convertView
                    .findViewById(R.id.alllight_number);
            groupHolder.equipment_number = (TextView) convertView
                    .findViewById(R.id.equipement_num);
            groupHolder.light_brightness = (TextView) convertView
                    .findViewById(R.id.light_brightness);
            groupHolder.group_bottomline = (View) convertView
                    .findViewById(R.id.group_line);
            groupHolder.group_topline = (View) convertView.findViewById(R.id.group_line2);
            groupHolder.iv_alllight = (ImageView) convertView.findViewById(R.id.iv_alllight);


            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }
        //第一项
        if (groupPosition == 0) {
            groupHolder.group_topline.setVisibility(View.GONE);
            groupHolder.light_brightness.setVisibility(View.GONE);
            if (light.getLightStatu() == 0)
                groupHolder.iv_alllight.setImageResource(R.drawable.light_alloff);
            else
                groupHolder.iv_alllight.setImageResource(R.drawable.light_allon);

            groupHolder.alllight_number.setText("在线" + light.getLightStatu());
            groupHolder.equipment_number.setText("设备1");
        } else {
            groupHolder.light_brightness.setVisibility(View.VISIBLE);
            String format = mcontext.getResources().getString(R.string.lightness_per);
            int percent = (int) (((float) Integer.parseInt(light.getLightness())) / 255 * 100);
            groupHolder.light_brightness.setText(String.format(format, percent));

            if (light.getLightStatu() == 3) {
                groupHolder.iv_alllight.setImageResource(R.drawable.lighton);
                groupHolder.light_brightness.setTextColor(Color.BLACK);
                groupHolder.alllight_number.setTextColor(Color.BLACK);
                groupHolder.equipment_number.setText("");
                //关灯
                if (light.getLightness().equals("0"))
                    groupHolder.iv_alllight.setImageResource(R.drawable.lightoff);
            } else {
                groupHolder.iv_alllight.setImageResource(R.drawable.lightoff);
                groupHolder.equipment_number.setText("离线");
                groupHolder.equipment_number.setTextColor(Color.parseColor("#c0c0c0"));
                groupHolder.light_brightness.setTextColor(Color.parseColor("#c0c0c0"));
                groupHolder.alllight_number.setTextColor(Color.parseColor("#c0c0c0"));
            }

            //图片 添加点击监听器
            groupHolder.iv_alllight.setOnClickListener(groupHolder);
            groupHolder.groupPosition = groupPosition;
            groupHolder.alllight_number.setText(light.getName());
            groupHolder.equipment_number.setVisibility(View.VISIBLE);

        }
        //最后一项
        if (groupPosition == fatherList.size() - 1) {
            groupHolder.group_bottomline.setVisibility(View.GONE);
        }
        //张开下面的横线没有
        if (expandableListView.isGroupExpanded(groupPosition)) {
            groupHolder.group_bottomline.setVisibility(View.GONE);
        } else {
            if (groupPosition != fatherList.size() - 1) {
                groupHolder.group_bottomline.setVisibility(View.VISIBLE);
            }
        }


//        if (groupPosition == 0) {
//            groupHolder.group_line1.setVisibility(View.GONE);
//        } else {
//            groupHolder.group_line1.setVisibility(View.VISIBLE);
//        }
//        groupHolder.group_name
//                .setText(fatherList.get(groupPosition));

        groupViewMap.put(groupPosition, convertView);
        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder childHolder = null;
        Light light = childList.get(groupPosition).get(0);
        if (convertView == null) {
            childHolder = new ChildHolder();
            convertView = inflater.inflate(R.layout.con_light_child_item,
                    null);
            childHolder.brightBar = (SeekBar) convertView.findViewById(R.id.brightBar);
            childHolder.chromeBar = (SeekBar) convertView.findViewById(R.id.chromeBar);
            childHolder.brightBar.setMax(255);
            childHolder.chromeBar.setMax(255);
//            childHolder.child_name = (TextView) convertView
//                    .findViewById(R.id.child_name);
//            childHolder.light_no = (TextView) convertView
//                    .findViewById(R.id.light_no);
//            childHolder.light_of_on = (ImageView) convertView
//                    .findViewById(R.id.light_of_on);
//            childHolder.child_line = convertView.findViewById(R.id.child_line);
            convertView.setTag(childHolder);
        } else {
            childHolder = (ChildHolder) convertView.getTag();
        }

        if (groupPosition != 0) {
            childHolder.brightBar.setProgress(Integer.parseInt(light.getLightness()));
            childHolder.chromeBar.setProgress(255-Integer.parseInt(light.getColor()));
        }
        childHolder.brightBar.setOnSeekBarChangeListener(childHolder);
        childHolder.chromeBar.setOnSeekBarChangeListener(childHolder);
        childHolder.groupPosition = groupPosition;

        return convertView;
    }


}
