package com.example.q.xmppclient.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.q.xmppclient.adapter.FriendAdapter;
import com.example.q.xmppclient.adapter.NoticeAdapter;
import com.example.q.xmppclient.adapter.RecentChatAdapter;
import com.example.q.xmppclient.adapter.ViewPagerAdapter;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.entity.ChatRecordInfo;
import com.example.q.xmppclient.R;
import com.example.q.xmppclient.entity.Notice;
import com.example.q.xmppclient.entity.User;
import com.example.q.xmppclient.manager.ContacterManager;
import com.example.q.xmppclient.manager.LoginConfig;
import com.example.q.xmppclient.manager.MessageManager;
import com.example.q.xmppclient.manager.NoticeManager;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.task.LoginTask;
import com.example.q.xmppclient.util.AppUtil;
import com.example.q.xmppclient.util.BottomNavigationViewHelper;
import com.example.q.xmppclient.util.DateUtil;
import com.example.q.xmppclient.util.FormatUtil;
import com.example.q.xmppclient.util.StringUtil;
import com.mob.wrappers.AnalySDKWrapper;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.q.xmppclient.common.Constant.ROSTER_PRESENCE_CHANGED;

public class MainActivity extends ActivityBase {

    //common
    public static User sendMsgTo;
    Toolbar toolbar;
    public LoginConfig loginConfig;
    public static User currentUser;//当前登录的用户
    myViewPager viewpager;
    BottomNavigationView navigation;
    private MenuItem menuItem;
    private long startTime=0;//保存第一次点击返回键的时间
    private MessageManager messageMgr;
    private NoticeManager noticeManager;

    //PersonalInfo
    View PersonalInfoView;
    private LinearLayout personInfoll, ll_icon, ll_nick, ll_user, ll_place, ll_sign;
    private ImageView image;
    TextView tv_nickname, tv_username, tv_place, tv_sign;
    //FriendList
    View FriendListView;
    private ListView friendListView;
    private LinearLayout ll_useradd;
    private LinearLayout ll_groupadd;
    private FriendAdapter friendadapter;

    //RecentChat
    View ChatRecentView;
    private LinearLayout chatInfoll;
    private ListView RecentChatListView;
    private ContacterReceiver receiver = null;
    private NoticeAdapter noticeAdapter = null;
    private RecentChatAdapter recentChatAdapter;
    private List<ChatRecordInfo> inviteNotices =null;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // 卸载广播接收器,创建
        if(receiver!=null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
//        if(!XmppConnectionManager.getInstance().getConnection().isConnected()) {
//            LoginTask loginTask = new LoginTask(this, loginConfig);
//            loginTask.execute();
//        }
        refreshRecentChat();
        super.onResume();
    }

