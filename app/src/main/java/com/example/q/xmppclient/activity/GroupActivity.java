package com.example.q.xmppclient.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.example.q.xmppclient.R;

public class GroupActivity extends ActivityBase {
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        initActionBar();
    }
    public void initActionBar()
    {
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("我的群组");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setOnCreateContextMenuListener(this);

    }
}
