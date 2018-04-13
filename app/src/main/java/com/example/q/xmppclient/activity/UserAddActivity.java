package com.example.q.xmppclient.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.manager.LoginConfig;
import com.example.q.xmppclient.util.StringUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UserAddActivity extends ActivityBase {
    Toolbar toolbar;
    EditText et_search;
    LoginConfig loginConfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_add);
        loginConfig=getLoginConfig();
        initActionBar();
        init();
    }
    public void initActionBar()
    {
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("新的朋友");
        toolbar.setOnCreateContextMenuListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    public void  init()
    {
        et_search=(EditText)findViewById(R.id.et_search);
        et_search.setFocusable(true);

        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==event.KEYCODE_ENTER)
                {
                    dosearchFriend(et_search.getText().toString());
                    return true;
                }
                return false;
            }
        });

    }
    public void showSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(et_search, InputMethodManager.SHOW_IMPLICIT);
    }
    public void dosearchFriend(String username)
    {
        final String jid=StringUtil.getJidByName(username,getLoginConfig().getServerName());
        final Handler handler=new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(UserAddActivity.this,"用户名不存在，请确认",Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(UserAddActivity.this,"你找的好友就是你自己哦！！",Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(UserAddActivity.this,"网络异常，请确认连接！",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Intent intent=new Intent(UserAddActivity.this,UserInfoActivity.class);
                    intent.putExtra("to",jid);
                    startActivity(intent);
                    break;
            }
            super.handleMessage(msg);
        }
    };
        Thread userisExsit=new Thread(new Runnable() {
            @Override
            public void run() {
                if (StringUtil.getJidByName(loginConfig.getUsername(),getResources().getString(R.string.xmpp_service_name)).equals(jid))
                {
                    Message message=new Message();
                    message.what=3;
                    handler.sendMessage(message);
                }else {
                    int key = IsUserOnLine(jid, getLoginConfig().getXmppIP());
                    Message message = new Message();
                    message.what = key;
                    handler.sendMessage(message);
                }
            }
        });
        userisExsit.start();
//        if(IsUserOnLine(jid,getLoginConfig().getServerName())==0)
//        {
//            Toast.makeText(UserAddActivity.this,"用户名不存在，请确认",Toast.LENGTH_SHORT).show();
//        }else
//        {
//            Intent intent=new Intent(UserAddActivity.this,UserInfoActivity.class);
//            intent.putExtra("to",jid);
//            startActivity(intent);
//        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * 判断openfire用户的状态
     *     strUrl : url格式 - http://zw:9090/plugins/presence/status?jid=zhangwei@zw&type=xml
     *    返回值 : 0 - 用户不存在error; 1 - 用户在线priority; 2 - 用户离线unavailable
     *  说明   ：必须要求 openfire加载 presence 插件，同时设置任何人都可以访问
     */
    public static short IsUserOnLine(String userjid,String servername) {
        short shOnLineState = 0;    //-不存在-
         String strUrl="http://"+servername+":9090/plugins/presence/status?jid="+userjid+"&type=xml";
        try {
            URL oUrl = new URL(strUrl);
            URLConnection oConn = oUrl.openConnection();
            if (oConn != null) {
                BufferedReader oIn = new BufferedReader(new InputStreamReader(oConn.getInputStream()));
                if (null != oIn) {
                    String strFlag = oIn.readLine();
                    oIn.close();

                    if (strFlag.indexOf("type=\"unavailable\"") >= 0) {
                        shOnLineState = 2;
                    }
                    if (strFlag.indexOf("type=\"error\"") >= 0) {
                        shOnLineState = 0;
                    } else if (strFlag.indexOf("priority") >= 0 || strFlag.indexOf("id=\"") >= 0) {
                        shOnLineState = 1;
                    }
                }
            }
        } catch (Exception e) {
                return 4;
        }

        return shOnLineState;
    }

}
