package com.example.q.xmppclient.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.q.xmppclient.activity.LoginActivity;
import com.example.q.xmppclient.activity.MainActivity;

/**
 * Created by q on 2017/11/6.
 */

public class DBManager {
    private int version=1;
    private  String databaseName;
    private Context context=null;
    private static DBManager dBmanager=null;


    private DBManager(Context context)
    {
        super();
        this.context=context;
    }

    /**
     * 获取DBmanager实例
     */
    public  static DBManager getInstance(Context context, String databaseName)
    {
        if(dBmanager==null)
        {
            dBmanager=new DBManager(context);
        }
        dBmanager.databaseName=databaseName;
        return  dBmanager;
    }
    /**
     * 打开数据库 注:SQLiteDatabase资源一旦被关闭,该底层会重新产生一个新的SQLiteDatabase
     */
    public SQLiteDatabase openDatabase() {
        return getDatabaseHelper().getWritableDatabase();
    }

    /**
     * 获取DataBaseHelper
     *
     * @return
     */
    public DataBaseHelper getDatabaseHelper() {

            return new DataBaseHelper(context, this.databaseName, null,
                    this.version);
    }

    /**
     *关闭数据库
     */

    public void closeDatabase(SQLiteDatabase database, Cursor cursor)
    {


    }
}
