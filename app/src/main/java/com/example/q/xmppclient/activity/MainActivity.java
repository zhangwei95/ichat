package com.example.q.xmppclient.activity;

import android.Manifest;
import android.animation.TypeConverter;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
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
import com.example.q.xmppclient.db.DataBaseHelper;
import com.example.q.xmppclient.db.SQLiteTemplate;
import com.example.q.xmppclient.entity.ChatRecordInfo;
import com.example.q.xmppclient.entity.FriendList;
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
import com.example.q.xmppclient.util.ValidataUtil;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.pubsub.EventElement;
import org.jivesoftware.smackx.pubsub.GetItemsRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends ActivityTool {

    //common
    public static User sendMsgTo;
    Toolbar toolbar;
    public static LoginConfig loginConfig;
    public static User currentUser;//当前登录的用户
    myViewPager viewpager;
    BottomNavigationView navigation;
    private MenuItem menuItem;
    private long startTime=0;//保存第一次点击返回键的时间
    private MessageManager messageMgr;
    private XMPPConnection xmppConnection;
    private NoticeManager noticeManager;

    //PersonalInfo
    View PersonalInfoView;
    private LinearLayout personInfoll, ll_icon, ll_nick, ll_user, ll_place, ll_sign;
    private ImageView image;
    TextView tv_nickname, tv_username, tv_place, tv_sign;
    //FriendList
    View FriendListView;
    private LinearLayout friendListll;
    private ListView friendListView;
    private LinearLayout useraddll;
    private LinearLayout groupll;
    private FriendAdapter friendadapter;

    //RecentChat
    View ChatRecentView;
    private LinearLayout chatInfoll;
    private ListView RecentChatListView;
    private ContacterReceiver receiver = null;


    private NoticeAdapter noticeAdapter = null;

    private RecentChatAdapter recentChatAdapter;
    private List<ChatRecordInfo> inviteNotices = new ArrayList<ChatRecordInfo>();


    @Override
    protected void onPause() {


        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // 卸载广播接收器
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        inviteNotices = new ArrayList<ChatRecordInfo>();
//        for(Notice item : noticeManager.getNoticeListByTypeAndPage(Notice.UNREAD))
//        {
//            ChatRecordInfo newNotice=new ChatRecordInfo();
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
        //按时间早晚排序
        Collections.sort(inviteNotices, new Comparator<ChatRecordInfo>() {
            @Override
            public int compare(ChatRecordInfo o1, ChatRecordInfo o2) {
                Date date1 = DateUtil.str2Date(o1.getNoticeTime(), Constant.MIDTIME_FORMART);
                Date date2 = DateUtil.str2Date(o2.getNoticeTime(), Constant.MIDTIME_FORMART);
                if (date1.before(date2)) {
                    return 1;
                }
                return -1;
//                return o2.getNoticeTime().compareTo(o1.getNoticeTime());
            }
        });
        friendadapter = new FriendAdapter(MainActivity.this,
                R.layout.friend_list_item, ContacterManager.getContacterList());
        friendadapter.notifyDataSetChanged();
        friendListView.setAdapter(friendadapter);
        recentChatAdapter = new RecentChatAdapter(MainActivity.this,
                R.layout.recent_chat_item, inviteNotices);
        RecentChatListView.setAdapter(recentChatAdapter);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        // 好友请求
        filter.addAction(Constant.ROSTER_SUBSCRIPTION);
        filter.addAction(Constant.ACTION_SYS_MSG);
        filter.addAction(Constant.NEW_MESSAGE_ACTION);
        registerReceiver(receiver, filter);

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
                        startActivity(reloginIntent);
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
                R.layout.friend_list_item, ContacterManager.getContacterList());
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
        useraddll = (LinearLayout) FriendListView.findViewById(R.id.ll_useradd);
        groupll = (LinearLayout) FriendListView.findViewById(R.id.ll_groupadd);
        useraddll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    useraddll.setBackgroundColor(getResources().getColor(R.color.gary));

                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    useraddll.setBackground(getResources().getDrawable(R.drawable.border_bg));
                    Intent intent = new Intent(MainActivity.this, UserAddActivity.class);
                    startActivity(intent);

                }
                return true;
            }
        });
        groupll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    groupll.setBackgroundColor(getResources().getColor(R.color.gary));

                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    groupll.setBackground(getResources().getDrawable(R.drawable.border_bg));
                    Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                    startActivity(intent);

                }
                return true;
            }
        });
    }

    /**
     * 初始化最近联系人界面
     * @author zw
     */
    public void initRecentChatInfo() {
        receiver = new ContacterReceiver();
        RecentChatListView = (ListView) ChatRecentView.findViewById(R.id.listview_chatList);
        for (Notice item : noticeManager.getNoticeListByTypeAndPage(Notice.UNREAD)) {
            ChatRecordInfo newNotice = new ChatRecordInfo();
            newNotice.setContent(item.getContent());
            newNotice.setFrom(item.getFrom());
            newNotice.setNoticeTime(item.getNoticeTime());
            newNotice.setTitle(item.getTitle());
            newNotice.setStatus(item.getStatus());
            inviteNotices.add(newNotice);
        }
        for (ChatRecordInfo item : messageMgr.getRecentContactsWithLastMsg()) {
            inviteNotices.add(item);
        }

        recentChatAdapter = new RecentChatAdapter(context, R.layout.recent_chat_item,
                inviteNotices);
        RecentChatListView.setAdapter(recentChatAdapter);
        RecentChatListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatRecordInfo chatRecordInfo = (ChatRecordInfo) parent.getItemAtPosition(position);
                User user = ContacterManager.getByUserJid(MainActivity.this,
                        chatRecordInfo.getFrom(),xmppConnection);
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
        currentUser = new User();
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
//        ll_icon.setOnClickListener(touch);
//        ll_nick.setOnClickListener(touch);
//        ll_user.setOnClickListener(touch);
//        ll_place.setOnClickListener(touch);
//        ll_sign.setOnClickListener(touch);
        ll_icon.setOnTouchListener(touch);
        ll_nick.setOnTouchListener(touch);
        ll_user.setOnTouchListener(touch);
        ll_place.setOnTouchListener(touch);
        ll_sign.setOnTouchListener(touch);
        try {
            VCard vCard = new VCard();
            currentUser.setJid(StringUtil.getJidByName(loginConfig.getUsername(),
                    loginConfig.getServerName()));
            vCard.load(xmppConnection, currentUser.getJid());
            if (vCard != null) {
                currentUser.setVCard(vCard);
            }
            tv_username.setText(loginConfig.getUsername());
            currentUser.setUsername(loginConfig.getUsername());
            if (vCard == null || vCard.getNickName() == null) {
                tv_nickname.setText(loginConfig.getUsername());
                currentUser.setNickName(loginConfig.getUsername());
            } else {
                tv_nickname.setText(vCard.getNickName());
                currentUser.setNickName(vCard.getNickName());
            }
            if (vCard == null || vCard.getAvatar() == null) {

                currentUser.setIcon(FormatUtil.drawable2Bitmap
                        (context.getResources().getDrawable(R.drawable.icon)));
                image.setImageBitmap(currentUser.getIcon());

            } else {
                ByteArrayInputStream bais = new ByteArrayInputStream(vCard.getAvatar());
                currentUser.setIcon(FormatUtil.getInstance().InputStream2Bitmap(bais));
                image.setImageBitmap(currentUser.getIcon());
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppUtil.syncIsDebug(getApplicationContext());
        loginConfig = getLoginConfig();
        LoginTask loginTask=new LoginTask(this,loginConfig);
        loginTask.execute();
        messageMgr=MessageManager.getInstance(context);
        noticeManager = NoticeManager.getInstance(context);
        xmppConnection=XmppConnectionManager.getInstance().getConnection();
        init();
        initActionBar();
        initFriendList();
        initRecentChatInfo();
        initPersonInfo();
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //默认 >3 的选中效果会影响ViewPager的滑动切换时的效果，故利用反射去掉
        BottomNavigationViewHelper.disableShiftMode(navigation);
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
                        ll_icon.setBackground(getResources().getDrawable(R.drawable.border_bg));
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.avatar");
                        startActivity(intent);
                        ll_icon.setBackground(getResources().getDrawable(R.drawable.border_bg));
                    }
                    return true;
                case R.id.ll_nick:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ll_nick.setBackgroundColor(getResources().getColor(R.color.touchevent));
                    }
                    if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        ll_nick.setBackground(getResources().getDrawable(R.drawable.border_bg));
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        ll_nick.setBackground(getResources().getDrawable(R.drawable.border_bg));
                    }
                    return true;
                case R.id.ll_user:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ll_user.setBackgroundColor(getResources().getColor(R.color.touchevent));
                    }
                    if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        ll_user.setBackground(getResources().getDrawable(R.drawable.border_bg));
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        ll_user.setBackground(getResources().getDrawable(R.drawable.border_bg));
                    }
                    return true;
                case R.id.ll_place:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ll_place.setBackgroundColor(getResources().getColor(R.color.touchevent));
                    }
                    if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        ll_place.setBackground(getResources().getDrawable(R.drawable.border_bg));
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        ll_place.setBackground(getResources().getDrawable(R.drawable.border_bg));
                    }
                    return true;

                case R.id.ll_sign:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ll_sign.setBackgroundColor(getResources().getColor(R.color.touchevent));
                    }
                    if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        ll_sign.setBackground(getResources().getDrawable(R.drawable.border_bg));
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        ll_sign.setBackground(getResources().getDrawable(R.drawable.border_bg));
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
            Notice notice = (Notice) intent.getSerializableExtra("notice");
            // String action = intent.getAction();
