package com.example.q.xmppclient.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.common.Constant;
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
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smackx.packet.VCard;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import dmax.dialog.SpotsDialog;

public class UserInfoActivity extends ActivityBase implements View.OnClickListener{
    Toolbar toolbar;
    Button btn_sendMsg;
    Button btn_video;
    Button btn_addFriend;
    Button btn_PassCheck;
    Button btn_denyCheck;
    Button btn_waiting;
    ImageView iv_icon;
    TextView tv_nickname;
    TextView tv_username;
    TextView tv_userinfo_address;
    TextView tv_userinfo_sign;
    Notice notice;
    String itemType= "none";//好友类型
    User user;
    LinearLayout ll_isfriendshow;
    LinearLayout ll_isnotfriendshow;
    LinearLayout ll_confirmPass;
    LinearLayout ll_waitingValidate;
    String jid;
    String[] group=new String[]{"Friends"};
    AlertDialog dialog;
    private MenuItem menuItem;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        user=new User();
        initActionBar();
        initUserInfo();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "onNewIntent: ");
        setIntent(intent);
        initUserInfo();
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
    public void initActionBar() {
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setTitle("详细信息");
        toolbar.setOnCreateContextMenuListener(this);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.userinfo_item_delete:
                        DelTask task=new DelTask();
                        task.execute();
                        break;
                      //返回为true
                }
                return true;
            }
        });

    }
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar_userinfomenu,menu);
        menuItem=menu.findItem(R.id.userinfo_item_delete);
        if("both".equals(itemType)){
            menuItem.setVisible(true);
        }else{
            menuItem.setVisible(false);
        }
        return  true;
    }
    // 初始化控件
    public  void initUserInfo()
    {
        ll_isfriendshow=(LinearLayout)findViewById(R.id.ll_isfriendshow);
        ll_isnotfriendshow=(LinearLayout)findViewById(R.id.ll_isnotfriendshow);
        ll_confirmPass=(LinearLayout)findViewById(R.id.ll_confirmPass);
        ll_waitingValidate=(LinearLayout)findViewById(R.id.ll_waitingValidate);
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
        btn_waiting=(Button)findViewById(R.id.userinfo_btn_Waiting);
        btn_sendMsg.setOnClickListener(this);
        btn_addFriend.setOnClickListener(this);
        btn_PassCheck.setOnClickListener(this);
        btn_denyCheck.setOnClickListener(this);
        btn_video.setOnClickListener(this);
        intent=this.getIntent();
        jid=intent.getStringExtra("to");
        itemType=existFriend(jid);
        //判断启动形式
//        if(getIntent().getExtras().getSerializable("notice")!=null){//点通知进来的
//
//            notice=(Notice) getIntent().getExtras().getSerializable("notice");
//            Log.e(TAG, "initUserInfo: notice  id="+notice.getId()+"  from= "+notice.getFrom());
//            jid=notice.getFrom();
//            Log.e(TAG, "initUserInfo: notice    jid= "+jid);
//            itemType=existFriend(jid);
//            Log.e(TAG, "initUserInfo: notice    jid= "+jid);
////            if(notice.getNoticeType()==notice.ADD_FRIEND)
////            {
////                //还没添加的好友
////                if(!existFriend(jid)){
////                    isFriend=3;
////                }else {
////                    //添加了的好友
////                    isFriend=1;
////                }
////            }
//        } else {
//            jid=getIntent().getStringExtra("to");
//            Log.e(TAG, "initUserInfo: not notice    jid= "+jid);
//            itemType=existFriend(jid);
////            //不是点通知进来的，不是好友
////            if (!existFriend(jid)) {
////                isFriend=2;
////            }else if(existFriend(jid)) {//是好友
////                isFriend=1;
////            }
//
//        }
////        if(isFriend==1){
////            user=ContacterManager.getUserByJidSql(jid);
////        }else{
//
////        }

//        if (!itemType.equals("none")&&!itemType.equals("remove")){
        if(ContacterManager.isExistInDB(jid)) {
                user = ContacterManager.getUserByJidSql(jid);
//            }
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
        switch (itemType)
        {
            //互相不是好友
            case "none":
                ll_confirmPass.setVisibility(View.GONE);
                ll_isnotfriendshow.setVisibility(View.VISIBLE);
                ll_waitingValidate.setVisibility(View.GONE);
                ll_isfriendshow.setVisibility(View.GONE);
                break;
            //等待通过
            case "from":
                //todo 等待通过
                ll_waitingValidate.setVisibility(View.VISIBLE);
                ll_confirmPass.setVisibility(View.GONE);
                ll_isnotfriendshow.setVisibility(View.GONE);
                ll_isfriendshow.setVisibility(View.GONE);

                break;
            //确认通过
            case "to":
                ll_confirmPass.setVisibility(View.VISIBLE);
                ll_waitingValidate.setVisibility(View.GONE);
                ll_isnotfriendshow.setVisibility(View.GONE);
                ll_isfriendshow.setVisibility(View.GONE);
                break;
            //互为好友
            case "both"://处理好友
                ll_confirmPass.setVisibility(View.GONE);
                ll_isnotfriendshow.setVisibility(View.GONE);
                ll_waitingValidate.setVisibility(View.GONE);
                ll_isfriendshow.setVisibility(View.VISIBLE);
                break;
            //以删除的好友
            case "remove":
                ll_confirmPass.setVisibility(View.GONE);
                ll_isnotfriendshow.setVisibility(View.VISIBLE);
                ll_waitingValidate.setVisibility(View.GONE);
                ll_isfriendshow.setVisibility(View.GONE);
                break;
        }
    }
    //按钮监听事件
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.userinfo_btn_addFriend:
                    AddTask addTask=new AddTask();
                    addTask.execute();
                    break;

                case R.id.userinfo_btn_sendMsg:
                    Intent intentSend = new Intent(this, ChatActivity.class);
                    intentSend.putExtra("to", jid);
                    startActivity(intentSend);
                    break;

                case R.id.userinfo_btn_PassCheck:
                    CheckTask checkTask=new CheckTask();
                    checkTask.execute();
                    break;
                case R.id.userinfo_btn_video:
                    Toast.makeText(this,"本功能暂未实现，敬请期待",Toast.LENGTH_SHORT).show();
                    break;
                case R.id.userinfo_btn_denyCheck:
                    DenyTask denyTask=new DenyTask();
                    denyTask.execute();
                    break;
            }
        }
    private class DelTask extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Boolean o) {
            if (o){
                dialog=new SpotsDialog(context, "删除成功",R.style.Custom);
                dialog.show();
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Intent delintent=new Intent(context,MainActivity.class);
                        startActivity(delintent);
                    }
                },500);
                finish();
            }else{
                dialog=new SpotsDialog(context, "删除失败",R.style.Custom);
                dialog.show();
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                },500);
            }
            super.onPostExecute(o);
        }
        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                sendSubscribe(Presence.Type.unsubscribe, jid);
                ContacterManager.deleteUser(jid);
                ContacterManager.deleteDBFriend(jid);
            }catch (XMPPException e) {
                return false;
            }
            Intent delBoastcase=new Intent(Constant.ROSTER_DELETED);
            sendBroadcast(delBoastcase);
            return true;
        }
    }
    private class DenyTask extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Boolean o) {
            if (o){
                dialog=new SpotsDialog(context, "拒绝成功",R.style.Custom);
                dialog.show();
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Intent intentDeny=new Intent(context,MainActivity.class);
                        startActivity(intentDeny);
                    }
                },500);

            }else{
                dialog=new SpotsDialog(context, "拒绝失败",R.style.Custom);
                dialog.show();
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                },500);
            }
            super.onPostExecute(o);
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            sendSubscribe(Presence.Type.unsubscribed, jid);
//
//            try {
//                sendSubscribe(Presence.Type.unsubscribed, jid);
//                XmppConnectionManager.getInstance().getConnection().getRoster()
//                        .createEntry(jid,StringUtil.getUserNameByJid(jid), group);
//            } catch (XMPPException e)
//            {
//                return false;
//            }
            return true;
        }
    }
    private class CheckTask extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Boolean o) {
            if (o){
                dialog=new SpotsDialog(context, "验证通过",R.style.Custom);
                dialog.show();
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Intent intentAllow = new Intent(context, ChatActivity.class);
                        intentAllow.putExtra("to", jid);
                        startActivity(intentAllow);
                        finish();
                    }
                },500);

            }else{
                dialog=new SpotsDialog(context, "验证失败",R.style.Custom);
                dialog.show();
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                },500);
            }
            super.onPostExecute(o);
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                sendSubscribe(Presence.Type.subscribe, jid);
                Log.e(TAG, "check send subscribe" );
                sendSubscribe(Presence.Type.subscribed, jid);
                Log.e(TAG, "check send subscribed" );
                sendSubscribe(Presence.Type.available, jid);
                Log.e(TAG, "check send available" );
                if(StringUtil.empty(XmppConnectionManager.getInstance().getConnection().
                        getRoster().getEntry(jid))) {
                    XmppConnectionManager.getInstance().getConnection().getRoster()
                            .createEntry(jid, StringUtil.getUserNameByJid(jid), group);
                }
                //todo 代码重复 待删除 和entriedUpdate
