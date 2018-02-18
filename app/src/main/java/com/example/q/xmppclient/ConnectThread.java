package com.example.q.xmppclient;


import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Handler;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import de.javawi.jstun.attribute.Password;

/**
 * 连接线程
 * Created by q on 2017/10/19.
 */

public class ConnectThread extends Thread {
    String serverip;
    String username;
    String password;
    Handler handler=new Handler();
    Context context ;
    private static XMPPConnection connection=null;

    public XMPPConnection getConnection() {
        return connection;
    }

    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }

    public ConnectThread()
    {
//        this.serverip=serverip;
//        this.username=username;
//        this.password= password;
//        this.handler=handler;
//        this.context=context;
    }
    @Override
    public void run() {
            try {
                XMPPConnection.DEBUG_ENABLED = true;
                ConnectionConfiguration conConfig = new ConnectionConfiguration(
                        "192.168.155.1", 5222);
                conConfig.setReconnectionAllowed(true);
                conConfig.setSendPresence(false);
                conConfig.setSASLAuthenticationEnabled(true);
                conConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
                conConfig.setServiceName("zw");
                connection = new XMPPConnection(conConfig);
                connection.connect();
//                if (connection.isConnected())
//                {
//                    if (username.isEmpty() || password.isEmpty() || serverip.isEmpty())
//                    {
//                        ////为用户名、密码或服务器为空,handle的message。what设为2，在主线程弹出用户名、密码或服务器不能为空的toast
//                        Message message = new Message();
//                        message.what = 2;
//                        handler.sendMessage(message);
//                        connection.disconnect();
//                    }
//                    else{
//                        try {
//                            connection.login(username, password);
//                        }catch (Exception e)
//                        {
//                            Message message = new Message();
//                            message.what = 3;
//                            handler.sendMessage(message);
//                        }
//                        if (connection.isAuthenticated())
//                        {
//                            Presence presence = new Presence(Presence.Type.available);
//                            connection.sendPacket(presence);
//                            ////handle message.what=1; 登录成功
//                            Message message = new Message();
//                            message.what = 1;
//                            handler.sendMessage(message);
//                        }else {
//                            //handle message.what=3; 用户名或密码错误
//                            Message message = new Message();
//                            message.what = 3;
//                            handler.sendMessage(message);
//                            connection.disconnect();
//                        }
//                    }
//                }
//            else
//                {
//                    Message message = new Message();
//                    message.what = 4;
//                    handler.sendMessage(message);
//                }
            }catch (XMPPException e)
            {
                Message message = new Message();
                message.what = 4;
                handler.sendMessage(message);
            }
    }


}
