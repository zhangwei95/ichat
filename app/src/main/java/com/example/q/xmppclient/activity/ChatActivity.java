package com.example.q.xmppclient.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.adapter.MessageListAdapter;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.entity.ChatMessage;
import com.example.q.xmppclient.entity.MessageList;
import com.example.q.xmppclient.entity.Notice;
import com.example.q.xmppclient.entity.User;
import com.example.q.xmppclient.manager.ContacterManager;
import com.example.q.xmppclient.manager.MessageManager;
import com.example.q.xmppclient.manager.NoticeManager;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.util.StringUtil;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

import java.util.List;

public class ChatActivity extends AChatActivity  {
    Toolbar toolbar;
    Button BtnMsgSend;
    EditText ETinputMsg;
    ListView chatList;
    MessageList list;
    ImageView iv_faceicon;
    ImageView iv_voice;
    ImageView iv_addicon;
    ImageView IV_User_icon;
    private Button listHeadButton;
    private View listHead;
    private int recordCount;
    private MessageListAdapter adapter = null;
    RosterEntry entry;
    LinearLayout tishi;
    TextView showadduser;
    Button chatuseradd;
    private int lastVisibleItemPosition = 0;// 标记上次滑动位置，初始化默认为0
    private boolean scrollFlag = false;// 标记是否滑动

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        to=getIntent().getStringExtra("to");
        initActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initComponent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        to=getIntent().getStringExtra("to");
        initActionBar();
        initComponent();
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
    //初始化活动的控件
    void initComponent() {
        tishi=(LinearLayout)findViewById(R.id.ll_checkadduser);
        tishi.bringToFront();
        showadduser=(TextView)findViewById(R.id.tv_chat_showadduser) ;
        chatuseradd=(Button)findViewById(R.id.btn_chat_add_user);
        chatList = (ListView) findViewById(R.id.lv_chatlist);
        iv_voice=(ImageView)findViewById(R.id.iv_voice);
        iv_faceicon=(ImageView)findViewById(R.id.iv_faceicon);
        iv_addicon=(ImageView)findViewById(R.id.iv_add);
        entry=XmppConnectionManager.getInstance().getConnection().getRoster().getEntry(to);

        //todo 判断好友状态 显示提示信息
        if(entry!=null){
            if(entry.getType()== RosterPacket.ItemType.to){
                //todo 显示是否添加好友
                tishi.setVisibility(View.VISIBLE);
                showadduser.setVisibility(View.VISIBLE);
                chatuseradd.setVisibility(View.VISIBLE);
                chatuseradd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendSubscribe(Presence.Type.subscribed, to);
//                      sendSubscribe(Presence.Type.subscribe, to);
                        // removeInviteNotice(notice.getId());
                        NoticeManager noticeManager = NoticeManager
                                .getInstance(context);
                        noticeManager.updateAddFriendStatus(
                                to,
                                Notice.READ,
                                "已经同意"
                                        + StringUtil.getUserNameByJid(to
                                        + "的好友申请"));
                        getIntent().putExtra("isFriend", true);
                        initComponent();
                    }
                });
            } else if(entry.getType()== RosterPacket.ItemType.from) {
                //todo 显示是否添加好友
                tishi.setVisibility(View.VISIBLE);
                showadduser.setText("等待对方通过好友请求");
                showadduser.setVisibility(View.VISIBLE);
                chatuseradd.setVisibility(View.VISIBLE);
                chatuseradd.setText("等待");
                chatuseradd.setClickable(false);
                tishi.bringToFront();

            }else if(entry.getType()== RosterPacket.ItemType.none) {
                //todo 显示是否添加好友
                tishi.setVisibility(View.VISIBLE);
                showadduser.setVisibility(View.VISIBLE);
                chatuseradd.setVisibility(View.VISIBLE);
                chatuseradd.setText("添加");
                chatuseradd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendSubscribe(Presence.Type.subscribe, to);
//                      sendSubscribe(Presence.Type.subscribe, to);
                        // removeInviteNotice(notice.getId());
                        getIntent().putExtra("isFriend", true);
                        initComponent();
                    }
                });
            }else if(entry.getType()== RosterPacket.ItemType.both) {
                tishi.setVisibility(View.GONE);
            }
        }else{
            //todo 显示是否添加好友
            tishi.setVisibility(View.VISIBLE);
            showadduser.setText("是否添加对方为好友");
            chatuseradd.setText("添加");
            showadduser.setVisibility(View.VISIBLE);
            chatuseradd.setVisibility(View.VISIBLE);
            tishi.bringToFront();
            chatuseradd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendSubscribe(Presence.Type.subscribed, to);
//                    sendSubscribe(Presence.Type.subscribe, to);
                    // removeInviteNotice(notice.getId());
                    NoticeManager noticeManager = NoticeManager
                            .getInstance(context);
                    noticeManager.updateAddFriendStatus(
                            to,
                            Notice.READ,
                            "已经同意"
                                    + StringUtil.getUserNameByJid(to
                                    + "的好友申请"));
                    getIntent().putExtra("isFriend", true);
                    initComponent();
                }
            });
        }
        list = new MessageList();
        chatList.setCacheColorHint(0);
        adapter = new MessageListAdapter(ChatActivity.this, getMessages(),
                chatList, pageSize,chatUser);
        chatList.setAdapter(adapter);
        ETinputMsg = (EditText) findViewById(R.id.et_InputMsg);
        BtnMsgSend = (Button) findViewById(R.id.btn_MsgSend);
