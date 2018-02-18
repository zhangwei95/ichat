package com.example.q.xmppclient.entity;

import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.util.DateUtil;

import java.util.Date;

/**
 * Created by q on 2017/11/5.
 */

/**
 * 消息对象
 * 实现接口Parcelable支持序列化，Comparable实现自定义对象的排序功能（compareTo()）
 */
public class ChatMessage implements Parcelable, Comparable<ChatMessage> {
    public static final String IMMESSAGE_KEY = "immessage.key";
    public static final String KEY_TIME = "immessage.time";
    public static final int SUCCESS = 0;
    public static final int ERROR = 1;
    private int type;
    private String content;
    private String time;
    /**
     * 与谁聊天
     */
    private String fromSubJid;
    /**
     * 0:接受 1：发送
     */
    private int msgType = 0;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFromSubJid() {
        return fromSubJid;
    }

    public void setFromSubJid(String fromSubJid) {
        this.fromSubJid = fromSubJid;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }
    public  ChatMessage()
    {
        this.type=SUCCESS;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(content);
        dest.writeString(time);
        dest.writeString(fromSubJid);
        dest.writeInt(msgType);
    }
    public static final Parcelable.Creator<ChatMessage> CREATOR=new Parcelable.Creator<ChatMessage>() {
                @Override
                public ChatMessage createFromParcel(Parcel source) {
                    ChatMessage chatMessage=new ChatMessage();
                    chatMessage.setType(source.readInt());
                    chatMessage.setContent(source.readString());
                    chatMessage.setTime(source.readString());
                    chatMessage.setFromSubJid(source.readString());
                    chatMessage.setMsgType(source.readInt());
                    return chatMessage;
                }
                @Override
                public ChatMessage[] newArray(int size) {
                    return new ChatMessage[size];
                }
            };
    public ChatMessage(String content,String time,String Jid,int messagetype)
    {
        super();
        this.content=content;
        this.time=time;
        this.fromSubJid=Jid;
        this.msgType=messagetype;
        }

    /**
     *按时间降序排序
     */
    @Override
    public int compareTo(@NonNull ChatMessage o) {
        if(this.getTime()==null||o.getTime()==null)
        {
        return 0;
        }
        String format = null;
        String time1 = "";
        String time2 = "";
        if (this.getTime().length() == o.getTime().length()
                && this.getTime().length() == 23) {
            time1 = this.getTime();
            time2 = o.getTime();
            format = Constant.LONGTIME_FORMART;
        } else {
            time1 = this.getTime().substring(0, 19);
            time2 = o.getTime().substring(0, 19);
        }
        Date da1 = DateUtil.str2Date(time1, format);
        Date da2 = DateUtil.str2Date(time2, format);
        if (time1.compareTo(time2)<0) {
            return -1;
        }
        if (time2.compareTo(time1)<0) {
            return 1;
        }
        return 0;
//        Date da1 = DateUtil.str2Date(time1, format);
//        Date da2 = DateUtil.str2Date(time2, format);
//        if (da1.compareTo(da2)<0) {
//            return -1;
//        }
//        if (da2.compareTo(da1)<0) {
//            return 1;
//        }
//        return 0;
    }
}
