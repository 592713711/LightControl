package com.zsg.jx.lightcontrol.listener;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 作者：GONGXI on 2016/8/12 17:03
 * 邮箱：gongxi@uascent.com
 */
public class EditTextTouchListener implements View.OnTouchListener{
    private Context context;
    private EditText editText;
    public EditTextTouchListener(Context context, EditText editText){
        this.context = context;
        this.editText = editText;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //获得帐号输入框右边的图片
        Drawable drawable = editText.getCompoundDrawables()[2];
        if (drawable == null) {
            return false;
        }
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return false;
        }
        if (event.getX() > editText.getWidth() - drawable.getIntrinsicWidth()) {
            if (editText.getText().length() == 0) {
                Toast.makeText(context, "请填写信息", Toast.LENGTH_SHORT).show();
            } else {
                editText.setText("");
            }
        }
        return false;
    }
}
