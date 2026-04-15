package com.syf.blognew.util;

import com.alibaba.fastjson.JSONObject;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.pojo.ResultBody;

import okhttp3.*;
import java.io.File;
import java.io.IOException;

public class UploadUtil {
    private static final OkHttpClient client = new OkHttpClient();

    public interface UploadCallback {
        void onSuccess(String url);
        void onFail(String msg);
    }

    // 上传单张图片 → 返回URL
    public static void uploadImage(String filePath, UploadCallback callback) {
        File file = new File(filePath);
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("image/*")))
                .build();

        Request request = new Request.Builder()
                .url(ApiConstant.FILE_UPLOAD)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFail(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                // 解析 ResultBody 里的 data 字段（URL）
                try {
                    ResultBody obj=JSONObject.parseObject(json,ResultBody.class);
                    String url = obj.getData().toString();
                    callback.onSuccess(url);
                } catch (Exception e) {
                    callback.onFail("解析失败");
                }
            }
        });
    }
}
