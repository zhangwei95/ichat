package com.example.q.xmppclient.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.q.xmppclient.activity.MainActivity;
import com.example.q.xmppclient.util.AppUtil;

import org.apache.http.cookie.CookieAttributeHandler;

import java.util.Date;

/**
 * Created by q on 2018/1/10.
 */

public class DataBaseHelper extends SDCardSQLiteOpenHelper{
    Context mContext;
    public static final String Create_contactor="create table if not exists im_contactors("+
            "id integer primary key autoincrement,"+
            "jid text unique,"+
            "nickname text,"+
            "avatar text,"+
            "country text,"+
            "province text,"+
            "city text,"+
            "sign text," +
            "itemType text);";
    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        super(context, name, factory, version);

        mContext=context;
    }
    //建表
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE if not exists  [im_msg_his] ([_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [content] NVARCHAR, [msg_from] NVARCHAR, [msg_to] NVARCHAR, [msg_time] TEXT, [msg_type] INTEGER);");
//        if(AppUtil.isDebug()) {
//            Toast.makeText(mContext, "create [im_msg_his_user] success", Toast.LENGTH_SHORT).show();
//        }
        db.execSQL("CREATE TABLE if not exists  [im_notice]  ([_id] INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT, [type] INTEGER, [title] NVARCHAR, [content] NVARCHAR, [notice_from] NVARCHAR, [notice_to] NVARCHAR, [notice_time] TEXT, [status] INTEGER);");
        db.execSQL(Create_contactor);
//        if(AppUtil.isDebug()) {
//            try {
//                Thread.sleep(1000);
//                Toast.makeText(mContext, "create [im_notice_user] success", Toast.LENGTH_SHORT).show();
//            } catch (InterruptedException e) {
//
//            }
//        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("drop table if exists im_contactors");
        onCreate(db);
    }
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }


}
