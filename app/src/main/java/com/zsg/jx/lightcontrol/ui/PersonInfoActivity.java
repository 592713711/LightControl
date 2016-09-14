package com.zsg.jx.lightcontrol.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.zsg.jx.lightcontrol.R;
import com.zsg.jx.lightcontrol.app.MyApplication;
import com.zsg.jx.lightcontrol.bin.User;
import com.zsg.jx.lightcontrol.dao.UserDao;
import com.zsg.jx.lightcontrol.listener.EditTextFocusListener;
import com.zsg.jx.lightcontrol.listener.EditTextTouchListener;
import com.zsg.jx.lightcontrol.listener.PwdTextWatcher;

import com.zsg.jx.lightcontrol.util.BaseTools;
import com.zsg.jx.lightcontrol.util.Config;
import com.zsg.jx.lightcontrol.util.FileUtils;
import com.zsg.jx.lightcontrol.util.JsonUtil;
import com.zsg.jx.lightcontrol.util.MessageUtil;
import com.zsg.jx.lightcontrol.util.SimpleMessage;
import com.zsg.jx.lightcontrol.util.UpdateUserInfo;
import com.zsg.jx.lightcontrol.volley.RequestListener;
import com.zsg.jx.lightcontrol.volley.RequestManager;
import com.zsg.jx.lightcontrol.volley.RequestParams;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import de.hdodenhof.circleimageview.CircleImageView;
import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class PersonInfoActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "PersonInfoActivity";
    private static final int PHOTO_REQUEST_CUT = 3;
    private boolean D = true;
    private static final int REQUEST_IMAGE = 2;
    private static String imagePath;

    private Map<String, Object> params;
    private Map<String, File> files;
    private ImageView btn_back;
    private RelativeLayout rl_head;
    private RelativeLayout rl_nickname;
    private RelativeLayout rl_sex;
    private RelativeLayout rl_changepassword;
    private CircleImageView image_head;
    private TextView nickname;
    private TextView text_sex;
    private TextView tv_mobile;
    private TextView tv_count;
    private Button btn_quit;

    private String snickname;
    private String sex;

    private EditText et_oldPassword;
    private EditText et_newPassword;
    private EditText et_surePassword;

    private UserDao userDao;
    private User user;

    private String newpwd;
    private final static int INFO_REQ = 1;
    private final static int PWD_REQ = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);
        userDao = MyApplication.getInstance().getUserDao();
        user = userDao.queryById(MyApplication.getInstance().getPreferenceUtil().getUid());
        initView();
        initData();

        EventBus.getDefault().register(this);

    }

    private void initView() {
        btn_back = (ImageView) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);

        rl_head = (RelativeLayout) findViewById(R.id.rl_head);
        rl_head.setOnClickListener(this);

        rl_nickname = (RelativeLayout) findViewById(R.id.rl_nickname);
        rl_nickname.setOnClickListener(this);

        rl_sex = (RelativeLayout) findViewById(R.id.rl_sex);
        rl_sex.setOnClickListener(this);

        rl_changepassword = (RelativeLayout) findViewById(R.id.rl_changepasswprd);
        rl_changepassword.setOnClickListener(this);

        btn_quit= (Button) findViewById(R.id.btn_wriitenoff);
        btn_quit.setOnClickListener(this);

        tv_mobile= (TextView) findViewById(R.id.mobile);
        tv_count= (TextView) findViewById(R.id.count);

        image_head = (CircleImageView) findViewById(R.id.img_head);

        Log.d(TAG, "user:" + user.toString());
        if (user.getImage() == null || "".equals(user.getImage())) {
            image_head.setImageResource(R.drawable.set_icon);
        } else {
            Picasso.with(this).load(Config.SERVER + user.getImage_thumb())
                    .error(R.drawable.set_icon)
                    .into(image_head);
        }


        nickname = (TextView) findViewById(R.id.nickname);

        text_sex = (TextView) findViewById(R.id.sex);


    }

    public void initData() {
        snickname = user.getName();
        if (snickname == null || snickname.equals("")) {
            nickname.setText(R.string.nosetting_nickname);

        } else {
            nickname.setText(snickname);
        }

        sex = user.getSex();
        if (!sex.equals("")) {
            text_sex.setText(sex.equals("1") ? "男" : "女");
        } else {
            text_sex.setText(R.string.nosetting_sex);
        }

        tv_mobile.setText(user.getPhone());
        tv_count.setText(user.getPhone());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_head:
                MultiImageSelector selector = MultiImageSelector.create(PersonInfoActivity.this);
                selector.showCamera(true);
                selector.single();
                //selector.multi();
                selector.start(PersonInfoActivity.this, REQUEST_IMAGE);
                break;

            case R.id.btn_back:
                finish();
                break;

            case R.id.rl_nickname:
                Intent intent = new Intent(PersonInfoActivity.this, NickNameActivity.class);
                startActivity(intent);
                break;

            case R.id.rl_sex:
                initSexDialog(this);
                break;

            case R.id.rl_changepasswprd:
                initPasswordDialog(this);
                break;
            case R.id.btn_wriitenoff:
                //退出登录  删除用户数据
                MyApplication.getInstance().getUserDao().deleteAll();
                Intent intent2=new Intent(PersonInfoActivity.this,LoginActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                finish();
                break;

        }

    }

    /**
     * 初始化弹出对话框
     *
     * @param context
     */
    private void initSexDialog(final Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.sex_dialog, null);
        final Dialog dlg = new Dialog(context, R.style.common_dialog);
        //Window window = dlg.getWindow();
        dlg.setContentView(view);
        dlg.show();
        RadioButton ra_man = (RadioButton) view.findViewById(R.id.rb_man);
        RadioButton ra_woman = (RadioButton) view.findViewById(R.id.rb_woman);
        sex = user.getSex();
        if (sex.equals("1")) {
            ra_man.setChecked(true);
            ra_woman.setChecked(false);

        } else {
            ra_woman.setChecked(true);
            ra_man.setChecked(false);
        }
        RelativeLayout rl_man = (RelativeLayout) view.findViewById(R.id.rl_man);
        RelativeLayout rl_woman = (RelativeLayout) view.findViewById(R.id.rl_woman);
        View.OnClickListener lis = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.rl_man:
                        if (D) Log.d(TAG, "click rl_man");
                        user.setSex("1");
                        // text_sex.setText("男");
                        break;
                    case R.id.rl_woman:
                        if (D) Log.d(TAG, "click rl_woman");
                        user.setSex("2");
                        // text_sex.setText("女");
                        break;
                }
                showLoadingDialog();
                UpdateUserInfo.UpdateUserInfo(user, requestListener(INFO_REQ), PersonInfoActivity.this);
                dlg.dismiss();
            }
        };

        rl_man.setOnClickListener(lis);
        rl_woman.setOnClickListener(lis);
        // 设置相关位置，一定要在 show()之后
        //Window window2 = dlg.getWindow();
        //WindowManager.LayoutParams params = window2.getAttributes();
        //window.setAttributes(params);
    }

    /**
     * 设置修改密码对话框
     *
     * @param context
     */
    private void initPasswordDialog(final Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.pwd_dialog, null);
        //初始化视图
        et_oldPassword = (EditText) view.findViewById(R.id.et_oldPassword);
        et_newPassword = (EditText) view.findViewById(R.id.et_newPassword);
        et_surePassword = (EditText) view.findViewById(R.id.et_surePassword);
        //设置监听器
        //给密码输入框设置文本改变监听器
        et_oldPassword.addTextChangedListener(new PwdTextWatcher(context, et_oldPassword));
        et_newPassword.addTextChangedListener(new PwdTextWatcher(context, et_newPassword));
        et_surePassword.addTextChangedListener(new PwdTextWatcher(context, et_surePassword));
        //设置触摸监听器
        et_oldPassword.setOnTouchListener(new EditTextTouchListener(context, et_oldPassword));
        et_newPassword.setOnTouchListener(new EditTextTouchListener(context, et_newPassword));
        et_surePassword.setOnTouchListener(new EditTextTouchListener(context, et_surePassword));
        //设置焦点监听器
        et_oldPassword.setOnFocusChangeListener(new EditTextFocusListener(context, et_oldPassword));
        et_newPassword.setOnFocusChangeListener(new EditTextFocusListener(context, et_newPassword));
        et_surePassword.setOnFocusChangeListener(new EditTextFocusListener(context, et_surePassword));
        //创建对话框
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("修改密码");
        dialog.setView(view);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //修改密码
                //原密码
                String oldpassword = et_oldPassword.getText().toString();
                //新密码
                String passwordstr = et_newPassword.getText().toString();
                //确认新密码
                String repasswordstr = et_surePassword.getText().toString();

                if (oldpassword.length() < 6 || oldpassword.length() > 16) {
                    showLongToast(getString(R.string.pwd_length_not_right));
                    return;
                } else if (passwordstr.length() < 6 || passwordstr.length() > 16) {
                    showShortToast(getString(R.string.pwd_length_not_right));
                    return;
                }
                if (!passwordstr.equals(repasswordstr)) {
                    showLongToast(getString(R.string.pwd_ensurepwd_not_match));
                    return;
                }
                newpwd=passwordstr;
                showLoadingDialog();
                requestAlterPassWord(oldpassword,passwordstr);

            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        dialog.create().show();
    }


    /**
     * 照片路径返回的路径
     *
     * @param requestCode
     * @param resultCode
     * @param data        数据（应该是图片的路径）
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE:
                    //获取图片返回的路径
                    List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                    //图片路径
                    imagePath = path.get(0);
                    if (D)
                        Log.d(TAG, "get picture path" + path.get(0) + "get picture size" + path.get(0));

                    startPhotoZoom(
                            Uri.fromFile(new File(imagePath)));
                    break;
                case PHOTO_REQUEST_CUT:
                    if (D) Log.d(TAG, "pic cut result ");
                    if (data != null) {
                        //保存裁剪的路径
                        setPicToView(data);
                        sendRequestLoadImage();
                    } else {
                        Toast.makeText(getApplicationContext(), "照相失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }


    /**
     * 得到本地图片的Bitmap
     *
     * @param url 图片路径
     * @return
     */
    private Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void startPhotoZoom(Uri uri1) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri1, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);

        intent.putExtra("return-data", true);// true:不返回uri，false：返回uri

        startActivityForResult(intent, PHOTO_REQUEST_CUT);

    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            // 取得SDCard图片路径做显示
            Bitmap photo = extras.getParcelable("data");
            Log.d(TAG, "set View");
            //保存到本地的路径，上传时候使用
            String urlPath = FileUtils.saveBitmap(photo, "photo");
            // Drawable drawable = new BitmapDrawable(null, photo);
            // image_head.setImageDrawable(drawable);
        }
    }

    /**
     * 发送上传头像请求
     */
    private void sendRequestLoadImage() {
        showLoadingDialog(getString(R.string.loadhead));
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Map<String, String> params = new HashMap<String, String>();
                params.put(Config.USER_ID, user.getId());
                params.put(Config.IMAGE, Environment.getExternalStorageDirectory()
                        + "/ble_anti_lost/photo.jpg");
                String str = "";
                try {
                    str = RequestManager.postForm(Config.URL_UPLOADUSERIMAGE, params);
                    Log.e("hjq", str);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.e("hjq", e.getMessage());
                }

                SimpleMessage msg = new SimpleMessage(MessageUtil.LOADHEAD_RSP, str);
                EventBus.getDefault().post(msg);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 上传头像消息回调
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showLoadHead(SimpleMessage msg) {
        if (msg.getTag() == MessageUtil.LOADHEAD_RSP) {
            closeLoadingDialog();
            try {
                JSONObject json = new JSONObject(msg.json);
                if (json.getInt(Config.CODE) == 1) {
                    user = userDao.queryById(MyApplication.getInstance().getPreferenceUtil().getUid());
                    if (user != null) {
                        user.setImage(json.getString(Config.IMAGE));
                        user.setImage_thumb(json.getString(Config.IMAGE_THUMB));
                        userDao.deleteById(user.getId());
                        userDao.insert(user);
                        Log.e("hjq", "new muser = " + user);
                        Picasso.with(this).load(Config.SERVER + user.getImage_thumb())
                                .error(R.drawable.set_icon)
                                .into(image_head);
                        // ImageLoaderUtil.displayImage(HttpUtil.SERVER + mUser.getImage_thumb(), image_head, PersonInfoActivity.this);
                    } else {

                    }
                } else {
                    showShortToast(json.getString(Config.MSG));
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = userDao.queryById(MyApplication.getInstance().getPreferenceUtil().getUid());
        initData();
    }

    private RequestListener requestListener(int type) {
        if (type == INFO_REQ)
            return new RequestListener() {
                @Override
                public void requestSuccess(String result) {
                    closeLoadingDialog();
                    try {
                        JSONObject json = new JSONObject(result);
                        if (json.getInt(Config.CODE) == 1) {
                            userDao.update(user);
                            showShortToast(getString(R.string.updateinfo_success));
                        } else {
                            showLongToast(json.getString(Config.MSG));
                            user = userDao.queryById(user.getId());
                        }
                        initData();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                @Override
                public void requestError(VolleyError e) {
                    closeLoadingDialog();
                    if (e.networkResponse == null) {
                        showShortToast(getString(R.string.network_error));
                    }
                }
            };

        if(type==PWD_REQ)
            return new RequestListener() {
                @Override
                public void requestSuccess(String result) {
                    closeLoadingDialog();
                    try {
                        JSONObject json = new JSONObject(result);
                        if ("ok".equals(JsonUtil.getStr(json, Config.STATUS))) {
                            showShortToast(getString(R.string.person_password_update_ok));
                            new UserDao(PersonInfoActivity.this).updatePassWordById(newpwd, user.getId());
                        } else {
                            BaseTools.showToastByLanguage(PersonInfoActivity.this, json);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void requestError(VolleyError e) {
                    closeLoadingDialog();
                    if (e.networkResponse == null) {
                        showShortToast(getString(R.string.network_error));
                    }
                }
            };
        return null;
    }


    /**
     * 修改密码请求
     */
    private void requestAlterPassWord(String oldpass, String newpass) {
        RequestParams params = new RequestParams();
        params.put("oldpass", oldpass);
        params.put("newpass", newpass);

        RequestManager.post(Config.URL_RESETPASSWORD, PersonInfoActivity.this, params, requestListener(PWD_REQ));
    }
}
