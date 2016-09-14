package com.zsg.jx.lightcontrol.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.zsg.jx.lightcontrol.R;

public class AddLightActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText edit_name;
    private EditText edit_num;
    private String light_name;
    private String light_no;
    private ImageButton btn_complte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_light);
        initView();

    }
    private void initView(){
        edit_name= (EditText) findViewById(R.id.editText_name);
        edit_num= (EditText) findViewById(R.id.editText_num);
        btn_complte=(ImageButton)findViewById(R.id.complete);
        btn_complte.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.complete:
                light_name=edit_name.getText().toString().trim();
                light_no=edit_num.getText().toString().trim();
                if (light_name.length() == 0) {
                    setHintText(edit_name, getResources().getString(
                            R.string.light_name_empty_remind));
                    return;
                } else if (light_name.length() > 20) {
                    setHintText(edit_name, getResources().getString(
                            R.string.light_name_format_remind));
                    return;
                }
                if (light_no.length() == 0) {
                    setHintText(edit_num, getResources().getString(
                            R.string.light_no_empty_remind));
                    return;
                } else if (Integer.valueOf(light_no) < 1 || Integer.valueOf(light_no) > 255) {
                    setHintText(edit_num, getResources().getString(
                            R.string.light_no_format_remind));
                    return;
                }
                goBack();
                break;
            default:
                break;
        }
    }
    private void goBack() {
        Intent intent = new Intent();
        intent.putExtra("light_name", light_name);
        intent.putExtra("light_no", light_no);
        intent.putExtra("group_index", getIntent().getIntExtra("group_index", 0));
        setResult(RESULT_OK, intent);
        finish();
    }
    /**
     * et  输入错误提示
     *
     * @param et
     * @param str
     */
    private void setHintText(EditText et, String str) {
        et.setText("");
        et.setHintTextColor(Color.parseColor("#FF4070"));
        et.setHint(str);
    }

    public void doBack(View v){
        finish();
    }
}
