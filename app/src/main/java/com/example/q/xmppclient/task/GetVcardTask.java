package com.example.q.xmppclient.task;

import android.os.AsyncTask;
import android.util.Log;

import com.example.q.xmppclient.manager.XmppConnectionManager;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import static android.content.ContentValues.TAG;

/**
 * Created by q on 2017/11/4.
 */

public class GetVcardTask extends AsyncTask<String, Integer,Long> {
    String nickname;

    protected Long doInBackground(String... params) {
        if (params.length < 1) {
            return Long.valueOf(-1);
        }
        VCard vCard = new VCard();
        // 获取用户 params[0] 的 vcard 信息
        try {
        // load(Connection connection, String  user)

            vCard.load(XmppConnectionManager.getInstance().getConnection(), params[0]);
            Log.d(TAG, "nickname:  " + vCard.getNickName());
        } catch (XMPPException e) {
            e.printStackTrace();
            return Long.valueOf(-2);
        }

        return Long.valueOf(0);
    }
}