package com.example.q.xmppclient.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.manager.AppManager;
import com.example.q.xmppclient.manager.LoginConfig;
import com.example.q.xmppclient.service.ChatService;
import com.example.q.xmppclient.service.ContactService;
import com.example.q.xmppclient.service.ReconnectService;
import com.example.q.xmppclient.util.CrashHandler;

/**
 * Created by q on 2017/10/26.
 */

public class ActivityTool extends AppCompatActivity implements IActivity {


    protected ProgressDialog progressDialog;
    protected Context context;
    protected AppManager appManager;
    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor Editer;
    protected NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        context=this;
        sharedPreferences=getSharedPreferences(Constant.LOGIN_SET,MODE_PRIVATE);
        Editer=sharedPreferences.edit();
        notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        progressDialog=new ProgressDialog(context);
        appManager =(AppManager) getApplication();
        appManager.addActivity(this);
    }
    public AppManager getAppManager() {
        return appManager;
    }

    @Override
    public Context getContext() {
        return context;
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void startService()
    {
        // 聊天服务
        Intent chatService = new Intent(context, ChatService.class);
        context.startService(chatService);
        // 自动恢复连接服务
        Intent reConnectService = new Intent(context, ReconnectService.class);
        context.startService(reConnectService);

        Intent contactService=new Intent(context,ContactService.class);
        context.startService(contactService);
        //todo 启动服务
    }
    public void  stopService()
    {
        //todo 销毁服务
        Intent chatServer = new Intent(context, ChatService.class);
        context.stopService(chatServer);

        // 自动恢复连接服务
        Intent reConnectService = new Intent(context, ReconnectService.class);
        context.stopService(reConnectService);

        Intent contactService=new Intent(context,ContactService.class);
        context.stopService(contactService);
    }

    /**
     * 保存LoginConfig
     * @param loginConfig 登录配置
     */

    @Override
    public void saveLoginConfig(LoginConfig loginConfig) {
        Editer.putString(Constant.XMPP_HOST,loginConfig.getXmppIP());
        Editer.putInt(Constant.XMPP_PORT,loginConfig.getXmppPort());
        Editer.putString(Constant.USERNAME,loginConfig.getUsername());
        Editer.putString(Constant.PASSWORD,loginConfig.getPassword());
        Editer.putString(Constant.XMPP_SEIVICE_NAME,loginConfig.getServerName());
        Editer.putBoolean(Constant.IS_AUTOLOGIN,loginConfig.isAutoLogin());
        Editer.putBoolean(Constant.IS_REMEMBER,loginConfig.isRemember());
        Editer.putBoolean(Constant.IS_FIRSTSTART,loginConfig.isFirstStart());
        Editer.putBoolean(Constant.IS_ONLINE,loginConfig.isOnline());
        Editer.putBoolean(Constant.IS_NEW_USER,loginConfig.isNewUser());
        Editer.apply();
    }

    /**
     * 获取LoginConfig
     * @return loginConfig
     */
    @Override
    public LoginConfig getLoginConfig() {
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.setXmppIP( sharedPreferences.getString(Constant.XMPP_HOST,getResources().getString(R.string.xmpp_host)));
        loginConfig.setXmppPort(sharedPreferences.getInt(Constant.XMPP_PORT,getResources().getInteger(R.integer.xmpp_port)));
        loginConfig.setUsername(sharedPreferences.getString(Constant.USERNAME,null));
        loginConfig.setPassword(sharedPreferences.getString(Constant.PASSWORD,null));
        loginConfig.setServerName(sharedPreferences.getString(Constant.XMPP_SEIVICE_NAME,getResources().getString(R.string.xmpp_service_name)));
        loginConfig.setAutoLogin(sharedPreferences.getBoolean(Constant.IS_AUTOLOGIN,
                getResources().getBoolean(R.bool.is_autologin)));
        loginConfig.setRemember(sharedPreferences.getBoolean(Constant.IS_REMEMBER,
                getResources().getBoolean(R.bool.is_remember)));
        loginConfig.setFirstStart(sharedPreferences.getBoolean(
                Constant.IS_FIRSTSTART, true));
        loginConfig.setNewUser(sharedPreferences.getBoolean(
                Constant.IS_NEW_USER, false));
        return loginConfig;
    }
    /**
     * 退出程序提示框
     */
    public void isExit()
    {
        new AlertDialog.Builder(context).setTitle("退出程序").setMessage("确定退出吗?")
                .setNeutralButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopService();
                        appManager.exit();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    /**
     *  显示Toast
     * @param text 显示的文字
     * @param longint   Toast.Length
     */
    public void showToast(String text, int longint) {
        Toast.makeText(context, text, longint).show();
    }

    /**
     * 显示Toast
     * @param text  显示的文字
     */
    public void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取LoginConfig
     * @return LoginConfig
     */

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    /**
     *
     * 关闭键盘事件
     *
     * @author shimiso
     * @update 2012-7-4 下午2:34:34
     */
    public void closeInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && this.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
