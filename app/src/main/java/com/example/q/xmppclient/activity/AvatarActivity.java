package com.example.q.xmppclient.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.manager.LoginConfig;
import com.example.q.xmppclient.manager.XmppConnectionManager;
import com.example.q.xmppclient.util.FormatUtil;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AvatarActivity extends ActivityTool {
    Toolbar toolbar;
    ImageView iv_icon;
    VCard vCard;
    LoginConfig loginConfig;
    private  static final  int TAKE_PHOTO=1;
    private  Uri imageUri;
    private static final int CHOOSE_PHOTO=0;


    public void initActionBar()
    {
        toolbar=(Toolbar)findViewById(R.id.avator_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("头像");

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
                        defalut:
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
                    try {
                        Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        iv_icon.setImageBitmap(bitmap);
                    }catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
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
                    iv_icon.setImageBitmap(bitmap);
                }
                break;
            default:
                break;
        }
    }
}
