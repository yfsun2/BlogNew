package com.syf.blognew.handler;


import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.syf.blognew.pojo.UserApplication;

public class ToastHandler extends Handler {

    public ToastHandler() {
        super(Looper.getMainLooper()); // 用主线程 Looper
    }

    public static void showToast(String msg){
        Toast.makeText(UserApplication.getAppContext(),msg,Toast.LENGTH_SHORT).show();
    }
}
