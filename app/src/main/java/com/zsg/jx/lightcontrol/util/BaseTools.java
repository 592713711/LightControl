package com.zsg.jx.lightcontrol.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by zsg on 2016/8/17.
 */
public class BaseTools {
    private static final String TAG = "BaseTools";

    /**
     * 更新用户基本信息之后，发生监听
     */
    public interface OnEditUserInfoListener {
        void onEditUserInfo();
    }

    public void setEditUserInfoListener(OnEditUserInfoListener listener) {
        onEditUserInfoListener = listener;
    }

    public static OnEditUserInfoListener onEditUserInfoListener;


    public final static int getWindowsWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(dm);
        return dm.widthPixels;
    }

    public final static int getWindowsHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(dm);
        return dm.heightPixels;
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    public static float sp2px(Resources resources, float sp) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }


    public static boolean stringIsEmpt(String str) {
        if (null == str || "".equals(str)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 将用户的基本信息从Share中清空
     */
    public static void clearShared(Context context) {
        SharedPreferences userInfo = context
                .getSharedPreferences("userInfo", 0);
        if (userInfo != null) {
            SharedPreferences.Editor editor = userInfo.edit();
            editor.clear();
            editor.commit();
        }
    }

    public static String numberToWan(String number) {
        if (number == null || number.equals(""))
            return "0";
        if (number.length() < 5)
            return number;
        else {
            double n = (double) Double.parseDouble(number) / 10000;
            double dou = Double
                    .parseDouble(new DecimalFormat("0.0 ").format(n));
            return dou + "万";
        }
    }

    public static String millisToString(int millis) {
        boolean negative = millis < 0;
        millis = Math.abs(millis);

        millis /= 1000;
        int sec = millis % 60;
        millis /= 60;
        int min = millis % 60;
        millis /= 60;
        int hours = millis;

        String time;
        DecimalFormat format = (DecimalFormat) NumberFormat
                .getInstance(Locale.US);
        format.applyPattern("00");
        time = format.format(hours) + ":" + format.format(min) + ":"
                + format.format(sec);
        time = negative ? "-" : "" + time;
        return time;
    }

    public static String millisToStringOfDay(int millis) {
        millis = Math.abs(millis);

        millis /= 1000;
        millis /= 60;
        int min = millis % 60;
        millis /= 60;
        int hours = millis % 24;
        millis /= 24;
        int days = millis;

        String day = (String) (days > 0 ? days + "天" : "");
        String hour = (String) (hours > 0 ? hours + "小时" : "");
        String mi = (String) (min > 0 ? min + "分钟" : "");

        return day + hour + mi;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getSysDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new java.util.Date());

    }

    /**
     * name是否符合规范
     *
     * @param name
     * @return
     */
    public static boolean isMemberChainLetter(String name) {
        String regEx = "^[a-zA-Z0-9\u4e00-\u9fa5]+$";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(name);
        return !m.matches();
    }

    /**
     * 是否是身份证号码
     *
     * @param
     * @return
     */
    public static boolean isID(String id) {
        if (id.trim().length() == 15) {
            return Pattern.matches("\\d{14}[0-9a-zA-Z]", id);
        } else if (id.trim().length() == 18) {
            return Pattern.matches("\\d{17}[0-9a-zA-Z]", id);
        }
        return false;
    }

    /**
     * 是否为电话号码
     *
     * @param phone
     * @return
     */
    public static boolean isPhoneNumber(String phone) {
        Pattern p = Pattern
                .compile("^((13[0-9])|(14[57])|(15[^4,\\D])|(18[0-9])|(17[0-9]))\\d{8}$");
        Matcher m = p.matcher(phone);
        return m.matches();
    }

    /**
     * 是否为mail
     *
     * @param mail
     * @return
     */
    public static boolean isMail(String mail) {
        Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}");
        Matcher m = p.matcher(mail);
        return m.matches();
    }


    // 过滤特殊字符
    public static String stringFilter(String str) throws PatternSyntaxException {
        // 清除掉所有特殊字符
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    /**
     * 判断文件夹是否存在
     */
    public static boolean isFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    /**
     * 创建目录
     *
     * @param dirName
     */
    public static File creatDir(String dirName) {
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    public static int getVerCode(Context context) {
        int verCode = -1;
        try {
            verCode = context.getPackageManager().getPackageInfo(
                    "com.tv.education.mobile", 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verCode;
    }

    public static boolean delDateFile(String path) {
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    // 删除指定文件夹下所有文件
    // param path 文件夹完整绝对路径
    public static boolean delAllDateFile(String path) {
        boolean flag = false;
        String allPath = path;
        File file = new File(allPath);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            file.delete();
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (allPath.endsWith(File.separator)) {
                temp = new File(allPath + tempList[i]);
            } else {
                temp = new File(allPath + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllDateFile(allPath + "/" + tempList[i]);// 先删除文件夹里面的文件
                flag = true;
            }
        }
        return flag;
    }

    public static String formatLong(long l) {
        double s = ((double) (l) / 1024);
        DecimalFormat dfL = new DecimalFormat("###0.00");
        return dfL.format(s);
    }

    /**
     * Convert a translucent themed Activity
     * {@link android.R.attr#windowIsTranslucent} to a fullscreen opaque
     * Activity.
     * <p/>
     * Call this whenever the background of a translucent Activity has changed
     * to become opaque. Doing so will allow the {@link android.view.Surface} of
     * the Activity behind to be released.
     * <p/>
     * This call has no effect on non-translucent activities or on activities
     * with the {@link android.R.attr#windowIsFloating} attribute.
     */
    public static void convertActivityFromTranslucent(Activity activity) {
        try {
            Method method = Activity.class
                    .getDeclaredMethod("convertFromTranslucent");
            method.setAccessible(true);
            method.invoke(activity);
        } catch (Throwable t) {
        }
    }

    /**
     * Convert a translucent themed Activity
     * {@link android.R.attr#windowIsTranslucent} back from opaque to
     * translucent following a call to
     * {@link #convertActivityFromTranslucent(Activity)} .
     * <p/>
     * Calling this allows the Activity behind this one to be seen again. Once
     * all such Activities have been redrawn
     * <p/>
     * This call has no effect on non-translucent activities or on activities
     * with the {@link android.R.attr#windowIsFloating} attribute.
     */
    @SuppressWarnings("rawtypes")
    public static void convertActivityToTranslucent(Activity activity) {
        try {
            Class<?>[] classes = Activity.class.getDeclaredClasses();
            Class<?> translucentConversionListenerClazz = null;
            for (Class clazz : classes) {
                if (clazz.getSimpleName().contains(
                        "TranslucentConversionListener")) {
                    translucentConversionListenerClazz = clazz;
                }
            }
            Method method = Activity.class.getDeclaredMethod(
                    "convertToTranslucent", translucentConversionListenerClazz);
            method.setAccessible(true);
            method.invoke(activity, new Object[]{null});
        } catch (Throwable t) {
        }
    }

    private static long lastClickTime;

    public static boolean isFastDoubleClick(long timeLimit) {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < timeLimit) { // 1000毫秒内按钮无效，这样可以控制快速点击，自己调整频率
            return false;
        }
        lastClickTime = time;
        return true;
    }

    /**
     * 是否符合电话号码的登录密码的规范
     *
     * @param str
     * @return
     */
    public static boolean isTelephonePwd(String str) {
        if (str.length() >= 6 && str.length() <= 15) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 通过字节获取字符串的子字符串
     *
     * @param str
     * @param end
     * @return
     */
    public static String subStringByBytes(String str, int end) {
        if (str.length() * 2 <= end) {
            return str;
        }
        char[] chArr = str.toCharArray();
        int lenByte = 0;
        for (int i = 0; i < chArr.length; i++) {
            if (chArr[i] > 127 || chArr[i] < 0) {
                lenByte += 2;
            } else {
                lenByte = lenByte + 1;
            }
            if (lenByte >= end) {
                if (lenByte == end) {
                    if (i != chArr.length - 1) {
                        return str.substring(0, i + 1) + "...";
                    }
                    return str.substring(0, i + 1);
                } else {
                    return str.substring(0, i) + "...";
                }
            }
        }
        return str;
    }

    /**
     * 根据语言来提示错误信息
     *
     * @param json
     */
    public static void showToastByLanguage(Context context, JSONObject json) {
        Locale locale = context.getResources().getConfiguration().locale;
        String str = "";
        if (locale.getLanguage().endsWith("zh")) {

            str = JsonUtil. getStr(json, Config.MSG);
            if (str != null && str.trim().length() != 0) {
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context,JsonUtil.  getStr(json, Config.ERROR), Toast.LENGTH_SHORT).show();
            }
        } else {
            str =JsonUtil. getStr(json, Config.ERROR);
            if (str != null && str.trim().length() != 0) {
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, JsonUtil. getStr(json, Config.MSG), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
