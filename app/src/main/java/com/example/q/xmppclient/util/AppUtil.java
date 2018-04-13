package com.example.q.xmppclient.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Environment;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.manager.LoginConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by q on 2018/1/10.
 */

public class AppUtil {
    private static Boolean isDebug=null;
    //是否是debug模式
    public  static boolean isDebug() {
        return isDebug==null?false:isDebug.booleanValue();
    }

    public  static  void syncIsDebug(Context context) {
        if(isDebug==null) {
            isDebug = context.getApplicationInfo() != null &&
                    (context.getApplicationInfo().flags &
                            ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        }

    }
    /**
     * 获取本地默认头像,没有则缓存到本地
     */
    public static byte[] getDefaultAvatar(Context context, LoginConfig loginConfig)
    {
        //默认image路径
        String imageDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath() +
                context.getResources().getString(R.string.img_dir)+"/";
        File dirfile = new File(imageDir);
        //获取内部存储状态
        String state = Environment.getExternalStorageState();
        //如果状态不是mounted，无法读写
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        //在本地缓存图片
        String fileName = "avatar_" +StringUtil.getJidByName
                (loginConfig.getUsername().toLowerCase(),loginConfig.getServerName())+".png";
        byte[] bytes=null;
        try {
            if (!dirfile.exists()) {
                dirfile.mkdirs();
            }
            File file = new File(imageDir, fileName);
            if (file.exists())
            {
                try {
                    FileInputStream in = new FileInputStream(file);
                    bytes = new byte[(int) file.length()];
                    in.read(bytes);
                    in.close();
                }catch (IOException e)
                {
                    e.printStackTrace();
                }
            }else {
                Bitmap mBitmap= null ;
                mBitmap = FormatUtil.drawable2Bitmap(context.getResources()
                        .getDrawable(R.drawable.default_icon));
                bytes=FormatUtil.Bitmap2Bytes(mBitmap);
                FileOutputStream out = new FileOutputStream(file);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    /**
     * 头像缓存到本地
     * @param context
     * @param bitmap 头像bitmap
     * @param jid 头像所属user的jid
     * @return
     */
    public static byte[] cachedAvatarImage(Context context,Bitmap bitmap,String jid)
    {
        //默认image路径
        String imageDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath() +
                context.getResources().getString(R.string.img_dir)+"/";
        File dirfile = new File(imageDir);
        //获取内部存储状态
        String state = Environment.getExternalStorageState();
        //如果状态不是mounted，无法读写
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        //在本地缓存图片
        String fileName = "avatar_" +jid.toLowerCase()+".png";
        byte[] bytes=null;
        try {
            if (!dirfile.exists()) {
                dirfile.mkdirs();
            }

            File file = new File(imageDir, fileName);
            if(!file.exists()) {
                Bitmap mBitmap = bitmap;
                bytes = FormatUtil.Bitmap2Bytes(mBitmap);
                FileOutputStream out = new FileOutputStream(file);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bytes;
    }
}
