package com.example.q.xmppclient.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.common.Constant;
import com.example.q.xmppclient.manager.LoginConfig;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.util.FormatUtil;
import com.example.q.xmppclient.util.StringUtil;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import dmax.dialog.SpotsDialog;

public class AvatarActivity extends ActivityBase {
    Toolbar toolbar;
    ImageView iv_icon;
    VCard vCard;
    LoginConfig loginConfig;
    Button btn_avatar_save;
    AlertDialog dialog;
    private  static final  int TAKE_PHOTO=1;
    private  Uri imageUri;
    private static final int CHOOSE_PHOTO=0;
    SetVcardTask setVcardTask=null;

    public void initActionBar()
    {
        toolbar=(Toolbar)findViewById(R.id.avator_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("头像");
        btn_avatar_save= (Button) findViewById(R.id.btn_avatar_save);
        btn_avatar_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVcardTask=null;
                dialog=null;
                setVcardTask=new SetVcardTask();
                setVcardTask.execute(FormatUtil.drawable2Bitmap(iv_icon.getDrawable()));
            }
        });
        toolbar.setOnCreateContextMenuListener(this);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            Intent intent;
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.select:
                        //todo 从相册中选取照片
                        intent =new Intent("android.intent.action.GET_CONTENT");
                        intent.setType("image/*");
                        startActivityForResult(intent,CHOOSE_PHOTO);
                        return true;
                    case R.id.take_photo:
                        //todo 直接拍摄照片
                        File outputImage=new File(getExternalCacheDir(),"output_image.jpg");
                        try {
                            while(outputImage.exists())
                            {
                                int count=0;
                                outputImage.renameTo(new File(getExternalCacheDir(),"output_image"+count+".jpg"));
                            }
                            outputImage.createNewFile();
                        }catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        if(Build.VERSION.SDK_INT>=24)
                        {
                            imageUri= FileProvider.getUriForFile(AvatarActivity.this,"fileprovider",outputImage);
                        }else
                        {
                            imageUri= Uri.fromFile(outputImage);
                        }
                        intent =new Intent("android.media.action.IMAGE_CAPTURE");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                        startActivityForResult(intent,TAKE_PHOTO);
                                return  true;
                        }
                return false;
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.avatar_menu,menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        byte[] image=intent.getByteArrayExtra("ClippedImage");
        iv_icon.setImageBitmap(FormatUtil.Bytes2Bitmap(image));
        btn_avatar_save.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar);
        initActionBar();
        loginConfig=getLoginConfig();
        iv_icon=(ImageView)findViewById(R.id.iv_icon);
        iv_icon.setImageBitmap(MainActivity.currentUser.getIcon());
//        try {
//            vCard = new VCard();
//            vCard.load(XmppConnectionManager.getInstance().getConnection(), FormatUtil.getJidByName(loginConfig.getUsername(),loginConfig.getServerName()));
//
//            if(vCard==null||vCard.getAvatar()==null)
//            {
//                iv_icon.setImageResource(R.drawable.avatar);
//            }else
//            {
//                ByteArrayInputStream bais = new ByteArrayInputStream(vCard.getAvatar());
//                iv_icon.setImageDrawable(FormatUtil.getInstance().InputStream2Drawable(bais));
//            }
//        }catch (XMPPException e)
//        {
//            e.printStackTrace();
//        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case  TAKE_PHOTO:
                if (resultCode==RESULT_OK)
                {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
                        byte[] datas = baos.toByteArray();
                        Intent intent=new Intent(this,ClipActivity.class);
                        intent.putExtra("SelectedImage",datas);
                        startActivity(intent);
                    }catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }finally {
                        try {
                            baos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap = null;
               //判断手机系统版本号  
                    if (Build.VERSION.SDK_INT >= 19) {
                         //4.4及以上系统使用这个方法处理图片  

                        bitmap = FormatUtil.handleImageOnKitKat(this, data);//ImgUtil是自己实现的一个工具类  
                    } else {
                        //4.4以下系统使用这个方法处理图片  
                        bitmap = FormatUtil.handleImageBeforeKitKat(this, data);
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
                        byte[] datas = baos.toByteArray();
                        Intent intent=new Intent(this,ClipActivity.class);
                        intent.putExtra("SelectedImage",datas);
                        startActivity(intent);
                    }finally {
                        try {
                            baos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dialog=null;
    }
    private class SetVcardTask extends AsyncTask<Bitmap, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            dialog=new SpotsDialog(context, "正在保存",R.style.Custom);
            dialog.show();
            Timer timer=new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {

                }
            },500);
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(Bitmap... param) {
            VCard vCard = new VCard();
            if (!XmppConnectionManager.getInstance().getConnection().isConnected()) {
                try {
                    XmppConnectionManager.getInstance().getConnection().connect();
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
            try {
                vCard.load(XmppConnectionManager.getInstance().getConnection());
            }catch (XMPPException e) {
                return false;
            }
            // 设置和更新用户信息
            if (setAndSaveAvatar(param[0])==null) {
                return false;
            }else
            {
                vCard.setAvatar(setAndSaveAvatar(param[0]));
            }
            try {
                vCard.save(XmppConnectionManager.getInstance().getConnection());
                Presence subscription = new Presence(Presence.Type.available);
                XmppConnectionManager.getInstance().getConnection()
                        .sendPacket(subscription);
                return true;
            } catch (XMPPException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            dialog.dismiss();
            if (bool) {
                dialog=new SpotsDialog(context,"保存成功",R.style.Custom);
                dialog.show();
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        dialog=null;
                        MainActivity.currentUser.setIcon(
                                FormatUtil.drawable2Bitmap(iv_icon.getDrawable()));
                        Intent updateIntent=new Intent();
                        updateIntent.setAction(Constant.REFRESH_PERSONALINFO);
                        sendBroadcast(updateIntent);
                        Intent intent = new Intent(AvatarActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                },500);
            }else {
                dialog=new SpotsDialog(context, "保存失败",R.style.Custom);
                dialog.show();
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        dialog=null;
//                        Intent intent = new Intent(AvatarActivity.this, MainActivity.class);
//                        intent.putExtra("action", "edit");
//                        startActivity(intent);
                    }
                },500);
            }


        }

        /**
         * 返回头像byte数组并将新头像缓存到本地
         */
        private byte[] setAndSaveAvatar(Bitmap bitmap) {
            //默认image路径
            String imageDir = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() +
                    AvatarActivity.this.getResources().getString(R.string.img_dir) + "/";
            File fileDir = new File(imageDir);
            //获取内部存储状态
            String state = Environment.getExternalStorageState();
            //如果状态不是mounted，无法读写
            if (!state.equals(Environment.MEDIA_MOUNTED)) {
                return null;
            }
            byte[] bytes=null;
            //本地读文件
            String fileName = "avatar_" + StringUtil.getJidByName
                    (loginConfig.getUsername(), loginConfig.getServerName()) + ".png";
            File file = new File(imageDir, fileName);
            bytes=FormatUtil.Bitmap2Bytes(bitmap);
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            }catch (IOException e)
            {
                return null;
            }
            return bytes;
        }
    }
}
