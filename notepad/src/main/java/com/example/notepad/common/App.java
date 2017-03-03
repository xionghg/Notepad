package com.example.notepad.common;

import android.database.sqlite.SQLiteDatabase;

import org.litepal.LitePalApplication;
import org.litepal.tablemanager.Connector;

/**
 * Created by daxiong on 2016/10/27.
 */

public class App extends LitePalApplication {

    public static final String TAG = "bill.lia";

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化数据库
        SQLiteDatabase db = Connector.getDatabase();
    }
}