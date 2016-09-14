package com.zsg.jx.lightcontrol.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.adapter.SceneAdapter;
import com.zsg.jx.lightcontrol.model.Area;
import com.zsg.jx.lightcontrol.model.DeviceGroup;
import com.zsg.jx.lightcontrol.model.Light;
import com.zsg.jx.lightcontrol.model.LightList;
import com.zsg.jx.lightcontrol.model.WifiDevice;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AddAreaActivity extends Activity implements View.OnClickListener,
        ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener
        , ExpandableListView.OnItemLongClickListener {
    private static final String TAG = "testAddAreaActivity";
    private ExpandableListView expandableListView;
    private List<DeviceGroup> parentList = new ArrayList<DeviceGroup>();
    private List<List<Light>> childData = new ArrayList<>();
    private SceneAdapter adapter;

    private EditText scene_name;
    private TextView next;
    LinkedList<Light> onLineList = new LinkedList<>();
    private WifiDevice device;

    private int type;

    public static final int ADD_TYPE = 1;
    public static final int UPDATE_TYPE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_area);

        LightList temp = (LightList) getIntent().getSerializableExtra("lightlist");
        for (Light light : temp.list) {
            if (light.getLightStatu() == 3)
                onLineList.add(light);
        }
        type = getIntent().getIntExtra("type", 0);
        device = (WifiDevice) getIntent().getSerializableExtra("device");

        initView();
        //获取灯泡列表
        initData();
        initListener();

    }

    public void initData() {
        //更新灯泡列表

        //灯泡列表
        DeviceGroup group = new DeviceGroup(1, onLineList.size(), onLineList.size());
        parentList.add(group);

        LinkedList<Light> linklight = new LinkedList<>();     //唯一一组
        //将灯泡添加进分组中
        for (Light light : onLineList) {
            if (type == ADD_TYPE)
                linklight.add(light);
            else {
                Area area = (Area) getIntent().getSerializableExtra("area");
                linklight.add(light);
                for (Light lightTemp : area.getList().list)
                    if (lightTemp.getId() == light.getId())
                        light.setIs_on(true);

            }
        }
        childData.add(linklight);
        adapter = new SceneAdapter(parentList, childData, AddAreaActivity.this, expandableListView);
        expandableListView.setAdapter(adapter);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                //父元素只能点击一项
                for (int i = 0; i < adapter.getGroupCount(); i++) {
                    if (groupPosition != i) {
                        expandableListView.collapseGroup(i);
                    }
                }
            }
        });
        // 为列表绑定数据源
        expandableListView.setAdapter(adapter);

    }

    public void initListener() {
        expandableListView.setOnGroupClickListener(this);
        expandableListView.setOnChildClickListener(this);
        expandableListView.setOnItemLongClickListener(this);
    }

    public void initView() {
        expandableListView = (ExpandableListView) findViewById(R.id.list);
        // 设置默认图标为不显示状态
        expandableListView.setGroupIndicator(null);
        // 设置默认图标为不显示状态
        expandableListView.setGroupIndicator(null);
        scene_name = (EditText) findViewById(R.id.edit_scename);
        if (type == UPDATE_TYPE) {
            Area area = (Area) getIntent().getSerializableExtra("area");
            scene_name.setText(area.getArea_name());
        }
        next = (TextView) findViewById(R.id.text_next);
        next.setOnClickListener(this);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Light light = childData.get(groupPosition).get(
                childPosition);

        if (light.is_on) {
            light.is_on = false;
        } else {
            light.is_on = true;
        }
        adapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_next:
                next(this);
                break;
        }

    }

    //传灯泡信息列表过去
    private void next(Context context) {
        String name = scene_name.getText().toString();

        if (name.length() == 0) {
            Toast.makeText(context, "输入的名称不能为空", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(context, AddScenLightActivity.class);
            //传情景模式的名称
            intent.putExtra("lightname", name);
            LinkedList<Light> selectLights = new LinkedList<>();
            for (Light light : onLineList) {
                if (light.is_on)
                    selectLights.add(light);
            }

            if (selectLights.isEmpty()) {
                Toast.makeText(context, "请选择添加的灯泡", Toast.LENGTH_SHORT).show();
            } else {
                LightList temp = new LightList();
                temp.list = selectLights;
                intent.putExtra("alter_type", type);
                intent.putExtra("model_type", AddScenLightActivity.AREA_TYPE);
                intent.putExtra("lightlist", temp);
                intent.putExtra("device", device);
                if (type == UPDATE_TYPE) {
                    Area area = (Area) getIntent().getSerializableExtra("area");
                    intent.putExtra("id", area.getAreaId());
                }
                startActivity(intent);
                finish();
            }

        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

        adapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }


}

