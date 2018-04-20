package com.example.q.xmppclient.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.activity.ActivityBase;
import com.example.q.xmppclient.activity.ChatActivity;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.entity.User;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.util.StringUtil;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dmax.dialog.SpotsDialog;

/**
 * Created by Administrator on 2018/4/20 0020.
 */

public class RequestListAdapter extends RecyclerView.Adapter<RequestListAdapter.ViewHolder> {
    private List<User> requestList;
    Context context;
    String[] group=new String[]{"Friends"};
    String TAG="RequestListAdapter";
    AlertDialog dialog;
    User user;

    //不同的位置获取不同的布局文件
//    @Override
//    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
//    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_list_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        user = requestList.get(position);
        holder.tv_nickname.setText(user.getNickName());
        holder.iv_userIcon.setImageBitmap(user.getIcon());
        if (user.getItemType()== RosterPacket.ItemType.to) {
            holder.btn_validate.setText("通过");
            holder.btn_validate.setBackground(context.getResources().getDrawable(R.drawable.shape_green));
            holder.btn_validate.setClickable(true);
            holder.btn_validate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (XmppConnectionManager.getInstance().getConnection().isConnected()) {
                        CheckTask task = new CheckTask();
                        task.execute(user.getJid());
                    }else {
                        ((ActivityBase)context).showToast("未连接到服务器！");
                    }
                }
            });
        }else if(user.getItemType()== RosterPacket.ItemType.from){
            holder.btn_validate.setText("等待");
            holder.btn_validate.setBackground(context.getResources().getDrawable(R.drawable.shape_gray));
            holder.btn_validate.setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iv_userIcon;
        TextView tv_nickname;
        Button btn_validate;

        public ViewHolder(View view) {
            super(view);
            iv_userIcon = (ImageView) view.findViewById(R.id.user_image);
            tv_nickname = (TextView) view.findViewById(R.id.user_nickname);
            btn_validate=view.findViewById(R.id.btn_request);
        }
    }
    public RequestListAdapter(List<User> requestList,Context context,AlertDialog dialog) {
        this.requestList = requestList;
        this.context=context;
        this.dialog=dialog;
    }
    private class CheckTask extends AsyncTask<String,Void,Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Boolean o) {
            if (o){
                dialog=new SpotsDialog(context, "验证通过",R.style.Custom);
                dialog.show();
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Intent intentAllow = new Intent(context, ChatActivity.class);
                        intentAllow.putExtra("to", user.getJid());
                        context.startActivity(intentAllow);
                    }
                },500);

            }else{
                dialog=new SpotsDialog(context, "验证失败",R.style.Custom);
                dialog.show();
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                },500);
            }
            super.onPostExecute(o);
        }
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                sendSubscribe(Presence.Type.subscribe, params[0]);
                Log.e(TAG, "check send subscribe" );
                sendSubscribe(Presence.Type.subscribed,  params[0]);
                Log.e(TAG, "check send subscribed" );
                sendSubscribe(Presence.Type.available,  params[0]);
                Log.e(TAG, "check send available" );
                if(StringUtil.empty(XmppConnectionManager.getInstance().getConnection().
                        getRoster().getEntry( params[0]))) {
                    XmppConnectionManager.getInstance().getConnection().getRoster()
                            .createEntry( params[0], StringUtil.getUserNameByJid( params[0]), group);
                }
                //todo 代码重复 待删除 和entriedUpdate
//                if(ContacterManager.isExistInDB(jid)){
//                    Log.e(TAG, "isExistInDB jid="+jid+"entry type="+FormatUtil.ItemType2string(
//                            XmppConnectionManager.getInstance().
//                            getConnection().getRoster().getEntry(jid).getType()));
//                    ContacterManager.updateDBFriend(XmppConnectionManager.getInstance().
//                            getConnection().getRoster().getEntry(jid),jid,XmppConnectionManager.
//                            getInstance().getConnection());
//                }else{
//                    Log.e(TAG, "isnotExistInDB jid="+jid+"entry type="+FormatUtil.ItemType2string(
//                            XmppConnectionManager.getInstance().
//                                    getConnection().getRoster().getEntry(jid).getType()) );
//                    ContacterManager.insertDBFriend(XmppConnectionManager.getInstance().
//                            getConnection().getRoster().getEntry(jid),jid,XmppConnectionManager.
//                            getInstance().getConnection());
//                }
            }catch (XMPPException e) {
                return false;
            }
            Intent checkBoasdcast=new Intent(Constant.ROSTER_ADDED);
            context.sendBroadcast(checkBoasdcast);
            return true;
        }
        protected void sendSubscribe(Presence.Type type, String to) {
            Presence presence = new Presence(type);
            presence.setTo(to);
            XmppConnectionManager.getInstance().getConnection()
                    .sendPacket(presence);
        }
    }

}
