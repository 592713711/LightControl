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
import com.zsg.jx.lightcontrol.model.LightItem;
import com.zsg.jx.lightcontrol.model.Theme;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 作者：GONGXI on 2016/8/24 09:21
 * 邮箱：gongxi@uascent.com
 */
public class ExbandDiffLightAdapter extends BaseExpandableListAdapter {
    private static String TAG = "textExbandDiffLightAdapter";
    private LayoutInflater inflater;
    private List<String> fatherList;
    private List<LinkedList<Light>> childList;
    private Context mcontext;
    private ExpandableListView expandableListView;
    private MySeekBarListener mySeekBarListener;
    private MyOnClickListener myOnClickListener;
    private HashMap<Integer, View> groupViewMap;

    private final static int LIGHT_PARENT = 0;//总项
    private final static int LIGHT_CHILD = 1;//分项

    public ExbandDiffLightAdapter(List<String> fatherList, List<LinkedList<Light>> childList, Context mcontext) {
        this.fatherList = fatherList;
        this.childList = childList;
        //Log.i(TAG, "childList.size:" + this.childList.size());
        this.mcontext = mcontext;
        inflater = LayoutInflater.from(mcontext);
        groupViewMap = new HashMap<>();
    }

    public ExbandDiffLightAdapter(List<String> fatherList, List<LinkedList<Light>> childList, Context mcontext, ExpandableListView expandableListView
            , MySeekBarListener mySeekBarListener, MyOnClickListener myOnClickListener) {
        this(fatherList, childList, mcontext);
        this.expandableListView = expandableListView;
        this.mySeekBarListener = mySeekBarListener;
        this.myOnClickListener = myOnClickListener;
    }

    @Override
    public int getGroupType(int groupPosition) {
        if (Integer.parseInt(childList.get(groupPosition).get(0).getId()) == 0)
            return 0;
        else
            return 1;
    }

