package com.example.q.xmppclient.manager;

import android.app.Activity;
import android.app.Application;

import com.mob.MobSDK;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by q on 2017/10/26.
 */

public class AppManager extends Application {
    private List<Activity> activityList = new LinkedList<Activity>();

    @Override
    public void onCreate() {
        MobSDK.init(this,"23b1270e6bd38","40e876da22dd51d9f0b41fb1ab78c661");
        super.onCreate();
    }

    // 添加Activity到容器中
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    // 遍历所有Activity并finish
    public void exit() {
        XmppConnectionManager.getInstance().disconnect();
        for (Activity activity : activityList) {
            activity.finish();
        }
    }
}
