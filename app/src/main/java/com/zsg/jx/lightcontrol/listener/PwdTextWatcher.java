package com.zsg.jx.lightcontrol.listener;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 作者：GONGXI on 2016/8/12 17:03
 * 邮箱：gongxi@uascent.com
 */
public class PwdTextWatcher implements TextWatcher {
    //允许输入的最大长度
    public static final int mMaxLength = 16;
    private Context context;
    private EditText etUserPwd;

    private int length = 0;
    private int selectionEnd = 0;

    public PwdTextWatcher(Context context, EditText etUserPwd) {
        this.context = context;
        this.etUserPwd = etUserPwd;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String value = etUserPwd.getText().toString();
        String userPwd =pwdFilter(value.toString());
        if (!value.equals(userPwd)) {
            Toast.makeText(context, "格式不正确", Toast.LENGTH_SHORT).show();
            etUserPwd.setText(userPwd);
            //光标置后
            etUserPwd.setSelection(userPwd.length());
        }

    }

    @Override
    public void afterTextChanged(Editable s) {
        length = s.toString().length();
        if (length > mMaxLength) {
            Toast.makeText(context,"密码过长！", Toast.LENGTH_SHORT).show();
            selectionEnd = etUserPwd.getSelectionEnd();
            s.delete(length, selectionEnd);
        }
    }
    /**
     * 密码过滤
     * @param string
     * @return
     */
    public static String pwdFilter(String string) {
        String regEx = "[^a-zA-z0-9_*.]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(string);
        return m.replaceAll("").trim();
    }
}