//                if(ContacterManager.isExistInDB(jid)){
//                    Log.e(TAG, "isExistInDB jid="+jid+"entry type="+FormatUtil.ItemType2string(
//                            XmppConnectionManager.getInstance().
//                            getConnection().getRoster().getEntry(jid).getType()));
//                    ContacterManager.updateDBFriend(XmppConnectionManager.getInstance().
//                            getConnection().getRoster().getEntry(jid),jid,XmppConnectionManager.
//                            getInstance().getConnection());
//                }else{
//                    Log.e(TAG, "isnotExistInDB jid="+jid+"entry type="+FormatUtil.ItemType2string(
//                            XmppConnectionManager.getInstance().
//                                    getConnection().getRoster().getEntry(jid).getType()) );
//                    ContacterManager.insertDBFriend(XmppConnectionManager.getInstance().
//                            getConnection().getRoster().getEntry(jid),jid,XmppConnectionManager.
//                            getInstance().getConnection());
//                }
            }catch (XMPPException e) {
                return false;
            }
            Intent checkBoasdcast=new Intent(Constant.ROSTER_ADDED);
            sendBroadcast(checkBoasdcast);
            return true;
        }
    }
    private class AddTask extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Boolean o) {
            if (o){
                dialog=new SpotsDialog(context, "请求成功，等待验证",R.style.Custom);
                dialog.show();
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Intent addintent=new Intent(context,MainActivity.class);
                        startActivity(addintent);
                        finish();
                    }
                },500);
            }else{
                dialog=new SpotsDialog(context, "请求失败",R.style.Custom);
                dialog.show();
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                },500);
            }
            super.onPostExecute(o);
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                sendSubscribe(Presence.Type.subscribe, jid);
                if(StringUtil.empty(XmppConnectionManager.getInstance().getConnection().
                        getRoster().getEntry(jid))) {
                    XmppConnectionManager.getInstance().getConnection().getRoster()
                            .createEntry(jid, StringUtil.getUserNameByJid(jid), group);
                }
                sendSubscribe(Presence.Type.subscribed, jid);
                //todo 代码重复 待删除 和entriedadd
