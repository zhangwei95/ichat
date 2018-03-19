package com.example.q.xmppclient.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.activity.ChatActivity;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.entity.ChatMessage;
import com.example.q.xmppclient.entity.Notice;
import com.example.q.xmppclient.manager.MessageManager;
import com.example.q.xmppclient.manager.NoticeManager;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.util.DateUtil;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import java.util.Calendar;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class ChatService extends Service {
    private Context context;
    private NotificationManager notificationManager;

    public ChatService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        context = this;
        initChatManager();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    /**
     * 初始化聊天管理器
     */
    private void initChatManager() {
        notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        XMPPConnection xmppConnection= XmppConnectionManager.getInstance().getConnection();
        xmppConnection.addPacketListener(packetListener,new MessageTypeFilter(Message.Type.chat));
    }
    PacketListener packetListener=new PacketListener() {
        @Override
        public void processPacket(Packet packet) {
            Message message=(Message)packet;
            if(message!=null&&message.getBody()!=null&&!message.getBody().equals("null"))
            {
                ChatMessage msg=new ChatMessage();
                String time = DateUtil.date2Str(Calendar.getInstance(),
                        Constant.LONGTIME_FORMART);
                msg.setTime(time);
                msg.setContent(message.getBody());
                if (message.getType()==Message.Type.error)
                {
                    msg.setType(ChatMessage.ERROR);
                }else
                {
                    msg.setType(ChatMessage.SUCCESS);
                }
                String from=message.getFrom().split("/")[0];
                msg.setFromSubJid(from);

                //生成通知
                NoticeManager noticeManager = NoticeManager
                        .getInstance(context);
                Notice notice = new Notice();
                notice.setTitle("会话信息");
                notice.setNoticeType(Notice.CHAT_MSG);
                notice.setContent(message.getBody());
                notice.setFrom(from);
                notice.setStatus(Notice.UNREAD);
                notice.setNoticeTime(time);
                // 历史记录
                ChatMessage newMessage = new ChatMessage();
                newMessage.setMsgType(0);
                newMessage.setFromSubJid(from);
                newMessage.setContent(message.getBody());
                newMessage.setTime(time);
                MessageManager.getInstance(context).saveChatMessage(newMessage);
                long noticeId = -1;

                noticeId = noticeManager.saveNotice(notice);

                if (noticeId != -1) {
                    Intent intent = new Intent(Constant.NEW_MESSAGE_ACTION);
                    intent.putExtra(ChatMessage.IMMESSAGE_KEY, msg);
                    intent.putExtra("notice", notice);
                    sendBroadcast(intent);
                    setNotiType(R.drawable.icon,
                            getResources().getString(R.string.new_message),
                            notice.getContent(), ChatActivity.class, from);
                }
            }

        }
    };

    /**
     *
     * 发出Notification的method.
     *
     * @param iconId
     *            图标
     * @param contentTitle
     *            标题
     * @param contentText
     *            你内容
     * @param activity
     * @author shimiso
     * @update 2012-5-14 下午12:01:55
     */
    private void setNotiType(int iconId, String contentTitle,
                             String contentText, Class activity, String from) {

		/*
		 * 创建新的Intent，作为点击Notification留言条时， 会运行的Activity
		 */
        Intent notifyIntent = new Intent(this, activity);
        notifyIntent.putExtra("to", from);
        // notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		/* 创建PendingIntent作为设置递延运行的Activity */
        PendingIntent appIntent = PendingIntent.getActivity(this, 0,
                notifyIntent, FLAG_UPDATE_CURRENT);

//		/* 创建Notication，并设置相关参数 */
//        Notification myNoti = new Notification();
//        // 点击自动消失
//        myNoti.flags = Notification.FLAG_AUTO_CANCEL;
//		/* 设置statusbar显示的icon */
//        myNoti.icon = iconId;
//		/* 设置statusbar显示的文字信息 */
//        myNoti.tickerText = contentTitle;
//		/* 设置notification发生时同时发出默认声音 */
//        myNoti.defaults = Notification.DEFAULT_SOUND;
//		/* 设置Notification留言条的参数 */
//        myNoti.setLatestEventInfo(this, contentTitle, contentText, appIntent);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(iconId)
                .setTicker(contentTitle)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(from)
                .setContentText(contentText)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();
        notification.contentIntent=appIntent;
		/* 送出Notification */
        notificationManager.notify(0, notification);
    }
}
