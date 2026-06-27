package com.syf.blognew.activity;

import static com.syf.blognew.R.*;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.donkingliang.imageselector.utils.ImageSelector;
import com.syf.blognew.R;
import com.syf.blognew.adapter.GiftAdapter;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.dialog.GiftEditDialog;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.pojo.entity.Gift;
import com.syf.blognew.pojo.entity.User;
import com.syf.blognew.util.UploadUtil;
import com.syf.blognew.websocket.WebSocketManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class GiftActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 23;
    private GiftAdapter adapter;
    private List<Gift> giftList;
    private ArrayList<String> imagePathList=new ArrayList<>();
    private final List<Uri> imageUriList = new ArrayList<>();
    private int userPoints = 0;
    TextView tvUserPoints,continuous;
    List<TextView> signList;
    private Button signBtn;
    private final int[] scores={8,18,28,38,68,88,188,188,288,288,288,388,388,388,588};
    private GiftEditDialog giftEditDialog;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift);

        tvUserPoints=findViewById(R.id.tv_user_points);
        tvUserPoints.setText(String.valueOf(userPoints));

        continuous=findViewById(R.id.tv_continuous);
        findViewById(R.id.btn_ggl).setOnClickListener(v -> {
            Intent intent=new Intent(this, GglActivity.class);
            startActivity(intent);
        });

        TextView back= findViewById(id.tv_back);
        back.setOnClickListener(v-> finish());
        TextView addGift=findViewById(id.add_gift);
        addGift.setOnClickListener(v-> saveOrUpdateGift(null));

        if(Objects.equals(WebSocketManager.getInstance().getUser().getPower(), "ADMIN")){
            addGift.setVisibility(RecyclerView.VISIBLE);

        }else{
            addGift.setVisibility(RecyclerView.INVISIBLE);
        }

        RecyclerView recyclerGifts = findViewById(id.recycler_gifts);
        recyclerGifts.setLayoutManager(new LinearLayoutManager(this));

        giftList = new ArrayList<>();

        LinearLayout layoutSignContainer = findViewById(id.ll_sign_scroll);

        signList=new ArrayList<>();

        // 循环创建15天签到UI
        for (int i = 0; i < 15; i++) {
            // 加载每个签到天的布局
            View itemView = getLayoutInflater().inflate(R.layout.item_sign_day, layoutSignContainer, false);

            TextView tvDay = itemView.findViewById(R.id.tv_day);
            TextView tvPoints = itemView.findViewById(R.id.tv_points);

            int day = i + 1;
            tvDay.setText(String.valueOf(day));
            signList.add(tvDay);
            tvPoints.setText("+" + scores[i]);

            // 第15天特殊大奖样式
            if (i == 14) {
                tvDay.setBackgroundResource(R.drawable.bg_sign_big);
                tvDay.setTextColor(0xFFFFFFFF);
                tvPoints.setTextColor(0xFFFF9500);
            }

            // 添加到容器
            layoutSignContainer.addView(itemView);
        }

        signBtn=findViewById(R.id.btn_sign);
        signBtn.setOnClickListener(v-> sign());
        adapter = new GiftAdapter(this, giftList, userPoints);
        adapter.setOnExchangeClickListener(new GiftAdapter.onExchangeClickListener() {
            @Override
            public void onExchangeClick(int position) {
                exchangeGift(position);
            }

            @Override
            public void onEditClick(int position) {
                saveOrUpdateGift(giftList.get(position));
            }
            @Override
            public void onDeleteClick(int position){
                deleteGift(position);
            }
        });
        recyclerGifts.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        userinfo();
        loadGiftData();
    }

    private void loadGiftData() {

        Runnable loadThread=()->{
            NetClient.get(ApiConstant.GIFT_LIST, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(()->ToastHandler.showToast(msg));
                }

                @Override
                public void onSuccess(String json) {
                    List<Gift> newData= JSONObject.parseArray(json, Gift.class);
                    giftList.clear();
                    giftList.addAll(0,newData);
                    runOnUiThread(()->adapter.notifyDataSetChanged());
                }
            });
        };
        new Thread(loadThread).start();
    }

    private void exchangeGift(int position){
        Runnable exchangeGiftThread=()->{
            NetClient.get(ApiConstant.GIFT_EXCHANGE+giftList.get(position).getId(), new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(()->ToastHandler.showToast("兑换失败"));
                }

                @Override
                public void onSuccess(String json) {
                    runOnUiThread(()->{
                        ToastHandler.showToast("兑换成功，请在消息通知里查看最新状态");
                    });
                    userinfo();
                    loadGiftData();
                }
            });
        };
        new Thread(exchangeGiftThread).start();
    }

    private void saveOrUpdateGift(Gift gift){
        imageUriList.clear();
        imagePathList.clear();
        giftEditDialog = new GiftEditDialog(GiftActivity.this,gift);
        giftEditDialog.setOnGiftOperateListener(new GiftEditDialog.OnGiftOperateListener() {
            @Override
            public void onUploadImage() {
                openGallery();
            }

            @Override
            public void onSave(Gift updateGift) {
                Runnable uploadThread=()->{
                    AtomicInteger count = new AtomicInteger(0);
                    if(!imagePathList.isEmpty()){
                        UploadUtil.uploadImage(imagePathList.get(0), new UploadUtil.UploadCallback() {
                            @Override
                            public void onSuccess(String url) {
                                if (count.incrementAndGet() == imagePathList.size()) {
                                    updateGift.setUrl(url);
                                    Runnable updateThread=()->{
                                        NetClient.post(ApiConstant.GIFT_UPDATE, updateGift, new NetCallBack() {
                                            @Override
                                            public void onFailure(int code, String msg) {
                                                runOnUiThread(()->ToastHandler.showToast(msg));
                                            }

                                            @Override
                                            public void onSuccess(String json) {
                                                loadGiftData();
                                            }
                                        });
                                    };
                                    new Thread(updateThread).start();
                                }
                            }

                            @Override
                            public void onFail(String msg) {
                                runOnUiThread(() -> ToastHandler.showToast("上传失败"));
                            }
                        });
                    }else{
                        Runnable updateThread=()->{
                            NetClient.post(ApiConstant.GIFT_UPDATE, updateGift, new NetCallBack() {
                                @Override
                                public void onFailure(int code, String msg) {
                                    runOnUiThread(()->ToastHandler.showToast(msg));
                                }

                                @Override
                                public void onSuccess(String json) {
                                    loadGiftData();
                                }
                            });
                        };
                        new Thread(updateThread).start();
                    }

                };
                new Thread(uploadThread).start();
            }
        });
        giftEditDialog.show();
    }

    private void deleteGift(int position){



        Runnable deleteThread=()->{
            NetClient.delete(ApiConstant.GIFT_DELETE + giftList.get(position).getId(), new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(()->ToastHandler.showToast("删除失败"));
                }

                @Override
                public void onSuccess(String json) {
                    runOnUiThread(()->{
                        ToastHandler.showToast("删除成功");
                        loadGiftData();
                    });
                }
            });
        };

        // 构建 Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除确认"); // 标题
        builder.setMessage("确定要删除这件物品吗？删除后无法恢复！"); // 提示内容

        // 确认删除
        builder.setPositiveButton("确定删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 在这里执行真正的删除逻辑
                new Thread(deleteThread).start();
            }
        });

        // 取消
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.dismiss(); // 关闭弹窗
        });

        // 显示 Dialog
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // 点击空白处不关闭弹窗（更严谨）
        dialog.show();
    }

    private void userinfo(){
        Runnable signCheckThread=()->{
            NetClient.get(ApiConstant.USER_INFO, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(()->ToastHandler.showToast(msg));
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(String json) {
                    User user= JSONObject.parseObject(json, User.class);
                    runOnUiThread(()->{
                        userPoints=user.getScore();
                        tvUserPoints.setText(String.valueOf(userPoints));
                        adapter.updateUserPoints(userPoints);
                        String content = "连续签到15天得3040积分，已签到" + user.getConsecutiveDays() + "/15天";
                        SpannableString spannable = new SpannableString(content);
                        int start = content.indexOf("3040");
                        int end = start + "3040".length();
                        ForegroundColorSpan orangeSpan = new ForegroundColorSpan(0xFFFF9500);
                        spannable.setSpan(orangeSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        continuous.setText(spannable);
                        if(user.getLastSignDate()!=null&&user.getLastSignDate().equals(LocalDate.now())){//今天签到过了
                            signBtn.setEnabled(false);
                            signBtn.setText("今日已签到");
                            for(int i=0;i<15;i++){
                                if(i<user.getConsecutiveDays()){
                                    signList.get(i).setBackgroundResource(R.drawable.bg_sign_checked);
                                    signList.get(i).setTextColor(Color.WHITE);
                                } else if(i<14){
                                    signList.get(i).setBackgroundResource(R.drawable.bg_sign_normal);
                                    signList.get(i).setTextColor(Color.parseColor("#666666"));
                                }
                            }
                        }else {//今天待签到
                            signBtn.setEnabled(true);
                            signBtn.setText("立即签到");
                            for(int i=0;i<15;i++){
                                if(i<user.getConsecutiveDays()){
                                    signList.get(i).setBackgroundResource(R.drawable.bg_sign_checked);
                                    signList.get(i).setTextColor(Color.WHITE);
                                } else if(i==user.getConsecutiveDays()){
                                    signList.get(i).setBackgroundResource(R.drawable.bg_sign_today);
                                    signList.get(i).setTextColor(Color.parseColor("#FF9500"));
                                } else if(i<14){
                                    signList.get(i).setBackgroundResource(R.drawable.bg_sign_normal);
                                    signList.get(i).setTextColor(Color.parseColor("#666666"));
                                }
                            }
                        }
                    });
                }
            });
        };
        new Thread(signCheckThread).start();
    }

    //签到
    private void sign(){
        Runnable signThread=()->{
            NetClient.get(ApiConstant.SIGN, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(()->ToastHandler.showToast(msg));
                }

                @Override
                public void onSuccess(String json) {
                    runOnUiThread(()->ToastHandler.showToast("签到成功"));
                    userinfo();
                }
            });
        };
        new Thread(signThread).start();
    }

    public void openGallery(){
        ImageSelector.builder()
                .useCamera(true) // 设置是否使用拍照
                .setSingle(true)  //设置是否单选
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
            }
            if(!imageUriList.isEmpty())giftEditDialog.setImageUri(imageUriList.get(0));
        }
    }

}