package com.example.q.xmppclient.activity;

import android.app.ProgressDialog;
import android.content.Context;

import com.example.q.xmppclient.manager.LoginConfig;

/**
 * Created by q on 2017/10/26.
 */

public interface IActivity  {

    /**
     *
     * 保存用户配置.
     * @param loginConfig 登录配置
     */
    public void saveLoginConfig(LoginConfig loginConfig);

    /**
     * 获取用户配置.
     */
    public LoginConfig getLoginConfig();

    /**
     *
     * 获取进度对话框.
     * @return ProgressDialog
     */
    public abstract ProgressDialog getProgressDialog();
    /**
     *
     * 获取上下文
     * @return Context
     */
    public abstract Context getContext();
    public abstract void startService();
}
