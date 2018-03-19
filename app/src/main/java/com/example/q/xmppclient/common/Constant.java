package com.example.q.xmppclient.common;

import android.text.StaticLayout;

/**
 * Created by q on 2017/10/26.
 */

public class Constant {
/**
 * 所有的action的监听的必须要以"ACTION_"开头
 *
 */

    /**
     * 花名册有删除的ACTION和KEY
     */
    public static final String ROSTER_DELETED = "roster.deleted";
    public static final String ROSTER_DELETED_KEY = "roster.deleted.key";

    /**
     * 花名册有更新的ACTION和KEY
     */
    public static final String ROSTER_UPDATED = "roster.updated";
    public static final String ROSTER_UPDATED_KEY = "roster.updated.key";

    /**
     * 花名册有增加的ACTION和KEY
     */
    public static final String ROSTER_ADDED = "roster.added";
    public static final String ROSTER_ADDED_KEY = "roster.added.key";

    /**
     * 花名册中成员状态有改变的ACTION和KEY
     */
    public static final String ROSTER_PRESENCE_CHANGED = "roster.presence.changed";
    public static final String ROSTER_PRESENCE_CHANGED_KEY = "roster.presence.changed.key";

    /**
     * 服务器的配置
     */
    public static final String LOGIN_SET = "login_set";// 登录设置
    public static final String USERNAME = "username";// 账户
    public static final String PASSWORD = "password";// 密码
    public static final String XMPP_HOST = "xmpp_host";// 地址
    public static final String XMPP_PORT = "xmpp_port";// 端口
    public static final String XMPP_SEIVICE_NAME = "xmpp_service_name";// 服务名
    public static final String IS_AUTOLOGIN = "isAutoLogin";// 是否自动登录
    public static final String IS_REMEMBER = "isRemember";// 是否记住账户密码
    public static final String IS_FIRSTSTART = "isFirstStart";// 是否首次启动
    public static final String IS_ONLINE="isOnline";//是否在线
    public static final String IS_NEW_USER="isNewUser";//是不是新用户


    //im_contactors 表列名
    public static final String JID = "jid";// 所有好友
    public static final String NICKNAME = "nickname";// 昵称
    public static final String COUNTRY = "country";// 国家
    public static final String PROVINCE = "province";// 省份
    public static final String CITY = "city";// 城市
    public static final String SIGN = "sign";// 签名
    public static final String AVATAR = "avatar";// 签名

    /**
     * 登录结果result
     */
    public static  final int LOGIN_SUCCESS=0;
    public static  final int LOGIN_ERROR=1;
    public static  final int USERNAME_PWD_ERROR=2;
    public static  final int SERVER_UNAVAILABLE=3;
    ;public static  final int UNKNOWN=4;

    /**
     * 登录结果信息反馈
     */
    public static  final String LOGIN_SUCCESS_MESSAGE="登录成功";
    public static  final String LOGIN_ERROR_MESSAGE="登录失败";
    public static  final String USERNAME_PWD_ERROR_MESSAGE="用户名或密码错误！";
    public static  final String SERVER_UNAVAILABLE_MESSAGE="找不到对应服务器，请检测网络连接和服务器";
    ;public static  final String UNKNOWN_MESSAGE="未知错误，连接失败";

    /**
     * 注册返回结果
     */
    public  static final   int REGISTER_SUCCESS=0;
    public  static final   int OTHER_REGISTER_ERROR=1;
    public  static final   int USER_EXIST=2;
    public static  final int REGISTER_SERVER_UNAVAILABLE=3;

    /**
     *注册返回提示信息
     */
    public  static final   String USER_EXIST_MESSAGE="用户名已存在！";


    /**
     * 收到好友邀请请求
     */
    public static final String ROSTER_SUBSCRIPTION = "roster.subscribe";
    public static final String ROSTER_SUB_FROM = "roster.subscribe.from";
    public static final String NOTICE_ID = "notice.id";

    public static final String NEW_MESSAGE_ACTION = "roster.newmessage";
    /**
     * 精确到毫秒
     */
    public static final String LONGTIME_FORMART = "yyyy-MM-dd HH:mm:ss SSS";
    /**
     * 年月日时分秒
     */
    public static final String MIDTIME_FORMART = "yyyy-MM-dd HH:mm:ss";
    /**
     * 月日时分秒
     */
    public static final String SHORTTIME_FORMART = "MM-dd HH:mm:ss";

    /**
     * 我的消息
     */
    public static final String MY_NEWS = "my.news";
    public static final String MY_NEWS_DATE = "my.news.date";


    /**
     * 系统消息
     */
    public static final String ACTION_SYS_MSG = "action_sys_msg";// 消息类型关键字
    public static final String MSG_TYPE = "broadcast";// 消息类型关键字
    public static final String SYS_MSG = "sysMsg";// 系统消息关键字
    public static final String SYS_MSG_DIS = "系统消息";// 系统消息
    public static final String ADD_FRIEND_QEQUEST = "好友请求";// 系统消息关键字
    /**
     * 重连接
     */
    /**
     * 重连接状态acttion
     *
     */
    public static final String ACTION_RECONNECT_STATE = "action_reconnect_state";
    /**
     * 描述冲连接状态的关机子，寄放的intent的关键字
     */
    public static final String RECONNECT_STATE = "reconnect_state";
    /**
     * 描述冲连接，
     */
    public static final boolean RECONNECT_STATE_SUCCESS = true;
    public static final boolean RECONNECT_STATE_FAIL = false;
    /**
     * 是否在线的SharedPreferences名称
     */
    public static final String PREFENCE_USER_STATE = "prefence_user_state";

    //数据库名
    public static  final String DATABASE_NAME="ICHAT";
    /**
     * 好友列表 组名
     */
    public static final String ALL_FRIEND = "所有好友";// 所有好友
    public static final String NO_GROUP_FRIEND = "未分组好友";// 所有好友


}
