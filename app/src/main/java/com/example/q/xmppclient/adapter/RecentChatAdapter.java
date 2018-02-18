package com.example.q.xmppclient.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.entity.ChatRecordInfo;
import com.example.q.xmppclient.entity.User;
import com.example.q.xmppclient.manager.ContacterManager;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.util.FormatUtil;

import java.util.List;

/**
 * Created by q on 2017/11/5.
 */

public class RecentChatAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<ChatRecordInfo> inviteUsers;
    private Context context;
    private int resourceId;
    OnClickListener contacterOnClick;

//    public  RecentChatAdapter(Context context,int textViewResourceId ,List<ChatRecordInfo> inviteUsers)
//    {
//        super(context,textViewResourceId,inviteUsers);
//        this.context=context;
//        this.resourceId=textViewResourceId;
//    }


    public RecentChatAdapter(Context context,int textViewResourceId ,List<ChatRecordInfo> inviteUsers) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.resourceId=textViewResourceId;
        this.inviteUsers = inviteUsers;
    }

    public void setNoticeList(List<ChatRecordInfo> inviteUsers) {
        this.inviteUsers = inviteUsers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatRecordInfo notice = inviteUsers.get(position);
        Integer ppCount = notice.getNoticeSum();
        ViewHolderx holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(resourceId, null);
            holder = new ViewHolderx();
            holder.newTitle = (TextView) convertView
                    .findViewById(R.id.new_title);
            holder.itemIcon = (ImageView) convertView
                    .findViewById(R.id.new_icon);
            holder.newContent = (TextView) convertView
                    .findViewById(R.id.new_content);
            holder.newDate = (TextView) convertView.findViewById(R.id.new_date);
            holder.paopao = (TextView) convertView.findViewById(R.id.paopao);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderx) convertView.getTag();
        }
        // connection.getRoster().getEntry(user)
        String jid = notice.getFrom();
        User u = ContacterManager.getUserByJidSql(jid);
        if (null == u) {
            u = new User();
            u.setNickName(jid);
        }

        holder.newTitle.setText(u.getNickName());
        if(u.getIcon()==null) {
            holder.itemIcon.setImageResource(R.drawable.default_icon);
        }else
        {
            holder.itemIcon.setImageDrawable(FormatUtil.bitmap2Drawable(u.getIcon()));
        }
        holder.newContent.setText(notice.getContent());
        holder.newContent.setTag(u);
        holder.newDate.setText(notice.getNoticeTime().substring(5, 16));

        if (ppCount != null && ppCount > 0) {
            holder.paopao.setText(ppCount + "");
            holder.paopao.setVisibility(View.VISIBLE);

        } else {
            holder.paopao.setVisibility(View.GONE);
        }
//        convertView.setOnClickListener(contacterOnClick);

        return convertView;
    }

    public class ViewHolderx {
        public ImageView itemIcon;
        public TextView newTitle;
        public TextView newContent;
        public TextView newDate;
        public TextView paopao;

    }

    @Override
    public int getCount() {
        return inviteUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return inviteUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnClickListener(View.OnClickListener contacterOnClick) {

        this.contacterOnClick = contacterOnClick;
    }
}