    public void init() {
        viewpager = (myViewPager) findViewById(R.id.view_pager);
        LayoutInflater lf = getLayoutInflater().from(this);
        ChatRecentView = lf.inflate(R.layout.main_chat_recent, null);
        FriendListView = lf.inflate(R.layout.main_friend_list, null);
        PersonalInfoView = lf.inflate(R.layout.main_personal_info, null);
        ArrayList<View> viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
        viewList.add(ChatRecentView);
        viewList.add(FriendListView);
        viewList.add(PersonalInfoView);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //默认 >3 的选中效果会影响ViewPager的滑动切换时的效果，故利用反射去掉
        BottomNavigationViewHelper.disableShiftMode(navigation);
        viewpager.setAdapter(new ViewPagerAdapter(viewList));
        viewpager.setCurrentItem(0);
        viewpager.setPageTransformer(true, new DepthPageTransformer());
        viewpager.addOnPageChangeListener(new myViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                menuItem = navigation.getMenu().getItem(position);
                menuItem.setChecked(true);
                switch (position) {
                    case 0:
                        toolbar.setTitle("ICHAT");
                        break;
                    case 1:
                        toolbar.setTitle("好友");
                        break;
                    case 2:
                        toolbar.setTitle("个人信息");
                        break;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    //页面切换效果
    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);
            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);
            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);
                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);
                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    public void initActionBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("IChat");
        toolbar.setOnCreateContextMenuListener(this);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.relogin:
                        Intent reloginIntent = new Intent(context, LoginActivity.class);
                        loginConfig.setUsername("");
                        loginConfig.setPassword("");
                        loginConfig.setAutoLogin(false);
                        saveLoginConfig(loginConfig);
                        startActivity(reloginIntent);
                        loginConfig=new LoginConfig();
                        loginConfig.setServerName(getResources().
                                getString(R.string.xmpp_service_name));
                        loginConfig.setXmppPort(getResources().getInteger(R.integer.xmpp_port));
                        loginConfig.setXmppIP(getResources().getString(R.string.xmpp_host));
                        saveLoginConfig(loginConfig);
                        XmppConnectionManager.disconnect();
                        finish();
                        break;
                }
                return true;    //返回为true

            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    public void initFriendList() {
        friendadapter = new FriendAdapter(MainActivity.this,
                R.layout.friend_list_item,ContacterManager.getBothContacterList());
        friendListView = (ListView) FriendListView.findViewById(R.id.listview_FriendList);
        friendListView.setAdapter(friendadapter);
        friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendMsgTo = (User) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
                intent.putExtra("to", sendMsgTo.getJid());
                startActivity(intent);
            }
        });
        ll_useradd = (LinearLayout) FriendListView.findViewById(R.id.ll_useradd);
        ll_groupadd = (LinearLayout) FriendListView.findViewById(R.id.ll_groupadd);
        ll_useradd.setOnTouchListener(touch);
        ll_groupadd.setOnTouchListener(touch);
    }

    /**
     * 初始化最近联系人界面
     * @author zw
     */
    public void initRecentChatInfo() {
        inviteNotices=new ArrayList<ChatRecordInfo>();
        RecentChatListView = (ListView) ChatRecentView.findViewById(R.id.listview_chatList);
//        for (Notice item : noticeManager.getNoticeListByTypeAndPage(Notice.UNREAD)) {
//            ChatRecordInfo newNotice = new ChatRecordInfo();
//            newNotice.setContent(item.getContent());
//            newNotice.setFrom(item.getFrom());
//            newNotice.setNoticeTime(item.getNoticeTime());
//            newNotice.setTitle(item.getTitle());
//            newNotice.setStatus(item.getStatus());
//            inviteNotices.add(newNotice);
//        }
        for (ChatRecordInfo item : messageMgr.getRecentContactsWithLastMsg()) {
            inviteNotices.add(item);
        }
        Collections.sort(inviteNotices);
        recentChatAdapter = new RecentChatAdapter(context, R.layout.recent_chat_item,
                inviteNotices);
        RecentChatListView.setAdapter(recentChatAdapter);
        RecentChatListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatRecordInfo chatRecordInfo = (ChatRecordInfo) parent.getItemAtPosition(position);
                User user = ContacterManager.getUserByJidSql(chatRecordInfo.getFrom());
                if (user == null) {
                    chatRecordInfo.setNoticeSum(0);
                    createChat(chatRecordInfo.getFrom());
                } else {
                    chatRecordInfo.setNoticeSum(0);
                    createChat(user);
                }
            }
        });

    }

    /**
     * 创建一个聊天 好友
     *
     * @param user
     */
    protected void createChat(User user) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("to", user.getJid());
        startActivity(intent);
    }

    /**
     * 创建一个聊天非好友
     *
     * @param
     */
    protected void createChat(String jid) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("to", jid);
        intent.putExtra("isFriend", false);
        startActivity(intent);
    }
    /**
     * 初始化个人信息（我）界面
     * @author zw
     */
    public void initPersonInfo() {
        loginConfig=getLoginConfig();
        image = (ImageView) PersonalInfoView.findViewById(R.id.test_icon);
        tv_nickname = (TextView) PersonalInfoView.findViewById(R.id.tv_nickname);
        tv_username = (TextView) PersonalInfoView.findViewById(R.id.tv_username);
        tv_place = (TextView) PersonalInfoView.findViewById(R.id.tv_address);
        tv_sign = (TextView) PersonalInfoView.findViewById(R.id.tv_sign);
        ll_icon = (LinearLayout) PersonalInfoView.findViewById(R.id.ll_icon);
        ll_nick = (LinearLayout) PersonalInfoView.findViewById(R.id.ll_nick);
        ll_user = (LinearLayout) PersonalInfoView.findViewById(R.id.ll_user);
        ll_place = (LinearLayout) PersonalInfoView.findViewById(R.id.ll_place);
        ll_sign = (LinearLayout) PersonalInfoView.findViewById(R.id.ll_sign);
        ll_icon.setOnTouchListener(touch);
        ll_nick.setOnTouchListener(touch);
        ll_user.setOnTouchListener(touch);
        ll_place.setOnTouchListener(touch);
        ll_sign.setOnTouchListener(touch);
        if(currentUser==null){
            currentUser=new User();
            currentUser.setJid(StringUtil.getJidByName(loginConfig.getUsername(),
                    loginConfig.getServerName()));
            currentUser.setIcon(getLocalAvatar());
            currentUser.setProvince(loginConfig.getProvince());
            currentUser.setCity(loginConfig.getCity());
            currentUser.setCountry(loginConfig.getCountry());
            currentUser.setSign(loginConfig.getSign());
            currentUser.setUsername(loginConfig.getUsername());
            currentUser.setNickName(loginConfig.getNickname());
        }
        tv_username.setText(currentUser.getUsername());
        tv_nickname.setText(currentUser.getNickName());
        image.setImageBitmap(currentUser.getIcon());
        if (StringUtil.empty(currentUser.getSign()))
            tv_sign.setText("还未设置签名哦！");
        else
            tv_sign.setText(currentUser.getSign());
        if(StringUtil.empty(currentUser.getCountry())) {
            tv_place.setText("还未设置地区哦！");
        }else if("中国".equals(currentUser.getCountry()))
        {
            tv_place.setText(loginConfig.getProvince()+" "+loginConfig.getCity());
        }else
        {
            tv_place.setText(loginConfig.getCountry());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppUtil.syncIsDebug(getApplicationContext());
        loginConfig = getLoginConfig();
//        loginConfig.setXmppPort(5222);
//        saveLoginConfig(loginConfig);
        XmppConnectionManager.getInstance().init(loginConfig);
        //
        if (!StringUtil.notEmpty(loginConfig.getUsername())
                ||!StringUtil.notEmpty(loginConfig.getPassword())){
            Intent intent=new Intent(this,LoginActivity.class);
            startActivity(intent);
            finish();
        }else {
            LoginTask loginTask=new LoginTask(this,loginConfig);
            loginTask.execute();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constant.ROSTER_ADDED);

            filter.addAction(Constant.ROSTER_DELETED);

            filter.addAction(ROSTER_PRESENCE_CHANGED);

            filter.addAction(Constant.ROSTER_UPDATED);
            // 好友请求
            filter.addAction(Constant.ROSTER_SUBSCRIPTION);
            //系统消息(暂时没用到)
            filter.addAction(Constant.ACTION_SYS_MSG);
            //新消息
            filter.addAction(Constant.NEW_MESSAGE_ACTION);
            //离线消息
            filter.addAction(Constant.GET_OFFLINEMSG);
            //更新个人信息
            filter.addAction(Constant.REFRESH_PERSONALINFO);
            //登录失败
            filter.addAction(Constant.LOGIN_FAILED);
            receiver = new ContacterReceiver();
            registerReceiver(receiver, filter);
            ContacterManager.init(this,XmppConnectionManager.getInstance()
                    .getConnection());
            messageMgr=MessageManager.getInstance(context);
            noticeManager = NoticeManager.getInstance(context);
            init();
            initActionBar();
            initFriendList();
            initRecentChatInfo();
            initPersonInfo();
        }

    }

    //处理点击个人信息栏的事件
    private View.OnTouchListener touch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.ll_icon:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ll_icon.setBackgroundColor(getResources().getColor(R.color.touchevent));
                    }
                    if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        ll_icon.setBackgroundColor(getResources().getColor(R.color.white));
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.avatar");
                        startActivity(intent);
                        ll_icon.setBackgroundColor(getResources().getColor(R.color.white));
                    }
                    return true;
                case R.id.ll_nick:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ll_nick.setBackgroundColor(getResources().getColor(R.color.touchevent));
                    }
                    if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        ll_nick.setBackgroundColor(getResources().getColor(R.color.white));
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        ll_nick.setBackgroundColor(getResources().getColor(R.color.white));
                        Intent intent=new Intent(MainActivity.this,PersonalInfoEditActivity.class);
                        intent.putExtra("EDIT","昵称");
                        startActivity(intent);
                    }
                    return true;
                case R.id.ll_user:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ll_user.setBackgroundColor(getResources().getColor(R.color.touchevent));
                    }
                    if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        ll_user.setBackgroundColor(getResources().getColor(R.color.white));
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        ll_user.setBackgroundColor(getResources().getColor(R.color.white));
                    }
                    return true;
                case R.id.ll_place:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ll_place.setBackgroundColor(getResources().getColor(R.color.touchevent));
                    }
                    if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        ll_place.setBackgroundColor(getResources().getColor(R.color.white));
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        ll_place.setBackgroundColor(getResources().getColor(R.color.white));

                    }
                    return true;

                case R.id.ll_sign:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ll_sign.setBackgroundColor(getResources().getColor(R.color.touchevent));
                    }
                    if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        ll_sign.setBackgroundColor(getResources().getColor(R.color.white));
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        ll_sign.setBackgroundColor(getResources().getColor(R.color.white));
                        Intent intent=new Intent(MainActivity.this,PersonalInfoEditActivity.class);
                        intent.putExtra("EDIT","签名");
                        startActivity(intent);
                    }
                    return true;
                case R.id.ll_useradd:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ll_useradd.setBackgroundColor(getResources().getColor(R.color.touchevent));
                    }
                    if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        ll_useradd.setBackgroundColor(getResources().getColor(R.color.white));
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        ll_useradd.setBackgroundColor(getResources().getColor(R.color.white));
                        Intent intent = new Intent(MainActivity.this, UserAddActivity.class);
                        startActivity(intent);
                    }
                    return true;
                case R.id.ll_groupadd:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ll_groupadd.setBackgroundColor(getResources().getColor(R.color.touchevent));
                    }
                    if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        ll_groupadd.setBackgroundColor(getResources().getColor(R.color.white));
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        ll_groupadd.setBackgroundColor(getResources().getColor(R.color.white));
                        Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                        startActivity(intent);
                    }
                    return true;
                default:
                    return true;
            }
        }
    };


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    toolbar.setTitle("ICHAT");
                    viewpager.setCurrentItem(0);
                    return true;
                case R.id.navigation_dashboard:
                    toolbar.setTitle("好友");
                    viewpager.setCurrentItem(1);
                    return true;
                case R.id.navigation_notifications:
                    toolbar.setTitle("个人信息");
                    viewpager.setCurrentItem(2);
                    return true;
            }
            return false;
        }
    };

    public class ContacterReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.NEW_MESSAGE_ACTION.equals(intent.getAction())){
                //收到新消息更新
                Notice notice = (Notice) intent.getSerializableExtra("notice");
                // String action = intent.getAction();
                //inviteNotices.add(notice);
                msgReceive(notice);
//                refresh();
            }else if(Constant.GET_OFFLINEMSG.equals(intent.getAction())){
                refreshRecentChat();
            }else if(Constant.REFRESH_PERSONALINFO.equals(intent.getAction())){
                refreshPersonalInfo();
            }else if(Constant.LOGIN_FAILED.equals(intent.getAction())){
                if(!XmppConnectionManager.getInstance().getConnection().isConnected()){
                    LoginTask loginTask=new LoginTask(MainActivity.this,loginConfig);
                    loginTask.execute();
                }
            }else if(Constant.ROSTER_ADDED.equals(intent.getAction())||
                    Constant.ROSTER_UPDATED.equals(intent.getAction())||
                    ROSTER_PRESENCE_CHANGED.equals(intent.getAction())){
                refreshFriendList();
            }else if(Constant.ROSTER_DELETED.equals(intent.getAction())){
                refreshFriendList();
                refreshRecentChat();
            }else if(ROSTER_PRESENCE_CHANGED.equals(intent.getAction())){
                refreshFriendList();
            }

