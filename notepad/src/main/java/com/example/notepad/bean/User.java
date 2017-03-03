package com.example.notepad.bean;

/**
 * Created by xionghg on 11/1/16.
 */

public class User {

    private int mUserId;

    private String mAccount;

    private String mPassword;

    public String getAccount() {
        return mAccount;
    }

    public void setAccount(String account) {
        mAccount = account;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int userId) {
        mUserId = userId;
    }
}
