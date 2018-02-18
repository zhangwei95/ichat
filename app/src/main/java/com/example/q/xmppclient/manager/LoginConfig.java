package com.example.q.xmppclient.manager;

/**
 * Created by q on 2017/10/25.
 */

public class LoginConfig {


    private String xmppIP;
    private int xmppPort;
    private String serverName;
    private String username;
    private String password;

    public boolean isNewUser() {
        return isNewUser;
    }

    public void setNewUser(boolean newUser) {
        isNewUser = newUser;
    }

    private  boolean isNewUser;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    private String nickname;
    private String sessionId;// 会话id
    private boolean isRemember;// 是否记住密码
    private boolean isAutoLogin;// 是否自动登录
    private boolean isOnline;// 用户连接成功connection
    private boolean isFirstStart;// 是否首次启动

    public String getXmppIP() {
        return xmppIP;
    }

    public void setXmppIP(String xmppIP) {
        this.xmppIP = xmppIP;
    }

    public int getXmppPort() {
        return xmppPort;
    }

    public void setXmppPort(int xmppPort) {
        this.xmppPort = xmppPort;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public boolean isRemember() {
        return isRemember;
    }

    public void setRemember(boolean remember) {
        isRemember = remember;
    }

    public boolean isAutoLogin() {
        return isAutoLogin;
    }

    public void setAutoLogin(boolean autoLogin) {
        isAutoLogin = autoLogin;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean isFirstStart() {
        return isFirstStart;
    }

    public void setFirstStart(boolean firstStart) {
        isFirstStart = firstStart;
    }

}
