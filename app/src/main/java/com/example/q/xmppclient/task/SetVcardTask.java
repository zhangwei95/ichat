package com.example.q.xmppclient.task;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.example.q.xmppclient.manager.XmppConnectionManager;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import static android.content.ContentValues.TAG;

/**
 * Created by q on 2017/11/8.
 */

public class SetVcardTask extends AsyncTask<Uri, Integer, Long> {
    @Override
    protected Long doInBackground(Uri... params) {
//        if (params.length < 1) {
//            return Long.valueOf(-1);
//        }
//        Uri uriFile = params[0]; // 需要传输的头像文件
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        FileInputStream fis;
//        try {
//            String[] proj = {MediaStore.Images.Media.DATA};
//            Cursor actualimagecursor = getContentResolver().query(uriFile, proj, null, null, null);
//            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            actualimagecursor.moveToFirst();
//            String filePath = actualimagecursor.getString(actual_image_column_index);
//            fis = new FileInputStream(new File(filePath));
//            byte[] buf = new byte[1024];
//            int n;
//            while (-1 != (n = fis.read(buf))) {
//                baos.write(buf, 0, n);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        byte[] bbytes = baos.toByteArray();
//
//// 设置和更新用户信息
//        VCard vCard = new VCard();
//        vCard.setNickName("安静的疯子");
//        vCard.setAvatar(bbytes);
//        try {
//            vCard.save(XmppConnectionManager.getInstance().getConnection());
//        } catch (XMPPException e) {
//            e.printStackTrace();
//        }
//
//        return Long.valueOf(0);
        return null;
    }
}