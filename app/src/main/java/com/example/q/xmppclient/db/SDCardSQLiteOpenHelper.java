package com.example.q.xmppclient.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.example.q.xmppclient.R;
import com.example.q.xmppclient.activity.LoginActivity;

import java.io.File;

/**
 * Created by q on 2018/1/10.
 */

public abstract class SDCardSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = SDCardSQLiteOpenHelper.class
            .getSimpleName();
    private final Context mContext;
    private final String mName;
    private final SQLiteDatabase.CursorFactory mFactory;
    private final int mNewVersion;

    private SQLiteDatabase mDatabase = null;
    private boolean mIsInitializing = false;

    /**
     * 创建一个SQLitehelper对象来完成创建，打开或管理数据库
     * 数据库不会马上创建和打开，直到以下两个方法被调用
     * {@link #getWritableDatabase} or {@link #getReadableDatabase}
     *
     * @param context
     *            用来打开和创建数据库
     * @param name
     *           数据库名, or null for an in-memory database
     * @param factory
     *            游标，自定义，或null使用默认的
     * @param version
     *            数据库版本号
     */
    public SDCardSQLiteOpenHelper(Context context, String name,
                                  SQLiteDatabase.CursorFactory factory, int version) {
        super(context,name,factory,version);
        if (version < 1)
            throw new IllegalArgumentException("Version must be >= 1, was "
                    + version);

        mContext = context;
        mName = name;
        mFactory = factory;
        mNewVersion = version;
    }

    /**
     * 创建或打开一个数据库，用来读写，一旦成功打开数据库就被加载到缓存
     * 当你需要写数据库的时候，你可以调用这个方法，当长时间不用请调用{@link #close}方法。
     *
     * <p>
     *
     * Errors such as bad permissions or a full disk may cause this operation to
     * fail, but future attempts may succeed if the problem is fixed.
     * </p>
     *
     * @throws SQLiteException
     *             if the database cannot be opened for writing
     * @return a read/write database object valid until {@link #close} is called
     */
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (mDatabase != null && mDatabase.isOpen() && !mDatabase.isReadOnly()) {
            return mDatabase; // The database is already open for business
        }
        if (mIsInitializing) {
            throw new IllegalStateException(
                    "getWritableDatabase called recursively");
        }

        // If we have a read-only database open, someone could be using it

        // (though they shouldn't), which would cause a lock to be held on

        // the file, and our attempts to open the database read-write would

        // fail waiting for the file lock. To prevent that, we acquire the

        // lock on the read-only database, which shuts out other users.

        boolean success = false;
        SQLiteDatabase db = null;
        try {
            mIsInitializing = true;
            if (mName == null) {
                db = SQLiteDatabase.create(null);
            } else {
                String path = getDatabasePath(mName).getPath();
                db = SQLiteDatabase.openOrCreateDatabase(path, mFactory);
            }

            int version = db.getVersion();
            if (version != mNewVersion) {
                db.beginTransaction();
                try {
                    if (version == 0) {
                        onCreate(db);
                    } else {
                        onUpgrade(db, version, mNewVersion);
                    }
                    db.setVersion(mNewVersion);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
            onOpen(db);
            success = true;
            return db;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mIsInitializing = false;
            if (success) {
                if (mDatabase != null) {
                    try {
                        mDatabase.close();
                    } catch (Exception e) {
                    }
                }
                mDatabase = db;
            } else {
                if (db != null)
                    db.close();
            }
        }
        return db;
    }

    /**
     * Create and/or open a database. This will be the same object returned by
     * {@link #getWritableDatabase} unless some problem, such as a full disk,
     * requires the database to be opened read-only. In that case, a read-only
     * database object will be returned. If the problem is fixed, a future call
     * to {@link #getWritableDatabase} may succeed, in which case the read-only
     * database object will be closed and the read/write object will be returned
     * in the future.
     *
     * @throws SQLiteException
     *             if the database cannot be opened
     * @return a database object valid until {@link #getWritableDatabase} or
     *         {@link #close} is called.
     */
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (mDatabase != null && mDatabase.isOpen()) {
            return mDatabase; // The database is already open for business

        }

        if (mIsInitializing) {
            throw new IllegalStateException(
                    "getReadableDatabase called recursively");
        }

        try {
            return getWritableDatabase();
        } catch (SQLiteException e) {
            if (mName == null)
                throw e; // Can't open a temp database read-only!

            Log.e(TAG, "Couldn't open " + mName
                    + " for writing (will try read-only):", e);
        }

        SQLiteDatabase db = null;
        try {
            mIsInitializing = true;
            String path = getDatabasePath(mName).getPath();
            db = SQLiteDatabase.openDatabase(path, mFactory,
                    SQLiteDatabase.OPEN_READWRITE);
            if (db.getVersion() != mNewVersion) {
                throw new SQLiteException(
                        "Can't upgrade read-only database from version "
                                + db.getVersion() + " to " + mNewVersion + ": "
                                + path);
            }

            onOpen(db);
            Log.w(TAG, "Opened " + mName + " in read-only mode");
            mDatabase = db;
            return mDatabase;
        } finally {
            mIsInitializing = false;
            if (db != null && db != mDatabase)
                db.close();
        }
    }

    /**
     * Close any open database object.
     */
    public synchronized void close() {
        if (mIsInitializing)
            throw new IllegalStateException("Closed during initialization");

        if (mDatabase != null && mDatabase.isOpen()) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    public File getDatabasePath(String name) {
        String EXTERN_PATH = null;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED) == true) {
            String dbPath = mContext.getString(R.string.dir)
                    + mContext.getString(R.string.db_dir) + "/";
            EXTERN_PATH = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + dbPath;
            File f = new File(EXTERN_PATH);
            if (!f.exists()) {
                f.mkdirs();
            }
        }
        return new File(EXTERN_PATH + name);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should
     * happen.
     *
     * @param db
     *            The database.
     */
    public abstract void onCreate(SQLiteDatabase db);

    /**
     * Called when the database needs to be upgraded. The implementation should
     * use this method to drop tables, add tables, or do anything else it needs
     * to upgrade to the new schema version.
     *
     * <p>
     * The SQLite ALTER TABLE documentation can be found <a
     * href="http://sqlite.org/lang_altertable.html">here</a>. If you add new
     * columns you can use ALTER TABLE to insert them into a live table. If you
     * rename or remove columns you can use ALTER TABLE to rename the old table,
     * then create the new table and then populate the new table with the
     * contents of the old table.
     *
     * @param db
     *            The database.
     * @param oldVersion
     *            The old database version.
     * @param newVersion
     *            The new database version.
     */
    @Override
    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion,
                                   int newVersion);
    /**
     * Called when the database has been opened. Override method should check
     * {@link SQLiteDatabase#isReadOnly} before updating the database.
     *
     * @param db
     *            The database.
     */
    public void onOpen(SQLiteDatabase db) {
    }
}