    @Override
    public int getGroupTypeCount() {
        return 2;
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


    class ParentGroupHolder implements View.OnClickListener {
        ImageView iv_alllight;
        TextView alllight_number;
        TextView equipment_number;
        View group_bottomline;
        View group_topline;
        int groupPosition;

        @Override
        public void onClick(View v) {
            Light light = childList.get(groupPosition).get(0);
            if (light.getLightStatu() == 3||light.getId().equals("0")) {
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

    class ChildGroupHolder implements View.OnClickListener {
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

            if (light.getLightStatu() == 3||light.getId().equals("0")) {
                //只有在线状态才能改变
                if (Integer.parseInt(light.getLightness()) > 0) {
                    //关灯
                    Log.d(TAG, "关灯");
                    myOnClickListener.onClickSwitch(light.getId(), false);
                } else {
                    //开灯
                    Log.d(TAG, "开灯");
                    myOnClickListener.onClickSwitch(light.getId(), true);
                }


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
                if (groupPosition == 0) {

                } else {
                    TextView tv_progress = ((ChildGroupHolder) groupViewMap.get(groupPosition).getTag()).light_brightness;
                    int percent = (int) (((float) progress) / 255 * 100);
                    String format = mcontext.getResources().getString(R.string.lightness_per);
                    tv_progress.setText(String.format(format, percent));
                }
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
        ParentGroupHolder groupHolder = null;
        ChildGroupHolder childHolder = null;
        Light light = childList.get(groupPosition).get(0);
        int type = getGroupType(groupPosition);
        if (type == LIGHT_PARENT) {

            if (convertView == null) {
                groupHolder = new ParentGroupHolder();

                convertView = inflater.inflate(R.layout.con_light_group_item2,
                        null);
                groupHolder.alllight_number = (TextView) convertView
                        .findViewById(R.id.alllight_number);
                groupHolder.equipment_number = (TextView) convertView
                        .findViewById(R.id.equipement_num);
                groupHolder.group_bottomline = (View) convertView
                        .findViewById(R.id.group_line);
                groupHolder.group_topline = (View) convertView.findViewById(R.id.group_line2);
                groupHolder.iv_alllight = (ImageView) convertView.findViewById(R.id.iv_alllight);
                convertView.setTag(groupHolder);
            } else {
                groupHolder = (ParentGroupHolder) convertView.getTag();
            }
            groupHolder.group_topline.setVisibility(View.INVISIBLE);
            if (light.getLightStatu() == 0) {
                groupHolder.iv_alllight.setImageResource(R.drawable.light_alloff);
            } else {
                groupHolder.iv_alllight.setImageResource(R.drawable.light_allon);
            }
            groupHolder.alllight_number.setText("在线" + light.getLightStatu());
            groupHolder.equipment_number.setText("设备" + (fatherList.size() - 1));
            groupHolder.iv_alllight.setOnClickListener(groupHolder);
            groupHolder.groupPosition = groupPosition;

            //张开下面的横线没有
            if (expandableListView.isGroupExpanded(groupPosition)) {
                groupHolder.group_bottomline.setVisibility(View.GONE);
            } else {
                if (groupPosition != fatherList.size() - 1) {
                    groupHolder.group_bottomline.setVisibility(View.VISIBLE);
                }
            }

        } else {
            if (childHolder == null) {
                childHolder = new ChildGroupHolder();
                convertView = inflater.inflate(R.layout.con_light_group_item,
                        null);
                childHolder.alllight_number = (TextView) convertView
                        .findViewById(R.id.alllight_number);
                childHolder.equipment_number = (TextView) convertView
                        .findViewById(R.id.equipement_num);
                childHolder.group_bottomline = (View) convertView
                        .findViewById(R.id.group_line);
                childHolder.light_brightness = (TextView) convertView
                        .findViewById(R.id.light_brightness);
                childHolder.group_topline = (View) convertView.findViewById(R.id.group_line2);
                childHolder.iv_alllight = (ImageView) convertView.findViewById(R.id.iv_alllight);
                convertView.setTag(childHolder);
            } else {
                childHolder = (ChildGroupHolder) convertView.getTag();
            }
            String format = mcontext.getResources().getString(R.string.lightness_per);
            int percent = (int) (((float) Integer.parseInt(light.getLightness())) / 255 * 100);
            childHolder.light_brightness.setText(String.format(format, percent));
            childHolder.iv_alllight.setOnClickListener(childHolder);
            childHolder.groupPosition = groupPosition;

            if (light.getLightStatu() == 3) {
                childHolder.iv_alllight.setImageResource(R.drawable.lighton);
                childHolder.light_brightness.setTextColor(Color.BLACK);
                childHolder.alllight_number.setTextColor(Color.BLACK);
                childHolder.equipment_number.setText("");
                //关灯
                if (light.getLightness().equals("0"))
                    childHolder.iv_alllight.setImageResource(R.drawable.lightoff);
            } else {
                childHolder.iv_alllight.setImageResource(R.drawable.lightoff);
                childHolder.equipment_number.setText("离线");
                childHolder.equipment_number.setTextColor(Color.parseColor("#c0c0c0"));
                childHolder.light_brightness.setTextColor(Color.parseColor("#c0c0c0"));
                childHolder.alllight_number.setTextColor(Color.parseColor("#c0c0c0"));
            }
            childHolder.alllight_number.setText(light.getName());
            //最后一项没有横线
            if (groupPosition == fatherList.size() - 1) {
                childHolder.group_bottomline.setVisibility(View.GONE);
            }
            //张开下面的横线没有
            if (expandableListView.isGroupExpanded(groupPosition)) {
                childHolder.group_bottomline.setVisibility(View.GONE);
            } else {
                if (groupPosition != fatherList.size() - 1) {
                    childHolder.group_bottomline.setVisibility(View.VISIBLE);
                }
            }
        }
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
            convertView.setTag(childHolder);
        } else {
            childHolder = (ChildHolder) convertView.getTag();
        }

        if (groupPosition != 0) {
            childHolder.brightBar.setProgress(Integer.parseInt(light.getLightness()));
            childHolder.chromeBar.setProgress(255 - Integer.parseInt(light.getColor()));
        }
        childHolder.brightBar.setOnSeekBarChangeListener(childHolder);
        childHolder.chromeBar.setOnSeekBarChangeListener(childHolder);
        childHolder.groupPosition = groupPosition;

        return convertView;
    }


}
