package com.example.q.xmppclient.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.manager.LoginConfig;
import com.example.q.xmppclient.task.RegisterTask;
import com.example.q.xmppclient.util.FormatUtil;
import com.example.q.xmppclient.util.StringUtil;
import com.example.q.xmppclient.util.ValidataUtil;

import java.util.Map;

public class RegisterActivity extends ActivityBase {
    private Button registerbtn;
    private EditText et_pwd,et_confirm_pwd,et_nickname,et_username;
    private LoginConfig loginConfig;
    private Map<String,String> attr;

    public void initActionBar()
    {
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("注册");
        toolbar.setOnCreateContextMenuListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initActionBar();
        registerbtn=(Button)findViewById(R.id.btn_register);
        et_username=(EditText)findViewById(R.id.et_username);
        et_pwd=(EditText)findViewById(R.id.et_pwd);
        et_confirm_pwd=(EditText)findViewById(R.id.et_pwd_confirm);
        et_nickname=(EditText)findViewById(R.id.et_nick);
        //限制nickname只能输入字母数字汉字
        et_nickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String editable = et_nickname.getText().toString();
                String str = StringUtil.stringFilter(editable.toString());
                if (!editable.equals(str)) {
                    et_nickname.setText(str);
                //设置新的光标所在位置  
                    et_nickname.setSelection(str.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count=0;
                String username=et_username.getText().toString();
                String pwd=et_pwd.getText().toString();
                String confirm_pwd=et_confirm_pwd.getText().toString();
                String nickname=et_nickname.getText().toString();
                loginConfig=getLoginConfig();

                if(ValidataUtil.isEmpty(et_username,"用户名"))
                {
                    showToast("用户名不能为空！");
                }else
                {
                    if(FormatUtil.isNumeric(username))
                    {
                        showToast("用户名不能为纯数字！");
                    }else {
                        loginConfig.setUsername(username);
                        count++;
                    }
                }
                if(ValidataUtil.isEmpty(et_pwd,"密码"))
                {
                    showToast("密码不能为空！");
                }else
                {
                    loginConfig.setPassword(pwd);
                    count++;
                }
                if(ValidataUtil.isEmpty(et_confirm_pwd,"确认密码"))
                {
                    showToast("确认密码不能为空！");
                }else
                {
                    count++;
                }
                if(confirm_pwd.equals(pwd))
                {
                    count++;
                }else
                {
                    et_confirm_pwd.setError("两次输入的密码不一致！");
                    et_pwd.clearAnimation();
                    et_confirm_pwd.clearAnimation();
                    et_confirm_pwd.setFocusable(true);
                    et_confirm_pwd.requestFocus();
                }

                if(count==4) {
                    if(nickname.trim().equals(""))
                    {
                        nickname=username;
                        loginConfig.setNickname(nickname);
                    }else
                    {
                        loginConfig.setNickname(nickname);
                    }
                    loginConfig.setFirstStart(true);
                    loginConfig.setNewUser(true);
                    RegisterTask registerTask = new RegisterTask(RegisterActivity.this, loginConfig);
                    registerTask.execute();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
