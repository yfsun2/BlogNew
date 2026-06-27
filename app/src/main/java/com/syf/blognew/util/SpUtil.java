package com.syf.blognew.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSONObject;
import com.syf.blognew.pojo.entity.User;
import com.syf.blognew.pojo.UserApplication;
import com.syf.blognew.service.BackgroundNotificationService;

public class SpUtil {
    private static final String SP_NAME = "user_info";

    // 存储token
    public static void setToken(String token) {
        Context ctx = UserApplication.getAppContext();
        SharedPreferences sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString("token", token).apply();
    }

    // 获取token
    public static String getToken() {
        Context ctx = UserApplication.getAppContext();
        SharedPreferences sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString("token", "");
    }

    // 退出登录清空
    public static void clearToken() {
        Context ctx = UserApplication.getAppContext();
        ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .edit().remove("token").apply();
    }

    // 登录成功保存
    public static void login(User user) {
        Context ctx = UserApplication.getAppContext();
        ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString("user", JSONObject.toJSONString(user))
                .putBoolean("is_login", true)
                .apply();
    }

    // 是否已经登录
    public static boolean isLogin() {
        Context ctx = UserApplication.getAppContext();
        return ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .getBoolean("is_login", false);
    }

    public static User getUser(){
        Context ctx=UserApplication.getAppContext();
        String jsonStr=ctx.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE).getString("user","");
        if(jsonStr.isEmpty()) return null;
        return JSONObject.parseObject(jsonStr,User.class);
    }

    // 退出登录
    public static void logout() {
        Context ctx=UserApplication.getAppContext();
        ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        UserApplication.getAppContext().sendBroadcast(new Intent(BackgroundNotificationService.ACTION_LOGOUT));
    }
}