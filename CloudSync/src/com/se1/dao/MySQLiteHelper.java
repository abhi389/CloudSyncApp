package com.se1.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_Name = "User";
    public static final String COLUMN1 = "emaiId";
    public static final String COLUMN2 = "password";
    public static final String COLUMN3 = "loggedIn";
    public static final String COLUMN4 = "FirstName";
    public static final String COLUMN5 = "LastName";
    public static final String COLUMN6 = "DropBoxAdded";

    private static final String DATABASE_NAME = "User.db";
    private static final int DATABASE_VERSION = 5;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_Name + "(" + COLUMN1
            + " text not null, "+COLUMN2
            + " text not null, "+ COLUMN3
            + " INT not null, "+ COLUMN4
            + " text not null, "+ COLUMN5
            + " text not null, "+ COLUMN6
            + " INT not null);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Name);
        onCreate(db);
    }

}
