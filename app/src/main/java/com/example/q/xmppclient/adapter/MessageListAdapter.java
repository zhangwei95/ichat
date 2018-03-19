package com.example.q.xmppclient.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.activity.ChatActivity;
import com.example.q.xmppclient.activity.MainActivity;
import com.example.q.xmppclient.activity.UserInfoActivity;
import com.example.q.xmppclient.entity.ChatMessage;
import com.example.q.xmppclient.entity.User;
import com.example.q.xmppclient.manager.ContacterManager;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.util.DateUtil;

import java.util.List;

/**
 * Created by q on 2018/1/10.
 */

public class MessageListAdapter extends BaseAdapter {


    private List<ChatMessage> items;
    private Context context;
    private ListView adapterList;
    private LayoutInflater inflater;
    private User user;
    private int pagesize;


    public MessageListAdapter(Context context, List<ChatMessage> items,
                              ListView adapterList,int psize) {
        this.context = context;
        this.items = items;
        this.adapterList = adapterList;
        this.pagesize=psize;

    }

    public void refreshList(List<ChatMessage> items) {
        this.items = items;
        this.notifyDataSetChanged();
        adapterList.setSelection(pagesize);
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ChatMessage message = items.get(position);
        if (message.getMsgType() == 0) {
            convertView = this.inflater.inflate(
                    R.layout.layout_chat_in, null);
            if(user==null) {
                user = ContacterManager.getUserByJidSql(message.getFromSubJid());
            }
            ImageView userIcon=(ImageView)convertView
                    .findViewById(R.id.iv_Chat_UserIcon);
            TextView useridView = (TextView) convertView
                    .findViewById(R.id.tv_Chat_NickName);
            TextView dateView = (TextView) convertView
                    .findViewById(R.id.tv_Chat_MsgDate);
            TextView msgView = (TextView) convertView
                    .findViewById(R.id.tv_Chat_Msg);

            useridView.setText(user.getNickName());
            userIcon.setImageBitmap(user.getIcon());
            dateView.setText(DateUtil.LongDate2Short(message.getTime()));
            msgView.setText(message.getContent());
            userIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context,UserInfoActivity.class);
                    intent.putExtra("to",message.getFromSubJid());
                    context.startActivity(intent);
                }
            });

        } else {
            convertView = this.inflater.inflate(
                    R.layout.layout_chat_out, null);
            ImageView userIcon=(ImageView)convertView
                    .findViewById(R.id.iv_Chat_UserIcon);
            TextView useridView = (TextView) convertView
                    .findViewById(R.id.tv_Chat_NickName);
            TextView dateView = (TextView) convertView
                    .findViewById(R.id.tv_Chat_MsgDate);
            TextView msgView = (TextView) convertView
                    .findViewById(R.id.tv_Chat_Msg);
            useridView.setText("我");
            userIcon.setImageBitmap(MainActivity.currentUser.getIcon());
            dateView.setText(DateUtil.LongDate2Short(message.getTime()));
            msgView.setText(message.getContent());
//            userIcon.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent=new Intent(context,UserInfoActivity.class);
//                    intent.putExtra("to",message.getFromSubJid());
//                    context.startActivity(intent);
//                }
//            });
        }
        return convertView;
    }

}
