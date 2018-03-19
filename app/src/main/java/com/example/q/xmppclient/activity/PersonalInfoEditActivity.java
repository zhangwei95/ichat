package com.example.q.xmppclient.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.manager.LoginConfig;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.task.SetVcardTask;
import com.example.q.xmppclient.util.StringUtil;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import dmax.dialog.SpotsDialog;

public class PersonalInfoEditActivity extends ActivityBase {
    Toolbar toolbar;
    EditText et_InfoEdit;
    TextView tv_helper;
    LoginConfig loginConfig;
    Intent intent;
    Button btn_Right;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info_edit);
        init();
        initActionBar();
        dialog=new SpotsDialog(this,R.style.Custom);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) et_InfoEdit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(et_InfoEdit, 0);
            }

        }, 500);
    }
    public void init()
    {
        loginConfig=getLoginConfig();
        et_InfoEdit=(EditText)findViewById(R.id.et_InfoEdit);
        btn_Right=(Button)findViewById(R.id.btn_Right);
        tv_helper=(TextView)findViewById(R.id.tv_helper);
        btn_Right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (intent.getStringExtra("EDIT"))
                {
                    case "昵称":
                        loginConfig.setNickname(et_InfoEdit.getText().toString().trim());
                        MainActivity.currentUser.setNickName(et_InfoEdit.getText().toString().trim());
                        break;
                    case "签名":
                        loginConfig.setSign(et_InfoEdit.getText().toString().trim());
                        MainActivity.currentUser.setSign(et_InfoEdit.getText().toString().trim());
                        break;
                }
                SetVcardTask setVcardTask=new SetVcardTask();
                setVcardTask.execute();
            }
        });
        et_InfoEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count==0)
                {
                    switch (intent.getStringExtra("EDIT"))
                    {
                        case "昵称":
                            et_InfoEdit.setHint("取个让人记得住的名字吧！");
                            break;
                        case "签名":
                            et_InfoEdit.setHint("还没有设置签名呢！");
                            break;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    public void initActionBar()
    {
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        intent=getIntent();
        intent.getStringExtra("EDIT");
        switch (intent.getStringExtra("EDIT"))
        {
            case "昵称":
            setTitle("修改昵称");
                et_InfoEdit.setHint("取个让人记得住的名字吧！");
                et_InfoEdit.setText(loginConfig.getNickname());//set之后光标自动去第一个位置
                et_InfoEdit.setSelection(et_InfoEdit.getText().length());//将光标移至文字末尾
                tv_helper.setText("取个好名字吧！！！");
                break;
            case "签名":
                setTitle("修改签名");
//                if(StringUtil.empty(loginConfig.getSign())) {
//
//                    et_InfoEdit.setHint("还没有设置签名呢！");
//                }else
//                {
                    et_InfoEdit.setText(loginConfig.getSign());
                    et_InfoEdit.setSelection(et_InfoEdit.getText().length());//将光标移至文字末尾
//                }
                tv_helper.setText("快让大家了解你吧！");
                break;
        }
        toolbar.setOnCreateContextMenuListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info_edit_menu, menu);
        return true;
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
    class SetVcardTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            dialog=new SpotsDialog(context, "正在保存",R.style.Custom);
            dialog.show();
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(Void... param) {
            VCard vCard = new VCard();
            try {
                vCard.load(XmppConnectionManager.getInstance().getConnection());
            }catch (XMPPException e)
            {
                return false;
            }

            // 设置和更新用户信息
            vCard.setNickName(loginConfig.getNickname());
            vCard.setAddressFieldHome(Constant.COUNTRY,loginConfig.getCountry());
            vCard.setAddressFieldHome(Constant.PROVINCE,loginConfig.getProvince());
            vCard.setAddressFieldHome(Constant.CITY,loginConfig.getCity());
            vCard.setAddressFieldHome(Constant.SIGN,loginConfig.getSign());
            vCard.setAvatar(getDefaultAvatar());

            try {
                vCard.save(XmppConnectionManager.getInstance().getConnection());
                saveLoginConfig(loginConfig);
                return true;
            } catch (XMPPException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            dialog.dismiss();
            if (bool) {
                dialog=new SpotsDialog(context,"保存成功",R.style.Custom);
            }else
            {
                dialog=new SpotsDialog(context, "保存失败",R.style.Custom);
            }
            dialog.show();
            Timer timer=new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    dialog.dismiss();
                    Intent intent = new Intent(PersonalInfoEditActivity.this, MainActivity.class);
                    intent.putExtra("action", "edit");
                    startActivity(intent);
                }
            },500);

        }

        /**
         * 获取本地头像,没有则缓存到本地
         */
        private byte[] getDefaultAvatar() {
            //默认image路径
            String imageDir = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() +
                    PersonalInfoEditActivity.this.getResources().getString(R.string.img_dir) + "/";
            File dirfile = new File(imageDir);
            //获取内部存储状态
            String state = Environment.getExternalStorageState();
            //如果状态不是mounted，无法读写
            if (!state.equals(Environment.MEDIA_MOUNTED)) {
                return null;
            }
            //本地读文件
            String fileName = "avatar_" + StringUtil.getJidByName
                    (loginConfig.getUsername(), loginConfig.getServerName()) + ".png";
            byte[] bytes = null;
            try {
                if (!dirfile.exists()) {
                    dirfile.mkdirs();
                }
                File file = new File(imageDir, fileName);
                try {
                    FileInputStream in = new FileInputStream(file);
                    bytes = new byte[(int) file.length()];
                    in.read(bytes);
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return bytes;
        }
    }
}
