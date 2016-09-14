package com.zsg.jx.lightcontrol.ui;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.zsg.jx.lightcontrol.R;

@SuppressLint("ValidFragment")
public class SelfFragment extends Fragment implements View.OnClickListener{
    private static final String TAG ="testSelfFragment" ;
    private boolean D=true;
    private RelativeLayout rlPersonInfo;
    private RelativeLayout rlPersonSpecial;
    private RelativeLayout rlPersonNewDevice;
    private RelativeLayout rlPersonIntroduce;
    private RelativeLayout rlPersonHelpFade;
    private RelativeLayout rlPersonAboutUs;

    public SelfFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_self, container, false);

        initView(v);

        return v;
    }

    private void initView(View v){
        rlPersonInfo= (RelativeLayout) v.findViewById(R.id.rlPersonInfo);
        rlPersonInfo.setOnClickListener(this);

        rlPersonSpecial=(RelativeLayout)v.findViewById(R.id.rl_special);
        rlPersonSpecial.setOnClickListener(this);

        rlPersonNewDevice=(RelativeLayout)v.findViewById(R.id.rl_allnewdevice);
        rlPersonNewDevice.setOnClickListener(this);

        rlPersonIntroduce=(RelativeLayout)v.findViewById(R.id.rl_funcintroduce);
        rlPersonIntroduce.setOnClickListener(this);

        rlPersonHelpFade=(RelativeLayout)v.findViewById(R.id.rl_helpfade);
        rlPersonHelpFade.setOnClickListener(this);

        rlPersonAboutUs=(RelativeLayout)v.findViewById(R.id.rl_aboutus);
        rlPersonAboutUs.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rlPersonInfo:
                if(D) Log.d(TAG,"Click rlPersonInfo");
                Intent intent=new Intent(getActivity(),PersonInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_special:
                if(D) Log.d(TAG,"Click rlSpecial");
                Intent intent2=new Intent(getActivity(),SpecialFunctionActivity.class);
                startActivity(intent2);
                break;
            case R.id.rl_allnewdevice:
                if(D) Log.d(TAG,"Click rlAllNewDevice");
                initDialog(getActivity());
                break;

            case R.id.rl_funcintroduce:
                if(D) Log.d(TAG,"Click rlFunction");
                Intent intent3=new Intent(getActivity(),FunctionIntroduceActivity.class);
                startActivity(intent3);
                break;
            case R.id.rl_helpfade:
                Intent intent4=new Intent(getActivity(),HelpFadeCallbackActivity.class);
                startActivity(intent4);
                break;
            case R.id.rl_aboutus:
                Intent intent5=new Intent(getActivity(), AboutUsActivity.class);
                startActivity(intent5);
                break;
        }
    }

    /**
     * 初始化弹出对话框
     * @param context
     */
    private void initDialog(final Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.allownewdevice_dialog, null);
        final Dialog dlg = new Dialog(context, R.style.common_dialog);
        Window window = dlg.getWindow();
        dlg.setContentView(view);
        dlg.show();
        RelativeLayout rl_submit=(RelativeLayout)view.findViewById(R.id.rl_submit);
        RelativeLayout rl_cancel=(RelativeLayout)view.findViewById(R.id.rl_cancel);

        View.OnClickListener listener=new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.rl_submit:
                        Toast.makeText(context,"点击了确定",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.rl_cancel:
                        Toast.makeText(context,"点击了取消",Toast.LENGTH_SHORT).show();
                        break;
                }
                dlg.dismiss();
            }

        };
        rl_submit.setOnClickListener(listener);
        rl_cancel.setOnClickListener(listener);


        // 设置相关位置，一定要在 show()之后
        Window window2 = dlg.getWindow();
        WindowManager.LayoutParams params = window2.getAttributes();
        window.setAttributes(params);
    }

}