package com.zsg.jx.lightcontrol.ui;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.util.PreferenceUtil;

import java.util.ArrayList;


public class SplashActivity extends FragmentActivity {
    private static final String TAG ="SplashActivity" ;
    //下面的点
    private ImageView mivIndicatorFirst;
    private ImageView mivIndicatorSecond;
    private ImageView mivIndicatorThird;
    private ImageView mivIndicatorFour;

    //开始体验按钮
    private Button mbtnStart;

    //ViewPager
    private ViewPager mvpMain;
    //Fragment的集合
    private ArrayList<Fragment> mFragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        // set UI
        setUI();

        // initial view page
        initViewPage();
    }
    private void setUI() {
        mbtnStart = (Button) findViewById(R.id.btnStart);
        mbtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.getInstance().getPreferenceUtil().setFirstAppStartFlag(false);
                Intent intent = new Intent(SplashActivity.this, FirstActivity.class);
                SplashActivity.this.startActivity(intent);
                finish();
            }
        });
        mivIndicatorFirst = (ImageView) findViewById(R.id.ivIndicatorFirst);
        mivIndicatorFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(0);
            }
        });

        mivIndicatorSecond = (ImageView) findViewById(R.id.ivIndicatorSecond);
        mivIndicatorSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(1);
            }
        });

        mivIndicatorThird= (ImageView) findViewById(R.id.ivIndicatorThird);
        mivIndicatorThird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(2);
            }
        });

        mivIndicatorFour=(ImageView) findViewById(R.id.ivIndicatorFour);
        mivIndicatorFour.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                changeFragment(3);
            }
        });

        mvpMain = (ViewPager) findViewById(R.id.vpMain);
    }

    private void initViewPage() {
        SplashFragment one = new SplashFragment();
        SplashFragment two = new SplashFragment();
        SplashFragment three = new SplashFragment();
        SplashFragment four = new SplashFragment();

        one.initial(R.drawable.guide01_chinese);
        two.initial(R.drawable.guide02_chinese);
        three.initial(R.drawable.guide03_chinese);
        four.initial(R.drawable.guide04_chinese);


        mFragmentList=new ArrayList<Fragment>();
        mFragmentList.add(one);
        mFragmentList.add(two);
        mFragmentList.add(three);
        mFragmentList.add(four);

        //ViewPager set adapter
        mvpMain.setAdapter(new RealsilFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList));
        //ViewPager page change listener
        mvpMain.setOnPageChangeListener(new mOnPageChangeListener());
        //ViewPager show first fragment
        changeFragment(0);
    }

    private void changeFragment(int item) {
        mvpMain.setCurrentItem(item);
    }

    /**
     * ViewPager change Fragment, Text Color change
     */
    private class mOnPageChangeListener implements ViewPager.OnPageChangeListener{

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    mivIndicatorFirst.setImageResource(R.drawable.page_indicator_focused);
                    mivIndicatorSecond.setImageResource(R.drawable.page_indicator);
                    mivIndicatorThird.setImageResource(R.drawable.page_indicator);
                    mivIndicatorFour.setImageResource(R.drawable.page_indicator);
                    mbtnStart.setVisibility(View.GONE);
                    break;
                case 1:
                    mivIndicatorFirst.setImageResource(R.drawable.page_indicator);
                    mivIndicatorSecond.setImageResource(R.drawable.page_indicator_focused);
                    mivIndicatorThird.setImageResource(R.drawable.page_indicator);
                    mivIndicatorFour.setImageResource(R.drawable.page_indicator);
                    mbtnStart.setVisibility(View.GONE);
                    break;
                case 2:
                    mivIndicatorFirst.setImageResource(R.drawable.page_indicator);
                    mivIndicatorSecond.setImageResource(R.drawable.page_indicator);
                    mivIndicatorThird.setImageResource(R.drawable.page_indicator_focused);
                    mivIndicatorFour.setImageResource(R.drawable.page_indicator);
                    mbtnStart.setVisibility(View.GONE);
                    break;
                case 3:
                    mivIndicatorFirst.setImageResource(R.drawable.page_indicator);
                    mivIndicatorSecond.setImageResource(R.drawable.page_indicator);
                    mivIndicatorThird.setImageResource(R.drawable.page_indicator);
                    mivIndicatorFour.setImageResource(R.drawable.page_indicator_focused);
                    mbtnStart.setVisibility(View.VISIBLE);


            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
    @Override
    public void onBackPressed() {
        // Disable back.
    }



}
