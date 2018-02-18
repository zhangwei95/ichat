package com.example.q.xmppclient.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.manager.LoginConfig;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.service.ChatService;
import com.example.q.xmppclient.task.LoginTask;
import com.example.q.xmppclient.util.ValidataUtil;

import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

public class LoginActivity extends ActivityTool {
    private Button btn_login,btn_register;
    private EditText et_username,et_password;
    private CheckBox cb_isRemember,cb_isAutoLogin;
    private LoginConfig loginConfig;
    private static int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    public  static int  count=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
//        Intent startserviceintent=new Intent(this,ConnectService.class);
//        startService(startserviceintent);
//        final EditText serverip = (EditText) findViewById(R.id.editText_serverIp);
//        final Handler handler = new Handler() {
//            public void handleMessage(Message msg) {
//                switch (msg.what) {
//                    case 1://handle message.what=1; 登录成功
//                        Intent intent = new Intent("com.example.q.xmppclient.action_login");
//                        startActivity(intent);
//                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
//                        break;
//                    case 2://handle message.what=2;为用户名、密码或服务器为空
//                        Toast.makeText(LoginActivity.this, "请输入用户名密码", Toast.LENGTH_SHORT).show();
//                        break;
//                    case 3://handle message.what=3;为用户名或密码错误
//                        Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
//                        break;
//                    case 4://handle message.what=4;还没连接上服务器，请检测网络是否连接
//                        Toast.makeText(LoginActivity.this, "还没连接上服务器，请检测网络是否连接", Toast.LENGTH_SHORT).show();
//                        break;
//                    default:
//                        break;
//                }
//            }
//        };
//        ConnectThread connectThread=new ConnectThread(
//                "192.168.155.1",
//                username.getText().toString().trim(),
//                password.getText().toString().trim(),
//                handler,LoginActivity.this
//        );
//        connectThread.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 初始化xmpp配置


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }

    }

    protected void  init() {
        loginConfig = getLoginConfig();
        XmppConnectionManager.getInstance().init(loginConfig);
        if (loginConfig.isAutoLogin()) {
            LoginTask loginTask = new LoginTask(LoginActivity.this, loginConfig);
            loginTask.execute();
        }
        //实例化各组件
        et_username = (EditText) findViewById(R.id.editText_userName);
        et_password = (EditText) findViewById(R.id.editText_password);
        cb_isRemember = (CheckBox) findViewById(R.id.cb_isRemember);
        cb_isAutoLogin = (CheckBox) findViewById(R.id.cb_isAutoLogin);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_register = (Button) findViewById(R.id.btn_register);
        //设置各组件默认状态
        et_username.setText(loginConfig.getUsername());
        et_password.setText(loginConfig.getPassword());
        cb_isRemember.setChecked(loginConfig.isRemember());
        cb_isRemember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    cb_isAutoLogin.setChecked(false);
                }
            }
        });
        cb_isAutoLogin.setChecked(loginConfig.isAutoLogin());
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo 登录按钮的操作
                    String username = et_username.getText().toString();
                    String password = et_password.getText().toString();
                    // 先记录下各组件的目前状态,登录成功后才保存
                    loginConfig.setPassword(password);
                    loginConfig.setUsername(username);
                    loginConfig.setXmppIP(getResources().getString(R.string.xmpp_host));
                    loginConfig.setServerName(getResources().getString(R.string.xmpp_service_name));
                    loginConfig.setRemember(cb_isRemember.isChecked());
                    loginConfig.setAutoLogin(cb_isAutoLogin.isChecked());
                    LoginTask loginTask = new LoginTask(LoginActivity.this, loginConfig);
                    loginTask.execute();

            }
        });
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sendCode(LoginActivity.this);
//                Intent intent=new Intent();
//                intent.setAction("android.intent.action.register");
//                startActivity(intent);
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);

            }
        });
    }
//    public void sendCode(Context context) {
//        RegisterPage page = new RegisterPage();
//        page.setRegisterCallback(new EventHandler() {
//            public void afterEvent(int event, int result, Object data) {
//                if (result == SMSSDK.RESULT_COMPLETE) {
//                    // 处理成功的结果
//                    HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
//                    String country = (String) phoneMap.get("country"); // 国家代码，如“86”
//                    String phone = (String) phoneMap.get("phone"); // 手机号码，如“13800138000”
//                    // TODO 利用国家代码和手机号码进行后续的操作
//                } else{
//                    // TODO 处理错误的结果
//                }
//            }
//        });
//        page.show(context);
//    }

    private boolean checkData()
    {
        boolean check=false;
        check=!ValidataUtil.isEmpty(et_username,"用户名")&&!ValidataUtil.isEmpty(et_password,"密码");
        return check;
    }

    @Override
    public void onBackPressed() {
        isExit();
    }
}
