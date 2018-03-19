package com.example.q.xmppclient.manager;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.db.DBManager;
import com.example.q.xmppclient.db.SQLiteTemplate;
import com.example.q.xmppclient.entity.ChatRecordInfo;
import com.example.q.xmppclient.entity.Notice;
import com.example.q.xmppclient.entity.User;
import com.example.q.xmppclient.util.FormatUtil;
import com.example.q.xmppclient.util.StringUtil;
import com.mob.wrappers.UMSSDKWrapper;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by q on 2018/1/11.
 */

public class ContacterManager {
    private static DBManager manager = null;
    /**
     * 保存着所有的联系人信息
     */
    public static Map<String, User> contacters = null;

    public static void init(Context context,XMPPConnection connection) {
        SharedPreferences sharedPre = context.getSharedPreferences(
                Constant.LOGIN_SET, Context.MODE_PRIVATE);
        String databaseName = sharedPre.getString(Constant.USERNAME, null);
        manager = DBManager.getInstance(context, databaseName);
        contacters = new HashMap<String, User>();
        SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);

        for (RosterEntry entry : connection.getRoster().getEntries()) {
            //本地如果存在，直接在本地查出来，否则就去服务器上获取并在本地缓存头像
            if (st.isExistsByField("im_contactors", "jid", entry.getUser())) {
                contacters.put(entry.getUser(),
                        getUserByJidSql(entry.getUser()));
            } else {
                User user;
                user = transEntryToUser(entry, connection);
                String imageDir = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() +"/ichat/images/";
                ContentValues values = new ContentValues();
                values.put(Constant.JID, user.getJid());
                values.put(Constant.NICKNAME, user.getNickName());
                values.put(Constant.AVATAR, imageDir + "avatar_" + user.getJid() + ".png");
                values.put(Constant.COUNTRY,user.getCountry());
                values.put(Constant.PROVINCE,user.getProvince());
                values.put(Constant.CITY,user.getCity());
                values.put(Constant.SIGN,user.getSign());
                st.insert("im_contactors", values);
                File dirfile = new File(imageDir);
                //获取内部存储状态
                String state = Environment.getExternalStorageState();
                //如果状态不是mounted，无法读写
                if (!state.equals(Environment.MEDIA_MOUNTED)) {
                    return;
                }
                    //在本地缓存图片
                    String fileName = "avatar_" + user.getJid()+".png";
                    Bitmap mBitmap = user.getIcon();
                    try {
                        if (!dirfile.exists()) {
                            dirfile.mkdirs();
                        }
                        File file = new File(imageDir, fileName);
                        FileOutputStream out = new FileOutputStream(file);
                        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    contacters.put(entry.getUser(),
                            user);
                }
            }
        }
    /**
     * 根据用户jid得到用户
     *
     * @param userJId
     */
    public static User getByUserJid(String userJId, XMPPConnection connection) {
        Roster roster = connection.getRoster();
        RosterEntry entry = connection.getRoster().getEntry(userJId);
        if (null == entry) {
            return null;
        }
        User user = new User();
        if (entry.getName() == null) {
            user.setUsername(StringUtil.getUserNameByJid(entry.getUser()));
        } else {
            user.setUsername(entry.getName());
        }
        user.setJid(entry.getUser());
        System.out.println(entry.getUser());
        Presence presence = roster.getPresence(entry.getUser());
        try {
            VCard vCard = new VCard();
            vCard.load(XmppConnectionManager.getInstance().getConnection(),userJId);
            user.setNickName(vCard.getNickName());
            user.setVCard(vCard);
            user.setProvince(vCard.getAddressFieldHome(Constant.PROVINCE));
            user.setCity(vCard.getAddressFieldHome(Constant.CITY));
            user.setSign(vCard.getAddressFieldHome(Constant.SIGN));
            user.setCountry(vCard.getAddressFieldHome(Constant.COUNTRY));
            user.setIcon(FormatUtil.Bytes2Bitmap(vCard.getAvatar()));
        }catch (XMPPException e)
        {
            e.printStackTrace();
        }
        return user;

    }

    public static void destroy() {
        contacters = null;
    }
    /**
     * 获得所有的联系人列表
     *
     * @return
     */
    public static List<User> getContacterList() {
        List<User> userList = new ArrayList<User>();
        if (contacters == null ||contacters.size()==0) {
            userList=getContacterFromLocal();
            return userList;
        }

        for (String key : contacters.keySet())
            userList.add(contacters.get(key));


        return userList;
    }

