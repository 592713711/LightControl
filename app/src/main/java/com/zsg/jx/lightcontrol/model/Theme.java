package com.zsg.jx.lightcontrol.model;

/**
 * Created by zsg on 2016/8/24.
 */
public class Theme {
    public int themeId;
    public String theme_name;
    public LightList list;


    public int getThemeId() {
        return themeId;
    }

    public LightList getList() {
        return list;
    }

    public String getTheme_name() {
        return theme_name;
    }

    public void setThemeId(int themeId) {
        this.themeId = themeId;
    }

    public void setTheme_name(String theme_name) {
        this.theme_name = theme_name;
    }

    public void setList(LightList list) {
        this.list = list;
    }
}
