package com.example.q.xmppclient.task;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.activity.ActivityBase;
import com.example.q.xmppclient.activity.LoginActivity;
import com.example.q.xmppclient.manager.LoginConfig;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.util.FormatUtil;
import com.example.q.xmppclient.util.StringUtil;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

/**
 * Created by q on 2017/11/8.
 */

public class SetVcardTask extends AsyncTask<Uri, Integer, Boolean> {
    LoginConfig loginConfig;
    ActivityBase activityBase;
    AlertDialog dialog;
    public SetVcardTask(ActivityBase activityBase,LoginConfig loginConfig)
    {
        this.loginConfig=loginConfig;
        this.activityBase=activityBase;
        dialog=new SpotsDialog(activityBase.getContext());
    }
    @Override
    protected void onPreExecute() {
        dialog.setTitle("上传中……");
        dialog.show();
        super.onPreExecute();
    }
    @Override
    protected Boolean doInBackground(Uri... params) {
        VCard vCard = new VCard();
        if (params.length < 1) {
            return false;
        }
            try {
                vCard.load(XmppConnectionManager.getInstance().getConnection());
            }catch (XMPPException e)
            {
                return false;
            }

        // 设置和更新用户信息
        vCard.setAddressFieldHome("country",loginConfig.getCountry());
        vCard.setAddressFieldHome("province",loginConfig.getProvince());
        vCard.setAddressFieldHome("city",loginConfig.getCity());
        vCard.setAddressFieldHome("sign",loginConfig.getSign());
        vCard.setAvatar(getDefaultAvatar());

        try {
            vCard.save(XmppConnectionManager.getInstance().getConnection());
            activityBase.saveLoginConfig(loginConfig);
        } catch (XMPPException e) {
            return false;
        }

        return true;

    }

    @Override
    protected void onPostExecute(Boolean bool) {
        super.onPostExecute(bool);
        if (bool) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {

                }

            }, 500);
            dialog.setTitle("上传成功~");
            dialog.dismiss();
        }else
        {
            dialog.setTitle("上传失败~");
            dialog.dismiss();
        }
    }

    /**
     * 获取本地头像,没有则缓存到本地
     */
    private byte[] getDefaultAvatar() {
        //默认image路径
        String imageDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath() +
                activityBase.getContext().getResources().getString(R.string.img_dir) + "/";
        File dirfile = new File(imageDir);
        //获取内部存储状态
        String state = Environment.getExternalStorageState();
        //如果状态不是mounted，无法读写
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        //本地读文件
        String fileName = "avatar_" + StringUtil.getJidByName
                (loginConfig.getUsername(), loginConfig.getServerName()) + ".png";
        byte[] bytes = null;
        try {
            if (!dirfile.exists()) {
                dirfile.mkdirs();
            }
            File file = new File(imageDir, fileName);
            try {
                FileInputStream in = new FileInputStream(file);
                bytes = new byte[(int) file.length()];
                in.read(bytes);
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bytes;
    }
}