//            inviteNotices.add(notice);
            msgReceive(notice);
            refresh();
        }
    }

    /**
     * 有新消息进来
     */
    protected void msgReceive(Notice notice) {
        int checkadd = 0;
        for (ChatRecordInfo ch : inviteNotices) {
            if (ch.getFrom().equals(notice.getFrom())) {
                ch.setContent(notice.getContent());
                ch.setNoticeTime(notice.getNoticeTime());
                Integer x = ch.getNoticeSum() == null ? 0 : ch.getNoticeSum();
                ch.setNoticeSum(x + 1);
                checkadd++;
            }
        }
        //不在最近联系人列表内的notice，生成新的chatrecordInfo加到列表里
//        if (checkadd==0)
//        {
//            ChatRecordInfo newNotice=new ChatRecordInfo();
//            newNotice.setContent(notice.getContent());
//            newNotice.setFrom(notice.getFrom());
//            newNotice.setNoticeTime(notice.getNoticeTime());
//            newNotice.setTitle(notice.getTitle());
//            newNotice.setStatus(notice.getStatus());
//            inviteNotices.add(newNotice);
//        }
        Collections.sort(inviteNotices, new Comparator<ChatRecordInfo>() {
            @Override
            public int compare(ChatRecordInfo o1, ChatRecordInfo o2) {
                Date date1 = DateUtil.str2Date(o1.getNoticeTime(), Constant.MIDTIME_FORMART);
                Date date2 = DateUtil.str2Date(o2.getNoticeTime(), Constant.MIDTIME_FORMART);
                if (date1.before(date2)) {
                    return 1;
                }
                return -1;
//                return o2.getNoticeTime().compareTo(o1.getNoticeTime());
            }
        });
        recentChatAdapter.setNoticeList(inviteNotices);
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
    public void onBackPressed() {

        if(System.currentTimeMillis()-startTime >= 2000)
        {
            showToast("再按一次退出程序！");
            startTime = System.currentTimeMillis();
        }else
        {
          isExit();
        }
    }
}