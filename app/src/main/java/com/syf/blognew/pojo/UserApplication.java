package com.syf.blognew.pojo;

import android.app.Application;
import android.content.Context;

import java.util.TimeZone;

import lombok.Getter;


public class UserApplication extends Application {

    @Getter
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }
}