    /**
     * 本地获取contacters
     * @return
     */
    public static List<User> getContacterFromLocal()
    {
        SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
        List<User> userList = st
                .queryForList(
                        new SQLiteTemplate.RowMapper<User>() {

                            @Override
                            public User mapRow(Cursor cursor, int index) {
                                User user = new User();
                                user.setJid(cursor.getString(cursor
                                        .getColumnIndex(Constant.JID)));
                                user.setNickName(cursor.getString(cursor
                                        .getColumnIndex(Constant.NICKNAME)));
                                user.setIcon(getBitmapFromAvatar(cursor.getString(cursor
                                        .getColumnIndex(Constant.AVATAR))));
                                user.setCountry(cursor.getString(cursor
                                        .getColumnIndex(Constant.COUNTRY)));
                                user.setProvince(cursor.getString(cursor
                                        .getColumnIndex(Constant.PROVINCE)));
                                user.setCity(cursor.getString(cursor
                                        .getColumnIndex(Constant.CITY)));
                                user.setSign(cursor.getString(cursor
                                        .getColumnIndex(Constant.SIGN)));
                                return user;
                            }
                        },
                        "select * from im_contactors",
                        null);

        return userList;
    }
    /**
     * 获得所有未分组的联系人列表
     *
     * @return
     */
    public static List<User> getNoGroupUserList(Roster roster) {
        List<User> userList = new ArrayList<User>();

        // 服务器的用户信息改变后，不会通知到unfiledEntries
        for (RosterEntry entry : roster.getUnfiledEntries()) {
            userList.add(contacters.get(entry.getUser()).clone());
        }

        return userList;
    }
    /**
     * 获得所有分组联系人
     *
     * @return
     */
    public static List<MRosterGroup> getGroups(Roster roster) {
        if (contacters == null)
            throw new RuntimeException("contacters is null");

        List<MRosterGroup> groups = new ArrayList<ContacterManager.MRosterGroup>();
        groups.add(new MRosterGroup(Constant.ALL_FRIEND, getContacterList()));
        for (RosterGroup group : roster.getGroups()) {
            List<User> groupUsers = new ArrayList<User>();
            for (RosterEntry entry : group.getEntries()) {
                groupUsers.add(contacters.get(entry.getUser()));
            }
            groups.add(new MRosterGroup(group.getName(), groupUsers));
        }
        groups.add(new MRosterGroup(Constant.NO_GROUP_FRIEND,
                getNoGroupUserList(roster)));
        return groups;
    }

    /**
     * 根据RosterEntry创建一个User
     *
     * @param entry
     * @return
     */
    public static User transEntryToUser(RosterEntry entry,XMPPConnection connection) {
        User user = new User();
        if (entry.getName() == null) {
            user.setUsername(StringUtil.getUserNameByJid(entry.getUser()));
        } else {
            user.setUsername(entry.getName());
        }
        user.setJid(entry.getUser());
        System.out.println(entry.getUser());
        Presence presence = connection.getRoster().getPresence(entry.getUser());
        user=getByUserJid(entry.getUser(),connection);
//        user.setFrom(presence.getFrom());
//        user.setStatus(presence.getStatus());
//        user.setSize(entry.getGroups().size());
//        user.setAvailable(presence.isAvailable());
//        user.setType(entry.getType());

        return user;
    }

