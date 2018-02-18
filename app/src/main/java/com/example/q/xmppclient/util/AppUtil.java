package com.example.q.xmppclient.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;

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
}
