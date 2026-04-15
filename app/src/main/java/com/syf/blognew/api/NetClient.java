package com.syf.blognew.api;

import androidx.annotation.NonNull;
import com.alibaba.fastjson.JSON;
import com.syf.blognew.pojo.ResultBody;
import com.syf.blognew.interceptor.NetInterceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetClient {
    private static NetClient netClient;
    private final OkHttpClient client;
    // 单例
    private NetClient() {
        client = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new NetInterceptor())
                .build();
    }

    private static NetClient getNetClient() {
        if (netClient == null) {
            netClient = new NetClient();
        }
        return netClient;
    }

    // ====================== 统一回调（只写一次！）======================
    private static Callback createCallback(NetCallBack netCallBack) {
        return new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                netCallBack.onFailure(-1, "网络异常,请检查网络");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    netCallBack.onFailure(response.code(), "服务器未响应");
                    return;
                }

                String json = response.body().string();
                ResultBody resultBody = JSON.parseObject(json, ResultBody.class);

                if (resultBody.getCode() == 200) {
                    String data = resultBody.getData() == null ? "" : resultBody.getData().toString();
                    netCallBack.onSuccess(data);
                } else {
                    netCallBack.onFailure(resultBody.getCode(), resultBody.getMsg());
                }
            }
        };
    }

    // ====================== GET ======================
    public static void get(String url, NetCallBack netCallBack) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Call call = getNetClient().client.newCall(request);
        call.enqueue(createCallback(netCallBack)); // 复用
    }

    // ====================== POST ======================
    public static void post(String url, RequestBody body, NetCallBack netCallBack) {
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = getNetClient().client.newCall(request);
        call.enqueue(createCallback(netCallBack)); // 复用
    }

    // ====================== DELETE ======================
    public static void delete(String url, NetCallBack netCallBack) {
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        Call call = getNetClient().client.newCall(request);
        call.enqueue(createCallback(netCallBack)); // 复用
    }
}