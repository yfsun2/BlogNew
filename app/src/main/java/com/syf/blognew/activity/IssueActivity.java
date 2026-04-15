package com.syf.blognew.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.donkingliang.imageselector.utils.ImageSelector;
import com.syf.blognew.R;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.pojo.UserApplication;
import com.syf.blognew.pojo.entity.User;
import com.syf.blognew.pojo.req.BlogAddReq;
import com.syf.blognew.util.UploadUtil;
import com.syf.blognew.websocket.WebSocketManager;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class IssueActivity extends AppCompatActivity{

    private static final int REQUEST_CODE = 100;
    private EditText text;
    private Button btnBack, btnIssue;
    private GridLayout layout_images;
    private final List<Uri> imageUriList = new ArrayList<>();
    private static final int MAX_IMAGES = 9;
    private int itemSize;
    private ArrayList<String> imagePathList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);
        imagePathList=new ArrayList<>();
        text = findViewById(R.id.text);
        btnBack = findViewById(R.id.back);
        btnIssue = findViewById(R.id.issue);
        layout_images = findViewById(R.id.layout_images);

        btnBack.setOnClickListener(v -> finish());

        btnIssue.setOnClickListener(v -> {
            publishBlog(imagePathList,Build.BRAND+" "+Build.MODEL,String.valueOf(text.getText()), WebSocketManager.getInstance().getUser().getId());
        });

        // 等布局加载完成再计算大小
        layout_images.post(() -> {
            calculateItemSize();
            refreshImagesLayout();
        });
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    /**
     * 【修复】根据 GridLayout 真实宽度计算，3张正好一行
     */
    private void calculateItemSize() {
        int parentWidth = layout_images.getWidth();
        int hSpace = dp2px(3);
        itemSize = (parentWidth - hSpace * 2 * 3) / 3;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    v.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v instanceof EditText) {
            int[] loc = new int[2];
            v.getLocationInWindow(loc);
            int left = loc[0], top = loc[1];
            int right = left + v.getWidth(), bottom = top + v.getHeight();
            return !(event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }

    public void openGallery(){
        ImageSelector.builder()
                .useCamera(true) // 设置是否使用拍照
                .setSingle(false)  //设置是否单选
                .setMaxSelectCount(9) // 图片的最大选择数量，小于等于0时，不限数量。
                .setSelected(imagePathList) // 把已选的图片传入默认选中。
                .canPreview(true) //是否可以预览图片，默认为true
                .start(this, REQUEST_CODE); // 打开相册
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && data != null) {
            //获取选择器返回的数据
            ArrayList<String> images = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT);
            imagePathList=images;
            /**
             * 是否是来自于相机拍照的图片，
             * 只有本次调用相机拍出来的照片，返回时才为true。
             * 当为true时，图片返回的结果有且只有一张图片。
             */
            boolean isCameraImage = data.getBooleanExtra(ImageSelector.IS_CAMERA_IMAGE, false);
            if (images != null && !images.isEmpty()) {
                for (String path : images) {
                    // 把路径转成 Uri，加入你的 imageUriList
                    Uri uri = Uri.parse("file://" + path);
                    if (!imageUriList.contains(uri) && imageUriList.size() < 9) {
                        imageUriList.add(uri);
                    }
                }
                // 刷新你的九宫格布局
                refreshImagesLayout();
            }
        }
    }
    private void refreshImagesLayout() {
        layout_images.removeAllViews();
        for (Uri uri : imageUriList) addImageItem(uri);
        if (imageUriList.size() < MAX_IMAGES) addAddButtonItem();
    }

    private void addImageItem(Uri uri) {
        FrameLayout itemView = (FrameLayout) getLayoutInflater().inflate(R.layout.item_image, null);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = itemSize;
        params.height = itemSize;
        int margin = dp2px(3);
        params.setMargins(margin, margin, margin, margin);
        itemView.setLayoutParams(params);

        ImageView ivImage = itemView.findViewById(R.id.iv_image);
        ImageView ivDelete = itemView.findViewById(R.id.iv_delete);
        ivImage.setImageURI(uri);

        ivDelete.setOnClickListener(v -> {
            imageUriList.remove(uri);
            refreshImagesLayout();
        });

        layout_images.addView(itemView);
    }

    private void addAddButtonItem() {
        Button btn = new Button(this);
        btn.setText("照片/视频");
        btn.setBackgroundColor(0xFFEEEEEE);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = itemSize;
        params.height = itemSize;
        int margin = dp2px(3);
        params.setMargins(margin, margin, margin, margin);
        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> openGallery()); // 【修复】用传统图库

        layout_images.addView(btn);
    }

    private void publishBlog(List<String> imagePathList, String model, String context, int userId) {
        List<String> urlList = new ArrayList<>();
        AtomicInteger count = new AtomicInteger(0);

        // 循环上传所有图片
        for (String path : imagePathList) {
            UploadUtil.uploadImage(path, new UploadUtil.UploadCallback() {
                @Override
                public void onSuccess(String url) {
                    urlList.add(url);
                    if (count.incrementAndGet() == imagePathList.size()) {
                        // 全部上传完成 → 发布博客
                        doPublishBlog(model, context, userId, urlList);
                    }
                }

                @Override
                public void onFail(String msg) {
                    runOnUiThread(() -> ToastHandler.showToast("上传失败"));
                }
            });
        }
    }

    private void doPublishBlog(String model, String context, int userId, List<String> urlList) {
        BlogAddReq req=new BlogAddReq();
        req.setModel(model);
        req.setContext(context);
        req.setUserId(userId);
        req.setImages(urlList);
        RequestBody body = RequestBody.create(JSON.toJSONString(req), MediaType.parse("application/json; charset=utf-8"));
        NetClient.post(ApiConstant.BLOG_ADD, body, new NetCallBack() {
            @Override
            public void onFailure(int code, String msg) {
                runOnUiThread(()-> ToastHandler.showToast(msg));
            }

            @Override
            public void onSuccess(String json) {
                runOnUiThread(()-> ToastHandler.showToast("发布成功"));
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}