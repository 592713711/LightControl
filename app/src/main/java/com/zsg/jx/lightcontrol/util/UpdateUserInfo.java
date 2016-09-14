package com.zsg.jx.lightcontrol.util;

import android.content.Context;

import com.android.volley.VolleyError;
import com.zsg.jx.lightcontrol.bin.User;
import com.zsg.jx.lightcontrol.volley.RequestListener;
import com.zsg.jx.lightcontrol.volley.RequestManager;
import com.zsg.jx.lightcontrol.volley.RequestParams;

/**
 * 上传用户信息
 * Created by zsg on 2016/8/18.
 */
public class UpdateUserInfo {
    /**
     * 上传更新的用户信息
     * @param user
     */
    public static void UpdateUserInfo(final User user, RequestListener listener, Context context) {
        RequestParams params = new RequestParams();
        params.put(Config.NAME, user.getName());
        params.put(Config.SEX, user.getSex());
        params.put(Config.USER_ID, user.getId());
        RequestManager.post(Config.URL_EDITPROFILE, context, params, listener);
    }


}
