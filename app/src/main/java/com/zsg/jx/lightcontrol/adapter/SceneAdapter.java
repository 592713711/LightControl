package com.zsg.jx.lightcontrol.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.model.DeviceGroup;
import com.zsg.jx.lightcontrol.model.Light;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 作者：GONGXI on 2016/8/23 10:16
 * 邮箱：gongxi@uascent.com
 */
public class SceneAdapter extends BaseExpandableListAdapter {
    private static String TAG = "SceneAdapter";
    private LayoutInflater inflater;
    private List<DeviceGroup> parentList = new ArrayList<>();
    private List<List<Light>> childData = new ArrayList<>();
    private Context mcontext;
    private ExpandableListView expandableListView;

    public SceneAdapter(List<DeviceGroup> parentList, List<List<Light>> childData, Context mcontext) {
        this.parentList = parentList;
        this.childData = childData;
        this.mcontext = mcontext;
        inflater = LayoutInflater.from(mcontext);
    }

    public SceneAdapter(List<DeviceGroup> parentList, List<List<Light>> childData, Context mcontext, ExpandableListView expandableListView) {
        this(parentList, childData, mcontext);
        this.expandableListView = expandableListView;

    }

    // 返回父列表个数
    @Override
    public int getGroupCount() {
        return parentList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return parentList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return (childData.get(groupPosition)).get(childPosition);
    }

    // 返回子列表个数
    @Override
    public int getChildrenCount(int groupPosition) {

        return childData.get(groupPosition).size();
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


    class GroupHolder {
        ImageView iv_alllight;
        TextView alllight_number;
        //View group_bottomline;
        CheckBox check_light;
    }

    class ChildHolder {
        TextView light_name;
        CheckBox check_light;
        ImageView iv_light;
    }


    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded,
                             View convertView, ViewGroup parent) {
        GroupHolder groupHolder = null;
        if (convertView == null) {
            groupHolder = new GroupHolder();
            convertView = inflater.inflate(R.layout.scen_light,
                    null);
            groupHolder.alllight_number = (TextView) convertView
                    .findViewById(R.id.alllight_number);
            groupHolder.iv_alllight = (ImageView) convertView.findViewById(R.id.iv_alllight);
            groupHolder.check_light = (CheckBox) convertView.findViewById(R.id.check_light);

            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }
        final DeviceGroup deviceGroup = parentList.get(groupPosition);
        groupHolder.alllight_number.setText("照明在线：" + deviceGroup.getOnLineCount());


        if (deviceGroup.is_on)
            groupHolder.check_light.setChecked(true);
        else
            groupHolder.check_light.setChecked(false);

        /*
        * groupListView的点击事件
		*/
        groupHolder.check_light.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                CheckBox check_light = (CheckBox) v
                        .findViewById(R.id.check_light);
          /*      if (!isExpanded) {
                    //展开某个group view
                    //expandableListView.expandGroup(groupPosition);
                } else {
                    //关闭某个group view
                    //expandableListView.collapseGroup(groupPosition);
                }*/

                if (!deviceGroup.is_on) {
                    expandableListView.expandGroup(groupPosition);
                    check_light.setChecked(true);
                    deviceGroup.is_on=true;
                    List<Light> list = childData
                            .get(groupPosition);
                    for (Light light : list) {
                        light.is_on=true;
                    }
                } else {
                    check_light.setChecked(false);
                    deviceGroup.is_on=false;
                    List<Light> list = childData
                            .get(groupPosition);
                    for (Light light : list) {
                       light.is_on=false;
                    }
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder childHolder = null;
        if (convertView == null) {
            childHolder = new ChildHolder();
            convertView = inflater.inflate(R.layout.scen_light_item,
                    null);
            childHolder.light_name = (TextView) convertView
                    .findViewById(R.id.light_name);
            childHolder.check_light = (CheckBox) convertView.
                    findViewById(R.id.light_check);
            childHolder.iv_light = (ImageView) convertView.
                    findViewById(R.id.iv_alllight);
            convertView.setTag(childHolder);
        } else {
            childHolder = (ChildHolder) convertView.getTag();
        }
        Light light=childData.get(groupPosition).get(childPosition);
        if (light.is_on) {
            childHolder.check_light.setChecked(true);
        } else {
            childHolder.check_light.setChecked(false);
        }
        if(light.getLightStatu()==3)
            childHolder.iv_light.setImageResource(R.drawable.lighton2);
        else
            childHolder.iv_light.setImageResource(R.drawable.lightoff);

        childHolder.light_name.setText(light.getName());
        return convertView;
    }


}
