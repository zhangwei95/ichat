package com.example.q.xmppclient.task;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.q.xmppclient.activity.ActivityBase;
import com.example.q.xmppclient.activity.LoginActivity;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.manager.LoginConfig;
import com.example.q.xmppclient.manager.XmppConnectionManager;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;

import java.util.Map;

/**
 * Created by q on 2017/10/29.
 */

public class RegisterTask extends AsyncTask<String,Integer,Integer> {
    private ProgressDialog pd;
    private Context context;
    private ActivityBase activityTool;
    private  LoginConfig loginConfig;
    private  XMPPConnection xmppConnection;
    private Map<String,String> attr;

    public RegisterTask(ActivityBase activityTool, LoginConfig loginConfig)
    {
        this.loginConfig=loginConfig;
        this.activityTool=activityTool;
        xmppConnection=XmppConnectionManager.getInstance().getConnection();
        pd=activityTool.getProgressDialog();
        context=activityTool.getContext();
    }
    @Override
    protected void onPreExecute() {
        pd.setMessage("注册中...");
        pd.show();
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... params) {
       return register();
    }

    @Override
    protected void onPostExecute(Integer result) {
        pd.dismiss();
        switch (result){
            //todo 注册成功或失败的处理
            case Constant.REGISTER_SUCCESS:
//                AlertDialog.Builder ad=new AlertDialog.Builder(context);
//
//                ad.setTitle("注册成功");
//                ad.setMessage("是否选择直接登录!");
//                ad.setCancelable(false);
//                ad.setPositiveButton("直接登录", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
                        loginConfig.setNewUser(true);
                        loginConfig.setFirstStart(true);
                        loginConfig.setRemember(true);
                        loginConfig.setAutoLogin(false);
                        LoginTask loginTask=new LoginTask(activityTool,loginConfig);
                        loginTask.execute();
//                    }
//                });
//                ad.setNegativeButton("手动登录", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Intent intent=new Intent();
//                        intent.setClass(context, LoginActivity.class);
//                        context.startActivity(intent);
//                    }
//                });
//                ad.show();
                break;
            case Constant.USER_EXIST:
                Toast.makeText(context,Constant.USER_EXIST_MESSAGE,Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(context,"default",Toast.LENGTH_SHORT).show();
                break;
        }
        super.onPostExecute(result);
    }
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
    private Integer register()
    {
        try {
            if(!xmppConnection.isConnected()) {
                xmppConnection.connect();
            }
        }catch (XMPPException e)
        {
            return Constant.REGISTER_SERVER_UNAVAILABLE;
        }
        Registration reg=new Registration();
        reg.setType(IQ.Type.SET);
        reg.setTo(xmppConnection.getServiceName());
        reg.setUsername(loginConfig.getUsername());
        reg.setPassword(loginConfig.getPassword());
        reg.addAttribute("name",loginConfig.getNickname());
        reg.addAttribute("android", "geolo_createUser_android");

        PacketFilter filter = new AndFilter(new PacketIDFilter(reg
                .getPacketID()), new PacketTypeFilter(IQ.class));
        PacketCollector collector =xmppConnection
                .createPacketCollector(filter);
        xmppConnection.sendPacket(reg);
        IQ result = (IQ) collector.nextResult(SmackConfiguration
                .getPacketReplyTimeout());
        collector.cancel();
        if (result==null)
        {
            return Constant.REGISTER_SERVER_UNAVAILABLE;
        }else if(result.getType()==IQ.Type.ERROR)
        {
            if (result.getError().toString().equalsIgnoreCase("conflict(409)"))
            {
                return Constant.USER_EXIST;
            }else
            {
                //todo 其他的一些注册失败结果
            }
        }else if(result.getType()==IQ.Type.RESULT)
        {
            return Constant.REGISTER_SUCCESS;
        }
        return Constant.OTHER_REGISTER_ERROR;
    }
}
