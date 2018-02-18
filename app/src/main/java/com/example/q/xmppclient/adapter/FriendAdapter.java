package com.example.q.xmppclient.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.entity.User;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.util.FormatUtil;
import com.example.q.xmppclient.util.StringUtil;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by q on 2017/11/4.
 */

public class FriendAdapter extends ArrayAdapter<User> {
        private int resourceId;
    static String imageDir;
    private Context mcontext;
        public  FriendAdapter(Context context,int textViewResourceId ,List<User> friendList)
        {
            super(context,textViewResourceId,friendList);
            mcontext=context;
            resourceId=textViewResourceId;
            imageDir= Environment.getExternalStorageDirectory().getAbsolutePath() + "/ichat/images/";
        }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User user=getItem(position);
        View view;
        if(convertView==null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        }else
        {
            view=convertView;
        }
        ImageView iv_icon=(ImageView)view.findViewById(R.id.iv_usericon);
        TextView  tv_nickname=(TextView)view.findViewById(R.id.tv_nickname);
        iv_icon.setImageDrawable(FormatUtil.bitmap2Drawable(user.getIcon()));
        tv_nickname.setText(user.getNickName());
//
//                //本地读取好友头像
//                try {
//                    String dir = imageDir;
//                    String fileName ="avatar_"+user.getUsername()+".png";
//                    FileInputStream stream = new FileInputStream(dir+fileName);
//                    Bitmap bitmap = BitmapFactory.decodeStream(stream);
//                    iv_icon.setImageBitmap(bitmap);
//                    stream.close();
//                }catch (Exception e)
//                {
//                    e.printStackTrace();
//                    //本地没有就保存到本地
//
//                    try {
//                        ByteArrayInputStream bais = new ByteArrayInputStream(vcard.getAvatar());
//                        iv_icon.setImageDrawable(FormatUtil.getInstance().InputStream2Drawable(bais));
//                        FormatUtil format=FormatUtil.getInstance();
//                        File dirfile=new File(imageDir);
////                    //获取内部存储状态
////                    String state = Environment.getExternalStorageState();
////                    //如果状态不是mounted，无法读写
////                    if (!state.equals(Environment.MEDIA_MOUNTED)) {
////                        return;
////                    }
//                        String fileName ="avatar_"+user.getUsername();
//                        Bitmap mBitmap=FormatUtil.drawable2Bitmap(iv_icon.getDrawable());
//                        bais.close();
//                        try {
//                            if(!dirfile.exists())
//                            {
//                                dirfile.mkdirs();
//                            }
//
//                            File file = new File(imageDir,fileName + ".png");
//                            FileOutputStream out = new FileOutputStream(file);
//                            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//                            out.flush();
//                            out.close();
//                        } catch (Exception ex) {
//                            e.printStackTrace();
//                        }
//                    }catch (IOException io)
//                    {
//                        io.printStackTrace();
//                    }
//
//                }
//
//        if(StringUtil.empty(user.getNickName())) {
//            tv_nickname.setText(user.getUsername());
//        }else
//        {
//            tv_nickname.setText(user.getNickName());
//        }

        return view;


    }
}
