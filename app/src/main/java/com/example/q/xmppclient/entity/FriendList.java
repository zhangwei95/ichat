package com.example.q.xmppclient.entity;

import com.example.q.xmppclient.entity.User;
import com.example.q.xmppclient.manager.XmppConnectionManager;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 好友列表
 * Created by q on 2017/10/17.
 */

public class FriendList {

    public ArrayList<User> getFriendList() {
        return friendList;
    }
    private ArrayList<User> friendList = new ArrayList<User>();

    public FriendList()
    {
        Roster roster= XmppConnectionManager.getInstance().getConnection().getRoster();
        Collection<RosterGroup> entriesGroup=roster.getGroups();
        for(RosterGroup group:entriesGroup)
        {
            Collection<RosterEntry> entries=group.getEntries();

            for (RosterEntry entry:entries)
            {
                addFriend(new User(entry.getUser()));
            }
        }
    }

    public void addFriend(User user)
    {
        friendList.add(user);
//        if (friendList.contains(user)) {
//            return true;
//        } else {
//            return false;
//        }
    }
    public boolean removeFriendList(User user) {
        friendList.remove(user);
        if (friendList.contains(user)) {
            return false;
        } else {
            return true;
        }
    }


}
