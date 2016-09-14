package com.zsg.jx.lightcontrol.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.zsg.jx.lightcontrol.R;

public class FunctionIntroduceActivity extends AppCompatActivity {
    private int [] image=new int[]{R.drawable.guide01_chinese, R.drawable.guide02_chinese, R.drawable.guide03_chinese,
    R.drawable.guide05_chinese, R.drawable.guide_main, R.drawable.guide_house, R.drawable.guide_light,
    R.drawable.guide_gateway, R.drawable.guide_scen, R.drawable.guide_setting};

    private ImageView show;
    private int currentImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_introduce);
        show= (ImageView) findViewById(R.id.img_show);
        show.setImageResource(R.drawable.guide01_chinese);
        currentImg=0;
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentImg<image.length-1){
                    show.setImageResource(image[++currentImg]);
                }else {
                    finish();
                }

            }
        });
    }
}
