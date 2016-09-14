package com.zsg.jx.lightcontrol.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.TextView;

import com.zsg.jx.lightcontrol.R;

public class AboutUsActivity extends AppCompatActivity {
    private TextView text_version;
    private String verison="LightControl 1.0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        text_version= (TextView) findViewById(R.id.text_version);
        text_version.setText(verison);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
