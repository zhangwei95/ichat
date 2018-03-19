package com.example.q.xmppclient.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.entity.Notice;
import com.example.q.xmppclient.entity.User;
import com.example.q.xmppclient.manager.ContacterManager;
import com.example.q.xmppclient.manager.MessageManager;
import com.example.q.xmppclient.manager.NoticeManager;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.util.AppUtil;
import com.example.q.xmppclient.util.FormatUtil;
import com.example.q.xmppclient.util.StringUtil;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;

public class UserInfoActivity extends ActivityBase implements View.OnClickListener{
    Toolbar toolbar;
    Button btn_sendMsg;
    Button btn_video;
    Button btn_addFriend;
    Button btn_PassCheck;
    Button btn_denyCheck;
    ImageView iv_icon;
    TextView tv_nickname;
    TextView tv_username;
    TextView tv_userinfo_address;
    TextView tv_userinfo_sign;
    Notice notice;
    int isFriend=2;// 1、好友 2、非好友 3、请求为好友
    User user;
    LinearLayout ll_isfriendshow;
    LinearLayout ll_isnotfriendshow;
    LinearLayout ll_confirmPass;
    String jid;
    String[] group=new String[]{"Friends"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        user=new User();
        initActionBar();
        initUserInfo();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //返回
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //icon文字同时显示
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }
    //初始化toolbar
    public void initActionBar()
    {
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setTitle("详细信息");
        toolbar.setOnCreateContextMenuListener(this);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.userinfo_item_delete:
                        Roster roster = XmppConnectionManager
                                .getInstance().getConnection().getRoster();
                        RosterEntry entry=roster.getEntry(jid);
                        try {
                            roster.removeEntry(entry);
                        }catch (XMPPException e)
                        {
                            Toast.makeText(context,"删除失败",Toast.LENGTH_SHORT).show();
                        }
                        MessageManager.getInstance(context).delChatHisWithSb(jid);
                        NoticeManager.getInstance(context).delNoticeHisWithSb(jid);
                        Intent delintent=new Intent(context,MainActivity.class);
                        startActivity(delintent);
                        break;
                }
                return true;    //返回为true

            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar_userinfomenu,menu);
        return  true;
    }
    // 初始化控件
    public  void initUserInfo()
    {
        ll_isfriendshow=(LinearLayout)findViewById(R.id.ll_isfriendshow);
        ll_isnotfriendshow=(LinearLayout)findViewById(R.id.ll_isnotfriendshow);
        ll_confirmPass=(LinearLayout)findViewById(R.id.ll_confirmPass);
        iv_icon=(ImageView)findViewById(R.id.userinfo_iv_icon);
        tv_nickname=(TextView)findViewById(R.id.userinfo_tv_nickname);
        tv_username=(TextView)findViewById(R.id.userinfo_tv_username);
        tv_userinfo_address= (TextView) findViewById(R.id.tv_userinfo_address);
        tv_userinfo_sign= (TextView) findViewById(R.id.tv_userinfo_sign);
        btn_sendMsg=(Button)findViewById(R.id.userinfo_btn_sendMsg);
        btn_video=(Button)findViewById(R.id.userinfo_btn_video);
        btn_addFriend=(Button)findViewById(R.id.userinfo_btn_addFriend);
        btn_PassCheck=(Button)findViewById(R.id.userinfo_btn_PassCheck);
        btn_denyCheck=(Button)findViewById(R.id.userinfo_btn_denyCheck);
        btn_sendMsg.setOnClickListener(this);
        btn_addFriend.setOnClickListener(this);
        btn_PassCheck.setOnClickListener(this);
        btn_denyCheck.setOnClickListener(this);
        btn_video.setOnClickListener(this);
        //判断启动形式
        if(getIntent().getSerializableExtra("notice")!=null)//点通知进来的
        {
            notice=(Notice) getIntent().getSerializableExtra("notice");
            jid=notice.getFrom();
            if(notice.getNoticeType()==notice.ADD_FRIEND)
            {
                //还没添加的好友
                if(!existFriend(jid)){
                    isFriend=3;
                }else {
                    //添加了的好友
                    isFriend=1;
                }
            }
        }
        else
        {
            jid=getIntent().getStringExtra("to");
            //不是点通知进来的，不是好友
            if (!existFriend(jid)) {
                isFriend=2;
            }else if(existFriend(jid)) {//是好友
                isFriend=1;

            }
        }
        if(isFriend==1){
            user=ContacterManager.getUserByJidSql(jid);
        }else{
            VCard vcard=null;
            try {
                vcard = new VCard();
                vcard.load(XmppConnectionManager.getInstance().getConnection(),jid);
                user.setNickName(vcard.getNickName());
                user.setIcon(FormatUtil.Bytes2Bitmap(vcard.getAvatar()));
                //更新本地好友头像
                //todo 可以对im_contactors的表结构更改，添加好友个信息字段然后保存到本地数据库
                AppUtil.cachedAvatarImage(this,FormatUtil.Bytes2Bitmap(vcard.getAvatar()),jid);
                user.setProvince(vcard.getAddressFieldHome("province"));
                user.setCity(vcard.getAddressFieldHome("city"));
                user.setSign(vcard.getAddressFieldHome("sign"));
                user.setCountry(vcard.getAddressFieldHome("country"));
            }catch (XMPPException e) {
            //todo 异步更新好友信息
                Toast.makeText(this,"请检查网络连接~",Toast.LENGTH_SHORT).show();
            }
        }
        iv_icon.setImageBitmap(user.getIcon());
        tv_username.setText("IChatNo:" + StringUtil.getUserNameByJid(jid));
        tv_nickname.setText(user.getNickName());
        if(StringUtil.empty(user.getCountry())){
            tv_userinfo_address.setText("该用户是黑户！");
        } else if ("中国".equals(user.getCountry())) {
            tv_userinfo_address.setText(user.getProvince() + " " + user.getCity());
        } else {
            tv_userinfo_address.setText(user.getCountry());
        }
        if (StringUtil.empty(user.getSign())){
            tv_userinfo_sign.setText("该用户暂未设置心情！");
        }else{
            tv_userinfo_sign.setText(user.getSign());
        }

        //按钮显示类型
        switch (isFriend)
        {
            case 1://处理好友
                ll_confirmPass.setVisibility(View.GONE);
                ll_isnotfriendshow.setVisibility(View.GONE);
                ll_isfriendshow.setVisibility(View.VISIBLE);
                break;
            case 2://处理不是好友
                ll_confirmPass.setVisibility(View.GONE);
                ll_isnotfriendshow.setVisibility(View.VISIBLE);
                ll_isfriendshow.setVisibility(View.GONE);
                break;
            case 3://处理添加好友
                ll_confirmPass.setVisibility(View.VISIBLE);
                ll_isnotfriendshow.setVisibility(View.GONE);
                ll_isfriendshow.setVisibility(View.GONE);
                break;
        }
    }
    //按钮监听事件
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.userinfo_btn_addFriend:
                    try {
                        sendSubscribe(Presence.Type.subscribed, jid);
                        sendSubscribe(Presence.Type.subscribe, jid);
                        XmppConnectionManager.getInstance().getConnection().getRoster()
                                .createEntry(jid,StringUtil.getUserNameByJid(jid) , group);
                    } catch (XMPPException e)
                    {
                        Toast.makeText(this,"添加失败", Toast.LENGTH_SHORT).show();
                    }
                    // removeInviteNotice(notice.getId());
                    Intent intentRequest=new Intent(this,MainActivity.class);
                    startActivity(intentRequest);
                    break;

                case R.id.userinfo_btn_sendMsg:
                    Intent intentSend = new Intent(this, ChatActivity.class);
                    intentSend.putExtra("to", jid);
                    startActivity(intentSend);
                    break;

                case R.id.userinfo_btn_PassCheck:
                    try {
                        sendSubscribe(Presence.Type.subscribed, jid);
                        XmppConnectionManager.getInstance().getConnection().getRoster()
                                .createEntry(jid, StringUtil.getUserNameByJid(jid), group);
//                        NoticeManager noticeManager = NoticeManager
//                                .getInstance(context);
//                        noticeManager.updateAddFriendStatus(
//                                notice.getId(),
//                                Notice.READ,
//                                "已经同意"
//                                        + StringUtil.getUserNameByJid(jid
//                                        + "的好友申请"));
                    }catch (XMPPException e)
                    {

                    }
                    Intent intentAllow = new Intent(this, ChatActivity.class);
                    intentAllow.putExtra("to", jid);
                    startActivity(intentAllow);
                    break;
                case R.id.userinfo_btn_video:
                    Toast.makeText(this,"本功能暂未实现，敬请期待",Toast.LENGTH_SHORT).show();
                    break;
                case R.id.userinfo_btn_denyCheck:
                    sendSubscribe(Presence.Type.unsubscribe,jid);
                    Intent intentDeny=new Intent(this,MainActivity.class);
                    startActivity(intentDeny);
                    break;
            }
        }

    /**
     * 回复一个presence信息给用户
     *
     * @param type
     * @param to
     */
    protected void sendSubscribe(Presence.Type type, String to) {
        Presence presence = new Presence(type);
        presence.setTo(to);
        XmppConnectionManager.getInstance().getConnection()
                .sendPacket(presence);
    }
    public  boolean  existFriend(String jid)
    {
        for (User item:ContacterManager.getContacterList())
        {
            if (item.getJid().equals(jid))
            {
                return true;
            }
        }
        return false;
    }
    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onResume() {
        super.onResume();
        initUserInfo();
    }
}
