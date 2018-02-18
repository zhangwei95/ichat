package com.example.q.xmppclient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import android.os.Message;
import android.os.Messenger;
import android.support.annotation.IntDef;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;


public class ConnectService extends Service {
    public ConnectService() {
    }
    Thread connectThread;
    String username;
    String password;
    Handler handler = new Handler();
    public static XMPPConnection connection = null;
    private boolean isConnected()
    {
        if (connection==null)
        {
            return false;
        }
        else return true;
    }

    public void openConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    XMPPConnection.DEBUG_ENABLED = true;
                    ConnectionConfiguration conConfig = new ConnectionConfiguration("192.168.155.1", 5222);
                    conConfig.setReconnectionAllowed(true);
                    conConfig.setSendPresence(false);
                    conConfig.setSASLAuthenticationEnabled(true);
                    conConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
                    conConfig.setServiceName("zw");
                    connection = new XMPPConnection(conConfig);
                    connection.connect();
                    Message message = new Message();
                    message.what = 5;
                    handler.sendMessage(message);
                }  catch (XMPPException e)
                {
                    Message message = new Message();
                    message.what = 4;
                    handler.sendMessage(message);
                }
            }
        }).start();

    }
    public static XMPPConnection getConnection() {
        return connection;
    }


    public void closeConnection() {
        if (isConnected())
            connection.disconnect();
            connection = null;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
    String command;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (connection ==null) {
            openConnection();
        }
            try {
                command = intent.getStringExtra("command");
                if (command.equals("login")) {
                    username = intent.getStringExtra("username");
                    password = intent.getStringExtra("password");
                    login();
                }
            } catch (Exception e) {
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
            }
        return super.onStartCommand(intent, flags, startId);
    }


        @Override
        public void onDestroy () {
            super.onDestroy();
            closeConnection();
        }
        public void  login()
        {
                try {
                    connection.login(username,password);
                    Presence presence = new Presence(Presence.Type.available);
                    connection.sendPacket(presence);
                }
                catch (XMPPException e)
                {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }



        }

}
