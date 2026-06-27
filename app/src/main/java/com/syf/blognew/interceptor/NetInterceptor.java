package com.syf.blognew.interceptor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.syf.blognew.activity.LoginActivity;
import com.syf.blognew.pojo.UserApplication;
import com.syf.blognew.service.BackgroundNotificationService;
import com.syf.blognew.util.SpUtil;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;


public class NetInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        //请求拦截带token访问
        Request originalRequest = chain.request();
        String token = SpUtil.getToken();
        Request.Builder requestBuilder = originalRequest.newBuilder();
        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", token);
        }
        Request newRequest = requestBuilder.build();

        Response response = chain.proceed(newRequest);
        if(response.code()==401){
            SpUtil.logout();
            Context context = UserApplication.getAppContext();
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(this::showToastAndLogin);
            } else {
                new Handler(Looper.getMainLooper()).post(this::showToastAndLogin);
            }
        }
        return response;
    }

    private void showToastAndLogin() {
        Toast.makeText(UserApplication.getAppContext(), "您的账号已下线，请先登录", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(UserApplication.getAppContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        UserApplication.getAppContext().startActivity(intent);
    }
}