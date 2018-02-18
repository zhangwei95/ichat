package com.example.q.xmppclient.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.activity.MainActivity;
import com.example.q.xmppclient.activity.UserInfoActivity;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.entity.Notice;
import com.example.q.xmppclient.entity.User;
import com.example.q.xmppclient.manager.ContacterManager;
import com.example.q.xmppclient.manager.NoticeManager;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.util.DateUtil;
import com.example.q.xmppclient.util.StringUtil;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import java.util.Calendar;
import java.util.Collection;

public class ContactService extends Service {
    private Roster roster = null;
    private Context context;
    /* 声明对象变量 */
    private NotificationManager notificationManager;
    @Override
    public void onCreate() {
        context = this;
        addSubscriptionListener();
        super.onCreate();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        return super.onStartCommand(intent, flags, startId);
    }
    private void init() {
		/* 初始化对象 */
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        initRoster();
    }
    /**
     * 添加一个监听，监听好友添加请求。
     */
    private void addSubscriptionListener() {
        PacketFilter filter = new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                if (packet instanceof Presence) {
                    Presence presence = (Presence) packet;
                    if (presence.getType().equals(Presence.Type.subscribe)) {
                        return true;
                    }
                }
                return false;
            }
        };
        XmppConnectionManager.getInstance().getConnection()
                .addPacketListener(subscriptionPacketListener, filter);
    }
    /**
     * 初始化花名册 服务重启时，更新花名册
     */
    private void initRoster() {
        roster = XmppConnectionManager.getInstance().getConnection()
                .getRoster();
        roster.removeRosterListener(rosterListener);
        roster.addRosterListener(rosterListener);
        ContacterManager.init(context,XmppConnectionManager.getInstance()
                .getConnection());
    }
    private PacketListener subscriptionPacketListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            String user = getSharedPreferences(Constant.LOGIN_SET, 0)
                    .getString(Constant.USERNAME, null);
            if (packet.getFrom().contains(user))
                return;
            // 如果是自动接收所有请求，则回复一个添加信息
            if (Roster.getDefaultSubscriptionMode().equals(
                    Roster.SubscriptionMode.accept_all)) {
                Presence subscription = new Presence(Presence.Type.subscribe);
                subscription.setTo(packet.getFrom());
                XmppConnectionManager.getInstance().getConnection()
                        .sendPacket(subscription);
            } else {
                NoticeManager noticeManager = NoticeManager
                        .getInstance(context);
                Notice notice = new Notice();
                notice.setTitle("好友请求");
                notice.setNoticeType(Notice.ADD_FRIEND);
                notice.setContent(StringUtil.getUserNameByJid(packet.getFrom())
                        + "申请加您为好友");
                notice.setFrom(packet.getFrom());
                notice.setTo(packet.getTo());
                notice.setNoticeTime(DateUtil.date2Str(Calendar.getInstance(),
                        Constant.LONGTIME_FORMART));
                notice.setStatus(Notice.UNREAD);
                long noticeId = noticeManager.saveNotice(notice);
                if (noticeId != -1) {
                    Intent intent = new Intent();
                    intent.setAction(Constant.ROSTER_SUBSCRIPTION);
                    notice.setId("" + noticeId);
                    intent.putExtra("notice", notice);
                    sendBroadcast(intent);
                    setNotiType(R.drawable.icon, "好友请求",
                            StringUtil.getUserNameByJid(packet.getFrom())
                                    + "申请加您为好友", UserInfoActivity.class,notice);
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
                             String contentText, Class activity,Notice notice) {
		/*
		 * 创建新的Intent，作为点击Notification留言条时， 会运行的Activity
		 */
        Intent notifyIntent = new Intent(this, activity);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notifyIntent.putExtra("notice",notice);
		/* 创建PendingIntent作为设置递延运行的Activity */
        PendingIntent appIntent = PendingIntent.getActivity(this, 0,
                notifyIntent, 0);

//		/* 创建Notication，并设置相关参数 */
//        Notification myNoti = new Notification();
//		/* 设置statusbar显示的icon */
//        myNoti.icon = iconId;
//		/* 设置statusbar显示的文字信息 */
//        myNoti.tickerText = contentTitle;
//		/* 设置notification发生时同时发出默认声音 */
//        myNoti.defaults = Notification.DEFAULT_SOUND;
//		/* 设置Notification留言条的参数 */
//        myNoti.setLatestEventInfo(this, contentTitle, contentText, appIntent);
//		/* 送出Notification */
//        myNotiManager.notify(0, myNoti);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(iconId)
                .setTicker(contentTitle)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setAutoCancel(true)
                .build();
        notification.contentIntent=appIntent;
		/* 送出Notification */
        notificationManager.notify(0, notification);
    }

    @Override
    public void onDestroy() {
        // 释放资源
        XmppConnectionManager.getInstance().getConnection()
                .removePacketListener(subscriptionPacketListener);
        ContacterManager.destroy();
        super.onDestroy();
    }
    private RosterListener rosterListener = new RosterListener() {

        @Override
        public void presenceChanged(Presence presence) {
            Intent intent = new Intent();
            intent.setAction(Constant.ROSTER_PRESENCE_CHANGED);
            String subscriber = presence.getFrom().substring(0,
                    presence.getFrom().indexOf("/"));
            RosterEntry entry = roster.getEntry(subscriber);
            if (ContacterManager.contacters.containsKey(subscriber)) {
                // 将状态改变之前的user广播出去
                intent.putExtra(User.userKey,
                        ContacterManager.contacters.get(subscriber));
                ContacterManager.contacters.remove(subscriber);
                ContacterManager.contacters.put(subscriber,
                        ContacterManager.transEntryToUser(context,entry,
                                XmppConnectionManager.getInstance().getConnection()));
            }
            sendBroadcast(intent);
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {
            for (String address : addresses) {
                Intent intent = new Intent();
                intent.setAction(Constant.ROSTER_UPDATED);
                // 获得状态改变的entry
                RosterEntry userEntry = roster.getEntry(address);
                User user = ContacterManager
                        .transEntryToUser(context,userEntry,
                                XmppConnectionManager.getInstance().getConnection());
                if (ContacterManager.contacters.get(address) != null) {
                    // 这里发布的是更新前的user
                    intent.putExtra(User.userKey,
                            ContacterManager.contacters.get(address));
                    // 将发生改变的用户更新到userManager
                    ContacterManager.contacters.remove(address);
                    ContacterManager.contacters.put(address, user);
                }
                sendBroadcast(intent);
                // 用户更新，getEntries会更新
                // roster.getUnfiledEntries中的entry不会更新
            }
        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {
            for (String address : addresses) {
                Intent intent = new Intent();
                intent.setAction(Constant.ROSTER_DELETED);
                User user = null;
                if (ContacterManager.contacters.containsKey(address)) {
                    user = ContacterManager.contacters.get(address);
                    ContacterManager.contacters.remove(address);
                }
                intent.putExtra(User.userKey, user);
                sendBroadcast(intent);
            }
        }

        @Override
        public void entriesAdded(Collection<String> addresses) {
            for (String address : addresses) {
                Intent intent = new Intent();
                intent.setAction(Constant.ROSTER_ADDED);
                RosterEntry userEntry = roster.getEntry(address);
                User user = ContacterManager
                        .transEntryToUser(context,userEntry,
                                XmppConnectionManager.getInstance().getConnection());
                ContacterManager.contacters.put(address, user);
                intent.putExtra(User.userKey, user);
                sendBroadcast(intent);
            }
        }
    };

}
