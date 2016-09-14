package com.zsg.jx.lightcontrol.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

import com.zsg.jx.lightcontrol.R;


public class ComReminderDialog extends Dialog {

    public TextView dialog_cancel;
    public TextView dialog_submit;
    public TextView dialog_content;

    public ComReminderDialog(Context context, String content, String cancel,
                             String submit) {
        this(context, R.style.CommonReminderDialog, content, cancel, submit);
    }

    public ComReminderDialog(Context context, int theme, String content,
                             String cancel, String submit) {
        super(context, theme);
        this.setContentView(R.layout.dialog_com_reminder);
        this.getWindow().getAttributes().gravity = Gravity.CENTER;
        dialog_cancel = (TextView) this.findViewById(R.id.dialog_cancel);
        dialog_submit = (TextView) this.findViewById(R.id.dialog_submit);
        dialog_content = (TextView) this.findViewById(R.id.dialog_content);
        dialog_cancel.setText(cancel);
        dialog_submit.setText(submit);
        dialog_content.setText(content);
    }


}
