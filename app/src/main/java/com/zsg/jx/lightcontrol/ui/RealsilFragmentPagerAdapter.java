package com.zsg.jx.lightcontrol.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * 作者：GONGXI on 2016/8/12 11:41
 * 邮箱：gongxi@uascent.com
 */
public class RealsilFragmentPagerAdapter extends FragmentPagerAdapter {
    ArrayList<Fragment> list;
    FragmentManager fm;
    public RealsilFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> list){
        super(fm);
        this.fm=fm;
        this.list=list;
    }
    public void clear() {
        for(int i = 0; i < list.size(); i ++) {
            fm.beginTransaction().remove(list.get(i)).commit();
        }

        list.clear();
        notifyDataSetChanged();
    }
    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
