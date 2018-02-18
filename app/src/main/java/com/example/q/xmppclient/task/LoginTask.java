package com.example.q.xmppclient.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.activity.MainActivity;
import com.example.q.xmppclient.activity.IActivity;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.db.DataBaseHelper;
import com.example.q.xmppclient.manager.LoginConfig;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.util.FormatUtil;
import com.example.q.xmppclient.util.StringUtil;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.packet.VCard;

import java.util.Random;


/**
 * Created by q on 2017/10/26.
 */
public class LoginTask extends AsyncTask<String, Integer, Integer> {
    private ProgressDialog progressDialog;
    private Context context;
    private LoginConfig loginConfig;
    private IActivity activityTool;
    private XMPPConnection xmppConnection;

    public LoginTask(IActivity activityTool, LoginConfig loginConfig) {
        this.activityTool = activityTool;
        this.loginConfig = loginConfig;
        this.progressDialog = activityTool.getProgressDialog();
        this.context = activityTool.getContext();

    }

    @Override
    protected void onPreExecute() {
        progressDialog.setMessage("正在登录");
        progressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... params) {
        return login();
    }

    @Override
    protected void onPostExecute(Integer result) {
        progressDialog.dismiss();
        switch (result) {
            case Constant.LOGIN_SUCCESS:
                Toast.makeText(context, Constant.LOGIN_SUCCESS_MESSAGE, Toast.LENGTH_SHORT).show();
                try {
                    //todo 要改
                    VCard vcard=new VCard();
                    if (XmppConnectionManager.getInstance().getConnection().isConnected()) {
                        vcard.load(XmppConnectionManager.getInstance().getConnection(),StringUtil.getJidByName(loginConfig.getUsername(),loginConfig.getServerName()));
                    }else
                    {
                        XmppConnectionManager.getInstance().getConnection().connect();
                        vcard.load(XmppConnectionManager.getInstance().getConnection(),StringUtil.getJidByName(loginConfig.getUsername(),loginConfig.getServerName()));
                    }
                    if(vcard==null||FormatUtil.empty(vcard.getNickName()))
                    {
                        vcard = new VCard();
                        vcard.setNickName(loginConfig.getNickname());
                        FormatUtil format = FormatUtil.getInstance();
                        vcard.setAvatar(format.Drawable2Bytes(context.getResources().getDrawable(R.drawable.default_icon)));
                        vcard.save(XmppConnectionManager.getInstance().getConnection());
                    }
                }catch (XMPPException e)
                {
                    Toast.makeText(context,"VCard生成失败",Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent();
                if (loginConfig.isFirstStart()) {
                    //todo 首次登录动画界面
//                    SetVcardTask setVcardTask=new SetVcardTask(loginConfig.getNickname());
//                    setVcardTask.execute();
                    loginConfig.setFirstStart(false);
                    Random random=new Random();
                    DataBaseHelper dbhelper=new DataBaseHelper(context,loginConfig.getUsername(),null,3);
                    dbhelper.getWritableDatabase();
                    intent.setClass(context, MainActivity.class);
                } else {
                    intent.setClass(context, MainActivity.class);
                }
                activityTool.saveLoginConfig(loginConfig);
                activityTool.startService();
                context.startActivity(intent);
                break;
            case Constant.LOGIN_ERROR:
                Toast.makeText(context,Constant.LOGIN_ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
                break;
            case Constant.SERVER_UNAVAILABLE:
                Toast.makeText(context,Constant.SERVER_UNAVAILABLE_MESSAGE, Toast.LENGTH_SHORT).show();
                break;
            case Constant.USERNAME_PWD_ERROR:
                Toast.makeText(context,Constant.USERNAME_PWD_ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
                break;
            case Constant.UNKNOWN:
                Toast.makeText(context,Constant.UNKNOWN_MESSAGE, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    private Integer login() {
        String username = loginConfig.getUsername();
        String password = loginConfig.getPassword();

        try {
            if (xmppConnection==null) {
                xmppConnection = XmppConnectionManager.getInstance().getConnection();
            }
            if(!xmppConnection.isConnected())
            {
              xmppConnection.connect();
            }
            xmppConnection.login(username, password);
            xmppConnection.sendPacket(new Presence(Presence.Type.available));
            loginConfig.setUsername(username);
            if (loginConfig.isRemember()) {
                loginConfig.setPassword(password);
            } else {
                loginConfig.setPassword("");
            }
            loginConfig.setOnline(true);
        } catch (Exception exc) {
            int code;

            if (exc instanceof XMPPException) {
                XMPPException xe = (XMPPException) exc;
                final XMPPError xmppError = xe.getXMPPError();
                if(xmppError!=null)
                {
                    code = xmppError.getCode();
                    if(code==502)
                    {
                        return Constant.SERVER_UNAVAILABLE;
                    } else if (code==401||code==403)
                    {
                        return Constant.USERNAME_PWD_ERROR;
                    }else
                        return Constant.UNKNOWN;
                }
            }
            return Constant.LOGIN_ERROR;
        }
        return Constant.LOGIN_SUCCESS;
    }
}
/**
 * Xmpp错误码
 * 302
 * 重定向
 * 尽管HTTP规定中包含八种不同代码来表示重定向，Jabber只用了其中一个（用来代替所有的重定向错误）。
 * 不过Jabber代码302是为以后的功能预留的，目前还没有用到
 * 400
 * 坏请求
 * Jabber代码400用来通知Jabber客户端，一个请求因为其糟糕的语法不能被识别。
 * 例如，当一个Jabber客户端发送一个的订阅请求给它自己活发送一条没有包含“to”属性的消息，Jabber代码400就会产生。
 * 401
 * 未授权的
 * Jabber代码401用来通知Jabber客户端它们提供的是错误的认证信息，如，在登陆一个Jabber服务器时使用一个错误的密码，或未知的用户名。
 * 402
 * 所需的费用
 * Jabber代码402为未来使用进行保留，目前还不用到。
 * 403
 * 禁止
 * Jabber代码403被Jabber服务器用来通知Jabber客户端该客户端的请求可以识别，但服务器拒绝执行。目前只用在注册过程中的密码存储失败。
 * 404
 * 没有找到
 * Jabber代码404用来表明Jabber服务器找不到任何与JabberID匹配的内容，该JabberID是一个Jabber客户端发送消息的目的地。
 * 如，一个用户打算向一个不存在的JabberID发送一条消息。如果接受者的Jabber服务器无法到达，将发送一个来自500级数的错误代码。
 * 405
 * 不允许的
 * Jabber代码405用在不允许操作被’from’地址标识的JabberID。例如，它可能产生在，
 * 一个非管理员用户试图在服务器上发送一条管理员级别的消息，或者一个用户试图发送一台Jabber服务器的时间或版本，
 * 或者发送一个不同的JabberID的vCard。
 * 408
 * 注册超时
 * 当一个Jabber客户端不能在服务器准备好的时间内发起一个请求时，Jabber服务器生成Jabber代码
 * 408。这个代码当前只用于Jabber会话管理器使用的零度认证模式中。
 * 500
 * 服务器内部错误
 * 当一台Jabber服务器遇到一种预期外的条件，该条件阻止服务器处理来自Jabber客户端的包，这是将用到Jabber代码500。
 * 现在，唯一会引发500错误代码的时间是当一个Jabber客户端试图通过服务器认证，而该认证因为某些原因没有被处理（如无法保存密码）。
 * 502
 * 远程服务器错误
 * 当因为无法到达远程服务器导致转发一个包失败时，使用Jabber代码502。
 * 该代码发送的特殊例子包括一个远程服务器的连接的失败，无法获取远程服务器的主机名，以及远程服务器错误导致的外部时间过期。
 * 503
 * 服务无法
 * 当一个Jabber客户端请求一个服务，而Jabber服务器通常由于一些临时原因无法提供该服务时，使获得用Jabber代码503。
 * 例如，一个Jabber客户端试图发送一条消息给另一个用户，该用户不在线，但它的服务器不提供离线存储服务，
 * 服务器将返回一个503错误代码给发送消息的JabberID。当为vcard-temp和jabber:iq:private名字空间设置信息时，
 * 出现通过xdb进行数据存储的写入错误，也使用该代码。
 * 504
 * 远程服务器超时Jabber代码504用于下列情况:试图连接一台服务器发生超时，错误的服务器名。
 * 510
 * 连接失败Jabber代码510
 */