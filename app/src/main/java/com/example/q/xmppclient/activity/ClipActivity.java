package com.example.q.xmppclient.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.util.FormatUtil;
import com.example.q.xmppclient.view.ClipImageLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ClipActivity extends ActivityBase {
    private ClipImageLayout mClipImageLayout;
    Toolbar toolbar;
    Button btn_avatar_clip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip);
        initActionBar();
        mClipImageLayout=(ClipImageLayout) findViewById(R.id.id_clipImageLayout);
        byte[] image=getIntent().getByteArrayExtra("SelectedImage");
        FormatUtil util=FormatUtil.getInstance();
        mClipImageLayout.setImageDrawable(util.Bytes2Drawable(image));
        btn_avatar_clip= (Button) findViewById(R.id.btn_avatar_clip);
        btn_avatar_clip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                Bitmap bitmap = mClipImageLayout.clip();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] datas = baos.toByteArray();
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                intent = new Intent(ClipActivity.this, AvatarActivity.class);
                intent.putExtra("ClippedImage", datas);
                startActivity(intent);
            }
        });
    }
    public void initActionBar() {
        toolbar=(Toolbar)findViewById(R.id.avator_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("裁剪头像");
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
}
