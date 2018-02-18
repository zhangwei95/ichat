package com.example.q.xmppclient.util;

import android.widget.TextView;

import org.jivesoftware.smack.util.StringUtils;


/**
 * Created by q on 2017/10/26.
 */

public class ValidataUtil {

    /**
     *  属性信息判断是否为空
     * @param view 文本框类对象
     * @param str 用户名，密码之类的不能字符串对象
     * @return
     */
    public static  boolean isEmpty(TextView view, String str)
    {
        if (view.getText().toString().trim().isEmpty()) {
            view.setError(str + "不能为空！");
            view.setFocusable(true);
            view.requestFocus();
            return true;
        }
        return false;
    }

}
