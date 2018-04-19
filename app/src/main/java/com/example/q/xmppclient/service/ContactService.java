package com.example.q.xmppclient.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.activity.MainActivity;
import com.example.q.xmppclient.activity.UserInfoActivity;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.entity.Notice;
import com.example.q.xmppclient.entity.User;
import com.example.q.xmppclient.manager.ContacterManager;
import com.example.q.xmppclient.manager.MessageManager;
import com.example.q.xmppclient.manager.NoticeManager;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.util.DateUtil;
import com.example.q.xmppclient.util.FormatUtil;
import com.example.q.xmppclient.util.StringUtil;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

import java.util.Calendar;
import java.util.Collection;

import static com.example.q.xmppclient.common.Constant.ROSTER_PRESENCE_CHANGED;

public class ContactService extends Service {
    static String TAG="ContactService";
    private Roster roster = null;
    private Context context;
    String[] group=new String[]{"Friends"};
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
        Log.e(TAG, "onStartCommand: ");
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
                    Log.e(TAG, "accept: presence Type="+presence.toString()+" from="+presence.getFrom());
                    if (presence.getType().equals(Presence.Type.subscribe)) {
                        Log.e(TAG, "accept: subscribe");
                        return true;
                    }
                    if (presence.getType().equals(Presence.Type.unsubscribe)) {
                        Log.e(TAG, "accept: unsubscribe");
                        return true;
                    }
                    if (presence.getType().equals(Presence.Type.unsubscribed)) {
                        Log.e(TAG, "accept: unsubscribed");
                        return true;
                    }
                    if (presence.getType().equals(Presence.Type.subscribed)) {
                        Log.e(TAG, "accept: subscribed");
                        return true;
                    }
                    if (presence.getType().equals(Presence.Type.available)) {
                        Log.e(TAG, "accept: available");
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
    public void createNotify(Packet packet,String content){
        NoticeManager noticeManager = NoticeManager
                .getInstance(context);
        Notice notice = new Notice();
        notice.setTitle("好友通知");
        notice.setNoticeType(Notice.ADD_FRIEND);
        notice.setContent(StringUtil.getUserNameByJid(packet.getFrom())
                + content);
        notice.setFrom(packet.getFrom());
        Log.e(TAG, "processPacket: notify" + notice.getFrom());
        notice.setTo(packet.getTo());
        notice.setNoticeTime(DateUtil.date2Str(Calendar.getInstance(),
                Constant.LONGTIME_FORMART));
        notice.setStatus(Notice.UNREAD);
        long noticeId = noticeManager.saveNotice(notice);
        Log.e(TAG, "noticeID=" + noticeId);
        if (noticeId != -1) {
            Intent intent = new Intent();
            intent.setAction(Constant.ROSTER_SUBSCRIPTION);
            notice.setId("" + noticeId);
            Log.e(TAG, "noticeID=" + notice.getId());
            setNotiType(R.mipmap.i_chat, "好友通知",
                    StringUtil.getUserNameByJid(packet.getFrom())
                            + content, UserInfoActivity.class, notice);
        }else{
            //todo 保存通知和发送广播
        }
    }
    private PacketListener subscriptionPacketListener = new PacketListener() {
        @Override
        public void processPacket(Packet packet) {
            Log.e(TAG, "PacketListener  processPacket packet.getFrom(): "+packet.getFrom());
            String user = getSharedPreferences(Constant.LOGIN_SET, 0)
                    .getString(Constant.USERNAME, null);
            if (StringUtil.getUserNameByJid(packet.getFrom()).equals(user)) {
                return;
            }
            Log.e(TAG, "packet.getFrom().contains(user) false");
            Presence presence = (Presence) packet;
            //状态包更新好友状态
            if(presence.getType().equals(Presence.Type.available)){
                if(roster.getEntry(presence.getFrom())!=null){
                    if(ContacterManager.isExistInDB(presence.getFrom())){
                        Log.e(TAG, "isExistInDB jid="+presence.getFrom()+"entry type="+FormatUtil.ItemType2string(
                                roster.getEntry(presence.getFrom()).getType()));
                        ContacterManager.updateDBFriend(roster.getEntry(presence.getFrom()),
                                roster.getEntry(presence.getFrom()).getUser(),XmppConnectionManager.
                                getInstance().getConnection());
                    }else{
                        Log.e(TAG, "isnotExistInDB jid="+presence.getFrom()+"entry type="+FormatUtil.ItemType2string(
                                roster.getEntry(presence.getFrom()).getType()) );
                        ContacterManager.insertDBFriend(roster.getEntry(presence.getFrom()),
                                roster.getEntry(presence.getFrom()).getUser(),XmppConnectionManager.
                                getInstance().getConnection());
                    }
                    if(ContacterManager.contacters.containsKey(presence.getFrom())){
                        ContacterManager.contacters.put(packet.getFrom(),
                                ContacterManager.transEntryToUser(XmppConnectionManager.getInstance().
                                                getConnection().getRoster().getEntry(packet.getFrom()),
                                        XmppConnectionManager.getInstance().getConnection()));
                    }
                    Intent intent=new Intent();
                    intent.setAction(ROSTER_PRESENCE_CHANGED);
                    sendBroadcast(intent);
                }
                return;
            }
            //收到删除包
            if(presence.getType().equals(Presence.Type.unsubscribe)){
                Log.e(TAG, "before remove entry jid="+packet.getFrom()+" entry type="+
                        (FormatUtil.ItemType2string(roster.getEntry(packet.getFrom()).getType())));
                Roster roster=XmppConnectionManager.getInstance().getConnection().getRoster();
                if(roster.getEntry(presence.getFrom())!=null){
                    try {
                        roster.removeEntry(roster.getEntry(presence.getFrom()));
                        Log.e(TAG, "after remove entry jid="+packet.getFrom()+" entry type="+
                                FormatUtil.ItemType2string(roster.getEntry(packet.getFrom()).getType()));
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                }
                if(roster.getEntry(presence.getFrom())==null||
                        roster.getEntry(presence.getFrom()).getType()!= RosterPacket.ItemType.both) {
                    createNotify(packet,"已将您删除！");
                }
                return;
            }

            if(presence.getType().equals(Presence.Type.unsubscribed)){
                if(roster.getEntry(presence.getFrom()).getType()== RosterPacket.ItemType.from){
                    createNotify(packet,"拒绝了您的好友请求！");
                }
                Roster roster=XmppConnectionManager.getInstance().getConnection().getRoster();
                if(roster.getEntry(presence.getFrom())!=null){
                    try {
                        roster.removeEntry(roster.getEntry(presence.getFrom()));
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }
            if(presence.getType().equals(Presence.Type.subscribed)){
                if(roster.getEntry(presence.getFrom()).getType()== RosterPacket.ItemType.both){
                    createNotify(packet,"接受了您的好友请求！");
                }
                return;
            }

            // 如果是自动接收所有请求，则回复一个添加信息
            if (Roster.getDefaultSubscriptionMode().equals(
                    Roster.SubscriptionMode.accept_all)) {
                Log.e(TAG, "processPacket: Roster.getDefaultSubscriptionMode()=accept_all");
                Presence subscription = new Presence(Presence.Type.subscribed);
                subscription.setTo(packet.getFrom());
                XmppConnectionManager.getInstance().getConnection()
                        .sendPacket(subscription);
                subscription = new Presence(Presence.Type.subscribe);
                subscription.setTo(packet.getFrom());
                XmppConnectionManager.getInstance().getConnection()
                        .sendPacket(subscription);
                if(ContacterManager.isExistInDB(packet.getFrom())){
                    ContacterManager.contacters.remove(packet.getFrom());
                    ContacterManager.contacters.put(packet.getFrom(),
                            ContacterManager.transEntryToUser(XmppConnectionManager.getInstance().
                                            getConnection().getRoster().getEntry(packet.getFrom()),
                                    XmppConnectionManager.getInstance().getConnection()));
                    ContacterManager.updateDBFriend(XmppConnectionManager.getInstance().
                            getConnection().getRoster().getEntry(packet.getFrom()),packet.getFrom(),
                            XmppConnectionManager.getInstance().getConnection());
                }else{
                    ContacterManager.insertDBFriend(XmppConnectionManager.getInstance().
                                    getConnection().getRoster().getEntry(packet.getFrom()),
                            packet.getFrom(), XmppConnectionManager.getInstance().getConnection());
                }
                subscription = new Presence(Presence.Type.available);
                subscription.setTo(packet.getFrom());
                XmppConnectionManager.getInstance().getConnection()
                        .sendPacket(subscription);
            } else if(Roster.getDefaultSubscriptionMode().equals(
                    Roster.SubscriptionMode.manual)){
                Log.e(TAG, "processPacket: Roster.getDefaultSubscriptionMode()=manual");
                //todo 如果我已添加对方，然后收到请求，显示对方已通过你的请求，没有添加则显示对方申请添加你为好友
                if (roster.getEntry(presence.getFrom())!=null&&
                        (roster.getEntry(presence.getFrom()).getType()== RosterPacket.ItemType.from||
                                roster.getEntry(presence.getFrom()).getType()== RosterPacket.ItemType.both)){
                    createNotify(packet,"已通过了您的好友请求！");
                }else{
                    createNotify(packet,"申请加您为好友！");
                }
            }else if (Roster.getDefaultSubscriptionMode().equals(
                    Roster.SubscriptionMode.reject_all)){
                Presence subscription = new Presence(Presence.Type.unsubscribe);
                subscription.setTo(packet.getFrom());
                XmppConnectionManager.getInstance().getConnection()
                        .sendPacket(subscription);
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
     *            内容
     * @param activity
     */
    private void setNotiType(int iconId, String contentTitle,
                             String contentText, Class activity,Notice notice) {
		/*
		 * 创建新的Intent，作为点击Notification留言条时， 会运行的Activity
		 */
        Intent notifyIntent = new Intent(this, activity);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        Bundle bundle = new Bundle();  //创建一个Bundle对象。
//        bundle.putSerializable("notice",notice); //把一个键值数据对添加到Bundle对象中。
//        Log.e(TAG, "noticeID="+notice.getId());
//        notifyIntent.putExtras(bundle);
        notifyIntent.putExtra("to",notice.getFrom());
		/* 创建PendingIntent作为设置递延运行的Activity */
        PendingIntent appIntent = PendingIntent.getActivity(this, 0,
                notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT);

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
                .setDefaults(Notification.DEFAULT_ALL)
                .build();
        notification.contentIntent=appIntent;
		/* 送出Notification */
        notificationManager.notify(1, notification);
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
            Log.e(TAG, "roster Listener presenceChanged: ");
            //接受到好友状态改变信息，修改contactors
            Intent intent = new Intent();
            intent.setAction(ROSTER_PRESENCE_CHANGED);
            String subscriber = presence.getFrom().substring(0,
                    presence.getFrom().indexOf("/"));
            RosterEntry entry = roster.getEntry(subscriber);
            if (ContacterManager.contacters.containsKey(subscriber)) {
                // 将状态改变之前的user广播出去
                intent.putExtra(User.userKey,
                        ContacterManager.contacters.get(subscriber));
                ContacterManager.contacters.remove(subscriber);
                ContacterManager.contacters.put(subscriber,
                        ContacterManager.transEntryToUser(entry,
                                XmppConnectionManager.getInstance().getConnection()));
                //todo 好友状态改变，将好友信息更新缓存到本地
                ContacterManager.updateDBFriend(entry,entry.getUser(),
                        XmppConnectionManager.getInstance().getConnection());

            }
            sendBroadcast(intent);
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {
            Log.e(TAG, "entriesUpdated: " );
            for (String address : addresses) {
                Intent intent = new Intent();
                intent.setAction(Constant.ROSTER_UPDATED);
                // 获得状态改变的entry
                RosterEntry userEntry = roster.getEntry(address);
                User user = ContacterManager
                        .transEntryToUser(userEntry,
                                XmppConnectionManager.getInstance().getConnection());
                if (ContacterManager.contacters.get(address) != null) {
                    // 这里发布的是更新前的user
                    intent.putExtra(User.userKey,
                            ContacterManager.contacters.get(address));
                    // 将发生改变的用户更新到userManager
                    ContacterManager.contacters.remove(address);
                    ContacterManager.contacters.put(address, user);
                }
                //todo 好友状态改变，将好友信息更新缓存到本地
                if(ContacterManager.isExistInDB(address)){
                    Log.e(TAG, "isExistInDB jid="+address+"entry type="+FormatUtil.ItemType2string(
                            userEntry.getType()));
                    ContacterManager.updateDBFriend(userEntry,userEntry.getUser(),XmppConnectionManager.
                            getInstance().getConnection());
                }else{
                    Log.e(TAG, "isnotExistInDB jid="+address+"entry type="+FormatUtil.ItemType2string(
                            userEntry.getType()) );
                    ContacterManager.insertDBFriend(userEntry,userEntry.getUser(),XmppConnectionManager.
                            getInstance().getConnection());
                }
                sendBroadcast(intent);
                // 用户更新，getEntries会更新
                // roster.getUnfiledEntries中的entry不会更新
            }
        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {
            Log.e(TAG, "entriesDeleted: " );
            for (String address : addresses) {
                Intent intent = new Intent();
                intent.setAction(Constant.ROSTER_DELETED);
                User user = null;
                if (ContacterManager.contacters.containsKey(address)) {
                    user = ContacterManager.contacters.get(address);
                    ContacterManager.contacters.remove(address);
                }
                //todo 删除好友 删除数据库信息
                ContacterManager.deleteDBFriend(user.getJid());
                MessageManager.getInstance(context).delChatHisWithSb(user.getJid());
                NoticeManager.getInstance(context).delNoticeHisWithSb(user.getJid());
                intent.putExtra(User.userKey, user);
                sendBroadcast(intent);
            }
        }

        @Override
        public void entriesAdded(Collection<String> addresses) {
            Log.e(TAG, "entriesAdded: " );
            for (String address : addresses) {
                Intent intent = new Intent();
                intent.setAction(Constant.ROSTER_ADDED);
                RosterEntry userEntry = roster.getEntry(address);
                if(userEntry==null){
                    Log.e(TAG, "entriesAdded: userEntry==null" );
                }
                User user = ContacterManager
                        .transEntryToUser(userEntry,
                                XmppConnectionManager.getInstance().getConnection());
                ContacterManager.contacters.put(address, user);
                //todo 添加好友,添加数据库信息
                if(ContacterManager.isExistInDB(address)){
                    Log.e(TAG, "isExistInDB jid="+address+" entry type="+ FormatUtil.ItemType2string(
                            XmppConnectionManager.getInstance().
                                    getConnection().getRoster().getEntry(address).getType()) );
                    ContacterManager.updateDBFriend(userEntry,userEntry.getUser(),
                            XmppConnectionManager.getInstance().getConnection());
                }else{
                    Log.e(TAG, "isnotExistInDB jid="+address+"entry type="+FormatUtil.ItemType2string(
                            XmppConnectionManager.getInstance().
                                    getConnection().getRoster().getEntry(address).getType()) );
                    ContacterManager.insertDBFriend(userEntry,userEntry.getUser(),
                            XmppConnectionManager.getInstance().getConnection());
                }
                intent.putExtra(User.userKey, user);
                sendBroadcast(intent);
            }
        }
    };

}
