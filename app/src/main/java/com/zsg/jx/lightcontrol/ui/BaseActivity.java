package com.zsg.jx.lightcontrol.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.util.DialogUtil;
import com.zsg.jx.lightcontrol.view.ComReminderDialog;

import java.util.List;

public class BaseActivity extends Activity implements View.OnClickListener {
    private Dialog dialog;
    private ComReminderDialog comReminderDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        MyApplication.getInstance().removeActivity(this);
        super.onDestroy();
    }

    public void showLongToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    public void showShortToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void showLoadingDialog() {
        dialog = DialogUtil.createLoadingDialog(this, getResources().getString(R.string.data_loading));
        dialog.setCancelable(true);
        dialog.show();
    }

    public void showLoadingDialog(String msg) {
        dialog = DialogUtil.createLoadingDialog(this, msg);
        dialog.setCancelable(true);
        dialog.show();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                onDialogCancel();
            }
        });
    }

    public void showLoadingDialog(String msg,boolean isCancelable) {
        dialog = DialogUtil.createLoadingDialog(this, msg);
        dialog.setCancelable(isCancelable);
        dialog.show();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                onDialogCancel();
            }
        });
    }

    protected void onDialogCancel() {
        Log.e("hjq", "onDialogCancel called");
    }

    public boolean closeLoadingDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            return true;
        } else {
            return false;
        }
    }


    String getTopActivity() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //4.4 崩溃
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            return (runningTaskInfos.get(0).topActivity).toString();
        } else {
            return null;
        }
    }

    /**
     * 关闭网络断开提示对话框
     *
     * @return
     */
    public void closeComReminderDialog() {
        if (comReminderDialog != null && comReminderDialog.isShowing()) {
            comReminderDialog.dismiss();
        }
    }

    /**
     * 网络断开提示对话框
     */
    public void showComReminderDialog() {
        closeComReminderDialog();
        comReminderDialog = new ComReminderDialog(this,
                getResources().getString(R.string.net_has_breaked)
                , getResources().getString(R.string.cancel), getResources().getString(R.string.ensure));
        comReminderDialog.setCanceledOnTouchOutside(false);
        comReminderDialog.show();
        comReminderDialog.dialog_cancel.setOnClickListener(new View.OnClickListener()
           {
               @Override
               public void onClick(View v) {
                   comReminderDialog.cancel();
               }
           }
        );
        comReminderDialog.dialog_submit.setOnClickListener(new View.OnClickListener()
           {
               @Override
               public void onClick(View v) {
                   comReminderDialog.cancel();
                   if (android.os.Build.VERSION.SDK_INT > 13) {
                       startActivity(new Intent(
                               android.provider.Settings.ACTION_SETTINGS));
                   } else {
                       startActivity(new Intent(
                               android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                   }
               }
           }
        );
    }


    @Override
    public void onClick(View view) {

    }
}
