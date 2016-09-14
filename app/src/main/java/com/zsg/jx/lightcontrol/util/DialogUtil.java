package com.zsg.jx.lightcontrol.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.view.ComReminderDialog;


public class DialogUtil {
    public static void showDialog(Context context, int titleid, int msgid,
                                  int leftbtnid, int rightbtnid, OnClickListener LeftOnClickListener,
                                  OnClickListener RightOnClickListener, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(false);
        builder.setTitle(titleid);
        builder.setMessage(msgid)
                .setNegativeButton(leftbtnid, LeftOnClickListener)
                .setPositiveButton(rightbtnid, RightOnClickListener).create()
                .show();
    }

    public static void showDialog(Context context, String title, String msg,
                                  String leftbtn, String rightbtn,
                                  OnClickListener LeftOnClickListener,
                                  OnClickListener RightOnClickListener, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(cancelable);
        builder.setTitle(title).setMessage(msg)
                .setNegativeButton(leftbtn, LeftOnClickListener)
                .setPositiveButton(rightbtn, RightOnClickListener).create()
                .show();
    }

    public static void showNoTitleDialog(Context context, int msgid,
                                         int leftbtnid, int rightbtnid, OnClickListener LeftOnClickListener,
                                         OnClickListener RightOnClickListener, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(cancelable);
        builder.setMessage(msgid)
                .setNegativeButton(leftbtnid, LeftOnClickListener)
                .setPositiveButton(rightbtnid, RightOnClickListener).create()
                .show();
    }

    public static void showNoTitleDialog(Context context, String msg,
                                         String leftbtn, String rightbtn,
                                         OnClickListener LeftOnClickListener,
                                         OnClickListener RightOnClickListener, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(cancelable);
        builder.setMessage(msg).setNegativeButton(leftbtn, LeftOnClickListener)
                .setPositiveButton(rightbtn, RightOnClickListener).create()
                .show();
    }

    /**
     * 得到自定义的progressDialog
     *
     * @param context
     * @param msg
     * @return
     */
    public static Dialog createLoadingDialog(Context context, String msg) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ProgressBar spaceshipImage = (ProgressBar) v.findViewById(R.id.loading);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        tipTextView.setText(msg);// 设置加载信息
        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog
        loadingDialog.setCancelable(false);// 不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));// 设置布局
        return loadingDialog;
    }

    public static AlertDialog getSelectDialog(Context context, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(cancelable);


        return builder.create();

    }

    /**
     * 网络断开提示对话框
     */
    public static void showComReminderDialog(final Context context) {
        final ComReminderDialog comReminderDialog = new ComReminderDialog(context,
                context.getResources().getString(R.string.net_has_breaked)
                , context.getResources().getString(R.string.cancel), context.getResources().getString(R.string.ensure));
        comReminderDialog.setCanceledOnTouchOutside(false);
        comReminderDialog.show();
        comReminderDialog.dialog_cancel.setOnClickListener(new View.OnClickListener() {
                                                               @Override
                                                               public void onClick(View v) {
                                                                   comReminderDialog.cancel();
                                                               }
                                                           }
        );
        comReminderDialog.dialog_submit.setOnClickListener(new View.OnClickListener() {
                                                               @Override
                                                               public void onClick(View v) {
                                                                   comReminderDialog.cancel();
                                                                   if (android.os.Build.VERSION.SDK_INT > 13) {
                                                                       context.startActivity(new Intent(
                                                                               android.provider.Settings.ACTION_SETTINGS));
                                                                   } else {
                                                                       context.startActivity(new Intent(
                                                                               android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                                                   }
                                                               }
                                                           }
        );
    }


}