//                if(ContacterManager.isExistInDB(jid)){
//                    Log.e(TAG, "isExistInDB jid="+jid+" entry type="+FormatUtil.ItemType2string(
//                            XmppConnectionManager.getInstance().
//                                    getConnection().getRoster().getEntry(jid).getType()) );
//                    ContacterManager.updateDBFriend(XmppConnectionManager.getInstance().
//                            getConnection().getRoster().getEntry(jid),jid,XmppConnectionManager.
//                            getInstance().getConnection());
//                }else{
//                    Log.e(TAG, "isnotExistInDB jid="+jid+"entry type="+FormatUtil.ItemType2string(
//                            XmppConnectionManager.getInstance().
//                                    getConnection().getRoster().getEntry(jid).getType()) );
//                    ContacterManager.insertDBFriend(XmppConnectionManager.getInstance().
//                            getConnection().getRoster().getEntry(jid),jid,XmppConnectionManager.
//                            getInstance().getConnection());
//                }
                sendSubscribe(Presence.Type.available, jid);
            } catch (XMPPException e)
            {
                return false;
            }
            Intent checkBoasdcast=new Intent(Constant.ROSTER_ADDED);
            sendBroadcast(checkBoasdcast);
            return true;
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
    String TAG="userinfo";
    public String existFriend(String jid)
    {
        jid=jid.toLowerCase();
        for (User item:ContacterManager.getContacterList())
        {
            Log.e(TAG, "jid="+item.getJid()+"-itemtype="+item.getItemType());
            if (item.getJid().equals(jid))
            {
                if(XmppConnectionManager.getInstance().getConnection().isConnected()){
                    RosterEntry entry=XmppConnectionManager.getInstance().getConnection().
                            getRoster().getEntry(jid);
                    if(entry!=null) {
                        if (entry.getType() != item.getItemType()) {
                            ContacterManager.updateDBFriend(XmppConnectionManager.getInstance().
                                    getConnection().getRoster().getEntry(jid), jid, XmppConnectionManager.
                                    getInstance().getConnection());
                            Log.e(TAG, "existFriend:  diff update to type:" +
                                    FormatUtil.ItemType2string(entry.getType()));
                            return FormatUtil.ItemType2string(entry.getType());
                        } else {
                            Log.e(TAG, "existFriend:  same type:" +
                                    FormatUtil.ItemType2string(item.getItemType()));
                            return FormatUtil.ItemType2string(item.getItemType());
                        }
                    }else {
                    return "none";
                    }
                }
                Log.e(TAG, "existFriend: not connect  dbtype:"+
                        FormatUtil.ItemType2string(item.getItemType()) );
                return FormatUtil.ItemType2string(item.getItemType());
            }
        }
        Log.e(TAG, "isnot existFriend: itemtype=none"+ContacterManager.getContacterList().size());
        return "none";
    }
    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onResume() {

        super.onResume();
    }
}
