package com.gjy.chatroom2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.gjy.chatroom2.javabean.User;

public class Mysqliteopenhelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "Mysqlite.db";
    private static final String create_users = "create table users(name varchar(32),password varchar(32))";


    public Mysqliteopenhelper(@Nullable Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(create_users);
//        User u = new User("1","111");
//        register(u);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long register(User u) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", u.getName());
        cv.put("password", u.getPassword());
        long users = db.insert("users", null, cv);
        return users;
    }

    public boolean login(String name, String password) {
        SQLiteDatabase db1 = getWritableDatabase();

        //登录结果
        boolean result = false;

        Cursor users = db1.query("users", null, "name like ?", new String[]{name}, null, null, null);
        if (users != null) {
            while (users.moveToNext()) {
                //获取数据库中1列的数据，0name,1password
                String password1 = users.getString(1);
                result = password1.equals(password);
                return result;
            }
        }
        return false;
    }


    //判断用户名是否存在
    public boolean isUserExist(String s1) {
        SQLiteDatabase db1 = getWritableDatabase();
        Cursor users = db1.query("users", null, "name like ?", new String[]{s1}, null, null, null);
        if (users != null) {
            while (users.moveToNext()) {
                //获取数据库中1列的数据，0name,1password
                String name = users.getString(0);
                if (name.equals(s1)) {
                    return true;
                }
            }
        }
        return false;
    }


    //修改密码
    public int updatePassword(String name, String newPassword) {
        SQLiteDatabase db1 = getWritableDatabase();
        //填充占位符
        ContentValues cv = new ContentValues();
        cv.put("password", newPassword);
        //使用SQL语句更新数据
        int update = db1.update("users", cv, "name=?", new String[]{name});

        //db1.close();

        //返回更新结果
        //返回的整数值代表了数据库中受影响的行数（即被更新的行数）,若返回0则代表更新失败
        return update;
    }

    public int deleteUser(String name){
        SQLiteDatabase db1 = getWritableDatabase();
        int delete = db1.delete("users", "name=?", new String[]{name});
        return delete;
    }

}