    /**
     * 修改这个好友的昵称
     *
     * @param user
     * @param nickname
     */
    public static void setNickname(User user, String nickname,
                                   XMPPConnection connection) {
        RosterEntry entry = connection.getRoster().getEntry(user.getJid());

        entry.setName(nickname);
    }
    /**
     * 把一个好友添加到一个组中
     *
     * @param user
     * @param groupName
     */
    public static void addUserToGroup(final User user, final String groupName,
                                      final XMPPConnection connection) {
        if (groupName == null || user == null)
            return;
        // 将一个rosterEntry添加到group中是PacketCollector，会阻塞线程
        new Thread() {
            public void run() {
                RosterGroup group = connection.getRoster().getGroup(groupName);
                // 这个组已经存在就添加到这个组，不存在创建一个组
                RosterEntry entry = connection.getRoster().getEntry(
                        user.getJid());
                try {
                    if (group != null) {
                        if (entry != null)
                            group.addEntry(entry);
                    } else {
                        RosterGroup newGroup = connection.getRoster()
                                .createGroup(groupName);
                        if (entry != null)
                            newGroup.addEntry(entry);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 把一个好友从组中删除
     *
     * @param user
     * @param groupName
     */
    public static void removeUserFromGroup(final User user,
                                           final String groupName, final XMPPConnection connection) {
        if (groupName == null || user == null)
            return;
        new Thread() {
            public void run() {
                RosterGroup group = connection.getRoster().getGroup(groupName);
                if (group != null) {
                    try {
                        System.out.println(user.getJid() + "----------------");
                        RosterEntry entry = connection.getRoster().getEntry(
                                user.getJid());
                        if (entry != null)
                            group.removeEntry(entry);
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public static class MRosterGroup {
        private String name;
        private List<User> users;

        public MRosterGroup(String name, List<User> users) {
            this.name = name;
            this.users = users;
        }

        public int getCount() {
            if (users != null)
                return users.size();
            return 0;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<User> getUsers() {
            return users;
        }

        public void setUsers(List<User> users) {
            this.users = users;
        }

    }

    /**
     *
     * 根据jid获得用户昵称
     *
     * @param Jid
     * @param connection
     * @return
     */
//    public static User getNickname(String Jid, XMPPConnection connection) {
//        Roster roster = connection.getRoster();
//        for (RosterEntry entry : roster.getEntries()) {
//            String params = entry.getUser();
//            if (params.split("/")[0].equals(Jid)) {
//                return transEntryToUser(,entry, roster);
//            }
//        }
//        return null;
//
//    }

    /**
     * 添加分组 .
     *
     * @param groupName
     * @param connection
     */
    public static void addGroup(final String groupName,
                                final XMPPConnection connection) {
        if (StringUtil.empty(groupName)) {
            return;
        }

        // 将一个rosterEntry添加到group中是PacketCollector，会阻塞线程
        new Thread() {
            public void run() {

                try {
                    RosterGroup g = connection.getRoster().getGroup(groupName);
                    if (g != null) {
                        return;
                    }
                    connection.getRoster().createGroup(groupName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 获得所有组名
     *
     * @return
     */
    public static List<String> getGroupNames(Roster roster) {

        List<String> groupNames = new ArrayList<String>();
        for (RosterGroup group : roster.getGroups()) {
            groupNames.add(group.getName());
        }
        return groupNames;
    }

    /**
     *
     * 从花名册中删除用户.
     *
     * @param userJid
     */
    public static void deleteUser(String userJid) throws XMPPException {

        Roster roster = XmppConnectionManager.getInstance().getConnection()
                .getRoster();
        RosterEntry entry = roster.getEntry(userJid);
        XmppConnectionManager.getInstance().getConnection().getRoster()
                .removeEntry(entry);

    }

    /**
     *
     * 从数据库中通过jid查找User
     *
     * @param Jid
     */
    public static User getUserByJidSql(String Jid) {
        SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
        return st.queryForObject(new SQLiteTemplate.RowMapper<User>() {

            @Override
            public User mapRow(Cursor cursor, int index) {
                User user = new User();
                user.setJid(cursor.getString(cursor
                        .getColumnIndex(Constant.JID)));
                user.setNickName(cursor.getString(cursor
                        .getColumnIndex(Constant.NICKNAME)));
                user.setCountry(cursor.getString(cursor
                        .getColumnIndex(Constant.COUNTRY)));
                user.setProvince(cursor.getString(cursor
                        .getColumnIndex(Constant.PROVINCE)));
                user.setCity(cursor.getString(cursor
                        .getColumnIndex(Constant.CITY)));
                user.setSign(cursor.getString(cursor
                        .getColumnIndex(Constant.SIGN)));
                try {
                    File avatar=new File(cursor.getString(cursor.getColumnIndex(Constant.AVATAR)));
                    FileInputStream stream = new FileInputStream(avatar);
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);
                    stream.close();
                    FormatUtil formatUtil=FormatUtil.getInstance();
                    user.setIcon(bitmap);
                }catch (IOException e)
                {
                    //todo 不存在图片
                }
                return user;
            }

        }, "select * from im_contactors where jid=?", new String[] { Jid });
    }
    public static Bitmap getBitmapFromAvatar(String avatar) {
        try {
            File avatarFile=new File(avatar);
            FileInputStream stream = new FileInputStream(avatarFile);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            stream.close();
            FormatUtil formatUtil=FormatUtil.getInstance();
            return bitmap;
        }catch (IOException e)
        {
            //todo 不存在图片
        }
        return null;
    }



}
