package com.example.q.xmppclient.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.entity.ChatMessage;
import com.example.q.xmppclient.entity.MessageList;
import com.example.q.xmppclient.entity.Notice;
import com.example.q.xmppclient.entity.User;
import com.example.q.xmppclient.manager.ContacterManager;
import com.example.q.xmppclient.manager.MessageManager;
import com.example.q.xmppclient.manager.NoticeManager;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.util.DateUtil;
import com.example.q.xmppclient.util.StringUtil;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public abstract class AChatActivity extends ActivityTool {
    private Chat chat = null;
    private List<ChatMessage> message_pool = null;
    private ChatManager chatman=null;
    protected String to;// 聊天人
    protected User chatUser;// 聊天人
    protected static int pageSize = 10;
    private static int pagenum = 1;
//    private List<Notice> noticeList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achat);
        pagenum=1;
        to = getIntent().getStringExtra("to");
        chatUser = ContacterManager.getByUserJid(this,to,XmppConnectionManager.getInstance().getConnection());
        if (to == null)
            return;
        chatman=XmppConnectionManager.getInstance().getConnection()
                .getChatManager();
        chat = chatman.createChat(to, null);
//        chatman.addChatListener(new ChatManagerListener() {
//            @Override
//            public void chatCreated(Chat chat, boolean b) {
//                chat.addMessageListener(new MessageListener() {
//                    @Override
//                    public void processMessage(Chat chat, Message message) {
//                        if(message.getFrom().contains(chatUser.getJid()))
//                        {
//                            receiveNewMessage(message);
//                        }
//                    }
//                });
//            }
//        });
    }

    @Override
    protected void onStart() {
        if(message_pool!=null)
        {
            message_pool.clear();
            pagenum=1;
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        // 第一次查询
        pagenum=1;
        message_pool = MessageManager.getInstance(context)
                .getMessageListByFrom(to, pagenum++, pageSize);
        if (null != message_pool && message_pool.size() > 0)
            Collections.sort(message_pool);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.NEW_MESSAGE_ACTION);
        registerReceiver(receiver, filter);

        // 更新某人所有通知
        NoticeManager.getInstance(context).updateStatusByFrom(to, Notice.READ);
        super.onResume();

    }
    protected void sendMessage(String messageContent) throws Exception {

        String time = DateUtil.date2Str(Calendar.getInstance(),
                Constant.LONGTIME_FORMART);
        Message message = new Message();
        message.setProperty(ChatMessage.KEY_TIME, time);
        message.setBody(messageContent);
        chat.sendMessage(message);

        ChatMessage newMessage = new ChatMessage();
        newMessage.setMsgType(1);
        newMessage.setFromSubJid(chat.getParticipant());
        newMessage.setContent(messageContent);
        newMessage.setTime(time);
        message_pool.add(newMessage);
        MessageManager.getInstance(context).saveChatMessage(newMessage);
        // MChatManager.message_pool.add(newMessage);

        // 刷新视图
        refreshMessage(message_pool);
    }
    protected abstract void refreshMessage(List<ChatMessage> messages);
    protected abstract void receiveNewMessage(ChatMessage message);
    protected List<ChatMessage> getMessages() {
        return message_pool;
    }

    /**
     * 下滑加载消息,true 返回成功，false 数据已经全部加载，全部查完了，
     *
     */
    protected Boolean addNewMessage() {
        List<ChatMessage> newMsgList = MessageManager.getInstance(context)
                .getMessageListByFrom(chatUser.getJid(),pagenum++, pageSize);
        if (newMsgList != null && newMsgList.size() > 0) {
            message_pool.addAll(newMsgList);
            Collections.sort(message_pool);
            return true;
        }
        return false;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constant.NEW_MESSAGE_ACTION.equals(action)) {
                ChatMessage message = intent
                        .getParcelableExtra(ChatMessage.IMMESSAGE_KEY);
                if(message.getFromSubJid().equals(to))
                {
                message_pool.add(message);
                receiveNewMessage(message);
                refreshMessage(message_pool);
                }
            }
        }

    };
    protected void resh() {
        // 刷新视图
        refreshMessage(message_pool);

    }

    @Override
    protected void onPause() {

        super.onPause();
        // 更新某人所有通知
        NoticeManager.getInstance(context).updateStatusByFrom(to, Notice.READ);
    }

    @Override
    protected void onStop() {

        super.onStop();
    }
}