//            else if(Constant.ROSTER_SUBSCRIPTION.equals(intent.getAction())){
//                //todo 刷新FriendList界面
//                friendadapter.notifyDataSetChanged();
//            }

        }
    }
    //刷新最近联系人列表
    public void refreshRecentChat(){
        inviteNotices=null;
        inviteNotices = new ArrayList<ChatRecordInfo>();
        for (ChatRecordInfo item : messageMgr.getRecentContactsWithLastMsg()) {
            inviteNotices.add(item);
        }
        //按时间早晚排序
        Collections.sort(inviteNotices);
        recentChatAdapter = new RecentChatAdapter(MainActivity.this,
                R.layout.recent_chat_item, inviteNotices);
        RecentChatListView.setAdapter(recentChatAdapter);
    }
    //刷新好友列表
    public void refreshFriendList(){
        friendadapter=new FriendAdapter(context,R.layout.friend_list_item,
                ContacterManager.getBothContacterList());
        friendListView.setAdapter(friendadapter);
        friendadapter.notifyDataSetChanged();
    }
    //刷新个人信息列表
    public void refreshPersonalInfo(){
        tv_username.setText(currentUser.getUsername());
        tv_nickname.setText(currentUser.getNickName());
        image.setImageBitmap(currentUser.getIcon());
        if (StringUtil.empty(currentUser.getSign()))
            tv_sign.setText("还未设置签名哦！");
        else
            tv_sign.setText(currentUser.getSign());
        if(StringUtil.empty(currentUser.getCountry())) {
            tv_place.setText("还未设置地区哦！");
        }else if("中国".equals(currentUser.getCountry()))
        {
            tv_place.setText(loginConfig.getProvince()+" "+loginConfig.getCity());
        }else
        {
            tv_place.setText(loginConfig.getCountry());
        }
    }

    /**
     * 有新消息进来
     */
    protected void msgReceive(Notice notice) {
        boolean isNew=true;
        if(notice!=null){
            for (ChatRecordInfo ch : inviteNotices) {
                if (ch.getFrom().equals(notice.getFrom())) {
                    ch.setContent(notice.getContent());
                    ch.setNoticeTime(notice.getNoticeTime());
                    Integer x = ch.getNoticeSum() == null ? 0 : ch.getNoticeSum();
                    ch.setNoticeSum(x + 1);
                    isNew=false;
                }
            }
            if(isNew){
                ChatRecordInfo newMsg=new ChatRecordInfo();
                newMsg.setFrom(notice.getFrom());
                newMsg.setContent(notice.getContent());
                newMsg.setNoticeTime(notice.getNoticeTime());
                newMsg.setTitle(notice.getTitle());
                newMsg.setNoticeType(notice.getStatus());
                newMsg.setNoticeSum(1);
                inviteNotices.add(newMsg);
            }
        }
        Collections.sort(inviteNotices);
        recentChatAdapter.notifyDataSetChanged();
    }

    private void refresh() {
//        inviteNotices = noticeManager.getNoticeListByTypeAndPage(Notice.All);

//        noticeAdapter.setNoticeList(inviteNotices);
//        noticeAdapter.notifyDataSetChanged();


//        inviteNotices = MessageManager.getInstance(context)
//                .getRecentContactsWithLastMsg();
//        recentChatAdapter.setNoticeList(inviteNotices);
        recentChatAdapter.notifyDataSetChanged();
    }

    private OnClickListener inviteListClick = new OnClickListener() {

        @Override
        public void onClick(View view) {
            final Notice notice = (Notice) view.findViewById(R.id.new_content)
                    .getTag();
            // 消息类型判断
            if (Notice.ADD_FRIEND == notice.getNoticeType()
                    && notice.getStatus() == Notice.UNREAD) {// 添加好友
                showAddFriendDialag(notice);
            } else if (Notice.SYS_MSG == notice.getNoticeType()) {// 系统通知
                Intent intent = new Intent(context,
                        SystemNoticeDetailActivity.class);
                intent.putExtra("notice_id", notice.getId());
                startActivityForResult(intent, 1);
            }

        }
    };

    /**
     * .弹出添加好友的对话框
     *
     * @param notice
     */
    private void showAddFriendDialag(final Notice notice) {
        final String subFrom = notice.getFrom();
        new AlertDialog.Builder(context)
                .setMessage(subFrom + "请求添加您为好友")
                .setTitle("提示")
                .setPositiveButton("添加", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 接受请求
                        sendSubscribe(Presence.Type.subscribed, subFrom);
                        sendSubscribe(Presence.Type.subscribe, subFrom);
                        // removeInviteNotice(notice.getId());
                        NoticeManager noticeManager = NoticeManager
                                .getInstance(context);
                        noticeManager.updateAddFriendStatus(
                                notice.getId(),
                                Notice.READ,
                                "已经同意"
                                        + StringUtil.getUserNameByJid(notice
                                        .getFrom()) + "的好友申请");
                        refresh();
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendSubscribe(Presence.Type.unsubscribe, subFrom);
                        // removeInviteNotice(notice.getId());
                        NoticeManager noticeManager = NoticeManager
                                .getInstance(context);
                        noticeManager.updateAddFriendStatus(
                                notice.getId(),
                                Notice.READ,
                                "已经拒绝"
                                        + StringUtil.getUserNameByJid(notice
                                        .getFrom()) + "的好友申请");
                        refresh();
                    }
                }).show();
    }

    //获取本地头像
    private Bitmap getLocalAvatar() {
        //默认image路径
        String imageDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath() +
                context.getResources().getString(R.string.img_dir) + "/";
        File dirfile = new File(imageDir);

        String fileName = "avatar_" + StringUtil.getJidByName
                (loginConfig.getUsername(), loginConfig.getServerName()) + ".png";
        byte[] bytes = null;
        try {
            if (!dirfile.exists()) {
                dirfile.mkdirs();
            }

            File file = new File(imageDir, fileName);
            if (file.exists()) {
                try {
                    FileInputStream in = new FileInputStream(file);
                    bytes = new byte[(int) file.length()];
                    in.read(bytes);
                    in.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else
            {
                //todo 不存在localAvatar（不确定要做）
                VCard vCard=new VCard();
                vCard.load(XmppConnectionManager.getInstance().getConnection());
                AppUtil.cachedAvatarImage(context,FormatUtil.Bytes2Bitmap(vCard.getAvatar()),
                        StringUtil.getJidByName(loginConfig.getUsername(),loginConfig.getServerName()));
                return  FormatUtil.Bytes2Bitmap(vCard.getAvatar());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Bitmap bitmap=FormatUtil.drawable2Bitmap(context.getResources().
                    getDrawable(R.drawable.default_icon));
            return bitmap;
        }
        return FormatUtil.Bytes2Bitmap(bytes);
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

        @Override
        public void onBackPressed(){

            if (System.currentTimeMillis() - startTime >= 2000) {
                showToast("再按一次退出程序！");
                startTime = System.currentTimeMillis();
            } else {
                isExit();
            }
        }

}