//        if (!getIntent().getBooleanExtra("isFriend", true)) {
//
//        }
//        else
//        {
//        LayoutInflater mynflater = LayoutInflater.from(context);
//        listHead = mynflater.inflate(R.layout.chatlistheader, null);
//        listHeadButton = (Button) listHead.findViewById(R.id.buttonChatHistory);
//        listHeadButton.setOnClickListener(chatHistoryCk);
//        }
        ETinputMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length()>0)
                {
                    BtnMsgSend.setVisibility(View.VISIBLE);
                    iv_addicon.setVisibility(View.INVISIBLE);
                }
                if (s.length()==0)
                {
                    BtnMsgSend.setVisibility(View.INVISIBLE);
                    iv_addicon.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        BtnMsgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = ETinputMsg.getText().toString().trim();
                if ("".equals(message)) {
                    Toast.makeText(ChatActivity.this, "不能为空",
                            Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        if(entry!=null&&entry.getType()== RosterPacket.ItemType.both){
                            sendMessage(message);
                            ETinputMsg.setText("");
                        }else{
                            showToast("对方不是您的好友！");
                        }
                    } catch (Exception e) {
                        showToast("信息发送失败");
                        ETinputMsg.setText(message);
                    }
                    closeInput();
                }
            }
        });
            //todo获取历史记录
        chatList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //判断状态
                switch (scrollState) {
                    // 当不滚动时
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:// 是当屏幕停止滚动时
                        scrollFlag = false;
                        // 判断滚动到底部 、position是从0开始算起的
                        if (chatList.getLastVisiblePosition() == (chatList
                                .getCount() - 1)) {
                            //TODO
                        }
                        // 判断滚动到顶部
                        if (chatList.getFirstVisiblePosition() == 0) {
                            //TODO
                            if (addNewMessage()) {
                                resh();
                            } else {
                                Toast.makeText(ChatActivity.this, "没有更多的历史消息", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 滚动时
                        scrollFlag = true;
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        // 当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时，即滚动时
                        scrollFlag = true;
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                //当滑动时
                if (scrollFlag
                        ) {
                    if (firstVisibleItem < lastVisibleItemPosition) {
                        // 上滑
                        //TODO
                    } else if (firstVisibleItem > lastVisibleItemPosition) {
                        // 下滑
                        //TODO
                    } else {
                        return;
                    }
                    lastVisibleItemPosition = firstVisibleItem;//更新位置

                }

            }
        });
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

    //初始化toolbar
    public void initActionBar()
    {
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (!getIntent().getBooleanExtra("isFriend", true)) {
            setTitle(StringUtil.getUserNameByJid(to));
        }else {
            setTitle(chatUser.getNickName());
        }
        toolbar.setOnCreateContextMenuListener(this);
    }
    @Override
    protected void receiveNewMessage(ChatMessage message) {

    }

    @Override
    protected void refreshMessage(List<ChatMessage> messages) {

        adapter.refreshList(messages);
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onResume() {
        //不是好友
        if (!getIntent().getBooleanExtra("isFriend",true))
        {
            super.onResume();
        }
        else {
            super.onResume();
//        recordCount = MessageManager.getInstance(context)
//                .getChatCountWithSb(MainActivity.sendMsgTo.getUsername());
//        if (recordCount <= 0) {
//            listHead.setVisibility(View.GONE);
//        } else {
//            listHead.setVisibility(View.VISIBLE);
//        }
            adapter = new MessageListAdapter(ChatActivity.this, getMessages(),
                    chatList, pageSize,chatUser);
            chatList.setAdapter(adapter);
            adapter.refreshList(getMessages());
        }
     }



}
