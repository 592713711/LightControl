package com.zsg.jx.lightcontrol.listener;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;

import com.zsg.jx.lightcontrol.R;


/**
 * 作者：GONGXI on 2016/8/12 17:03
 * 邮箱：gongxi@uascent.com
 */
public class EditTextFocusListener implements View.OnFocusChangeListener{
    private Context context;
    private EditText editText;
    public EditTextFocusListener(Context context, EditText editText){
        this.context = context;
        this.editText = editText;
    }
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        //加载图标
        Drawable clear = ContextCompat.getDrawable(context,/**
         * 作者：GONGXI on 2016/8/12 17:03
         * 邮箱：gongxi@uascent.com
         */ R.drawable.ic_clear_24dp);
        //设置边框
        clear.setBounds(0,0,clear.getIntrinsicWidth(),clear.getIntrinsicHeight());
        editText.setCompoundDrawables(
                null,
                null,
                hasFocus?clear:null,
                null
        );
    }
}
