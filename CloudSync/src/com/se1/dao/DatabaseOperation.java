package com.se1.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseOperation {

    // Database fields
    private static SQLiteDatabase database;
    private static MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN1,
            MySQLiteHelper.COLUMN2,MySQLiteHelper.COLUMN3,
            MySQLiteHelper.COLUMN4,MySQLiteHelper.COLUMN5,MySQLiteHelper.COLUMN6};

    public DatabaseOperation(Context context)
    {
        dbHelper = new MySQLiteHelper(context);
    }

    public static void open() throws SQLException {

        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
        }

    }

    public void close()
    {
        dbHelper.close();
    }

    public void insertUser(String emailId,String password,int loggedIn,String firstName,String lastName) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN1,emailId);
        values.put(MySQLiteHelper.COLUMN2,password);
        values.put(MySQLiteHelper.COLUMN3,loggedIn);
        values.put(MySQLiteHelper.COLUMN4,firstName);
        values.put(MySQLiteHelper.COLUMN5,lastName);
        values.put(MySQLiteHelper.COLUMN6,0);

        Log.d("emailId",emailId);
        Log.d("password",password);
        Log.d("loggedIn in insert", ""+loggedIn);
        //Log.d("MySQLiteHelper.TABLE_Name",MySQLiteHelper.TABLE_Name);
        if(MySQLiteHelper.TABLE_Name != null)
            database.insert(MySQLiteHelper.TABLE_Name, null, values);

    }
    public User getUserDetail() {
        String selectQuery = "SELECT  * FROM " + MySQLiteHelper.TABLE_Name;
        open();
        Cursor cursor = database.rawQuery(selectQuery, null);
        String[] data = null;
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            User user = newUser(cursor);
            return user;
        }

        return null;
        //System.out.println("Comment deleted with id: " + id);t
        //database.delete(MySQLiteHelper.TABLE_COMMENTS, MySQLiteHelper.COLUMN_ID
        //      + " = " + id, null);
    }

    public void addSignIn(String emailId)
    {
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.COLUMN3,1);
        database.update(MySQLiteHelper.TABLE_Name, cv, MySQLiteHelper.COLUMN1+"=?", new String[]{emailId});
    }
    public void forgotPassword(String emailId,int randomNo)
    {
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.COLUMN2,""+randomNo);
        database.update(MySQLiteHelper.TABLE_Name, cv, MySQLiteHelper.COLUMN1+"=?", new String[]{emailId});
    }
    public void editProfile(String emailId,String firstName,String lastName)
    {
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.COLUMN1,""+emailId);
        cv.put(MySQLiteHelper.COLUMN4,""+firstName);
        cv.put(MySQLiteHelper.COLUMN5,""+lastName);
        database.update(MySQLiteHelper.TABLE_Name, cv, MySQLiteHelper.COLUMN1+"=?", new String[]{emailId});
        database.update(MySQLiteHelper.TABLE_Name, cv, MySQLiteHelper.COLUMN4+"=?", new String[]{firstName});
        database.update(MySQLiteHelper.TABLE_Name, cv, MySQLiteHelper.COLUMN5+"=?", new String[]{lastName});
    }
    public void resetPassword(String emailId,String password)
    {
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.COLUMN2,password);
        database.update(MySQLiteHelper.TABLE_Name, cv, MySQLiteHelper.COLUMN1+"=?", new String[]{emailId});
    }
    public void removeSignIn(String emailId)
    {
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.COLUMN3,0);
        database.update(MySQLiteHelper.TABLE_Name, cv, MySQLiteHelper.COLUMN1+"=?", new String[]{emailId});
    }
    public void dropBoxAdded(int dropboxAdded)
    {
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.COLUMN6,dropboxAdded);
        User user=getUserDetail();
        database.update(MySQLiteHelper.TABLE_Name, cv, MySQLiteHelper.COLUMN1+"=?", new String[]{user.getEmailId()});
    }
    public boolean isdropBoxAdded()
    {
        User user=getUserDetail();
        if(user.getDropBoxAdded() == 1)
            return true;
        else return false;
    }
    private User newUser(Cursor cursor) {
        User user= new User();
        user.setEmailId(cursor.getString(0));
        user.setPassword(cursor.getString(1));
        user.setLoggedIn(cursor.getInt(2));
        user.setFirstName(cursor.getString(3));
        user.setLastName(cursor.getString(4));
        user.setDropBoxAdded(cursor.getInt(5));
        return user;

    }

}