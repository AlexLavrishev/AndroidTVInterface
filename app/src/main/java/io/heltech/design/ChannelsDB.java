package io.heltech.design;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by shadow on 21/08/17.
 */

public class ChannelsDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION=1;
    public static final String DATABASE_NAME="db_name";
    public static final String TABLE_CHANNELS="channels";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "_name";
    public static final String KEY_LCN = "_lcn";
    public static final String KEY_STREAM = "_stream";
    public static final String KEY_LOGO = "_logo";

    public ChannelsDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE  " + TABLE_CHANNELS + "(" + KEY_ID + " integer primary key, " + KEY_LCN + " integer, " + KEY_NAME + " TEXT, " + KEY_LOGO + " BLOB, " + KEY_STREAM + " TEXT )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exist " + TABLE_CHANNELS);

        onCreate(db);
    }

}