package com.zsg.jx.lightcontrol.util;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 作者：GONGXI on 2016/8/17 13:54
 * 邮箱：gongxi@uascent.com
 */
public class FileUtils {
    public static String SDPATH = Environment.getExternalStorageDirectory() + "/ble_anti_lost/";

    public static String saveBitmap(Bitmap bm, String picName) {
        String croppath = "";
        try {
            if (!isFileExist("")) {
                File tempf = createSDDir("");
            }
            File f = new File(SDPATH, picName + ".jpg");
            //得到裁剪相机的图片存到本地图片
            croppath = f.getPath();
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return croppath;
    }


    /**
     * 判断目录是否存在
     *
     * @param filename
     * @return
     */
    public static boolean isFileExist(String filename) {
        File file = new File(SDPATH + filename);
        file.isFile();
        return file.exists();
    }

    /**
     * 创建根目录
     *
     * @param dirName
     * @return
     */
    public static File createSDDir(String dirName) throws IOException {
        File dir = new File(SDPATH + dirName);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            System.out.println("createSDDir:" + dir.getAbsolutePath());
            System.out.println("createSDDir" + dir.mkdir());
        }
        return dir;
    }
}
