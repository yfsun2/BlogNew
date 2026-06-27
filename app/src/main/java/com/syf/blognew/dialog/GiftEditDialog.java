package com.syf.blognew.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.syf.blognew.R;
import com.syf.blognew.pojo.entity.Gift;

import java.net.URI;
import java.util.Objects;

public class GiftEditDialog extends Dialog {

    private OnGiftOperateListener listener;
    private ImageView ivGiftIcon;
    private LinearLayout layoutUploadHint;
    private EditText etGiftName, etGiftCount, etGiftScore;
    private final Gift gift;

    public interface OnGiftOperateListener {
        void onUploadImage();
        void onSave(Gift gift);
    }

    public GiftEditDialog(@NonNull Context context,Gift gift) {
        super(context);
        if(gift==null){
            this.gift=new Gift(null,"","",0,0);
            initDialogView("添加礼物信息");
        }else {
            this.gift=gift;
            initDialogView("编辑礼物信息");
        }
    }
    public void setOnGiftOperateListener(OnGiftOperateListener listener){
        this.listener = listener;
    }

    @SuppressLint("SetTextI18n")
    private void initDialogView(String title) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_gift, null);
        setContentView(rootView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
        setCanceledOnTouchOutside(true);

        // 绑定控件
        FrameLayout flUploadBox = rootView.findViewById(R.id.fl_upload_box);
        ivGiftIcon = rootView.findViewById(R.id.iv_gift_icon);
        TextView tvTitle = rootView.findViewById(R.id.gift_dialog_title);
        tvTitle.setText(title);
        layoutUploadHint = rootView.findViewById(R.id.layout_upload_hint);
        etGiftName = rootView.findViewById(R.id.et_gift_name);
        etGiftName.setText(gift.getName());
        etGiftCount = rootView.findViewById(R.id.et_gift_count);
        etGiftCount.setText(gift.getCount().toString());
        etGiftScore = rootView.findViewById(R.id.et_gift_score);
        etGiftScore.setText(gift.getNeedScore().toString());

        setImageUrl(gift.getUrl());

        Button btnCancel = rootView.findViewById(R.id.btn_cancel);
        Button btnConfirm = rootView.findViewById(R.id.btn_confirm);

        // 点击上传图片
        flUploadBox.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUploadImage();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
        btnConfirm.setOnClickListener(v -> checkDataAndSubmit());
    }

    // 上传图片成功后设置预览+URL
    public void setImageUri(Uri uri) {
        if(uri==null|| Objects.requireNonNull(uri.getPath()).isEmpty()) return;
        layoutUploadHint.setVisibility(View.GONE);
        ivGiftIcon.setVisibility(View.VISIBLE);
        Glide.with(getContext())
                .load(uri)
                .centerCrop()
                .into(ivGiftIcon);
    }
    public void setImageUrl(String url) {
        if(url==null||url.isEmpty()) return;
        layoutUploadHint.setVisibility(View.GONE);
        ivGiftIcon.setVisibility(View.VISIBLE);
        Glide.with(getContext())
                .load(url)
                .centerCrop()
                .into(ivGiftIcon);
    }
    // 数据校验
    private void checkDataAndSubmit() {
        String name = etGiftName.getText().toString().trim();
        String countStr = etGiftCount.getText().toString().trim();
        String scoreStr = etGiftScore.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "请输入礼物名称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (countStr.isEmpty()) {
            Toast.makeText(getContext(), "请输入礼物数量", Toast.LENGTH_SHORT).show();
            return;
        }
        if (scoreStr.isEmpty()) {
            Toast.makeText(getContext(), "请输入兑换积分", Toast.LENGTH_SHORT).show();
            return;
        }
//        if (giftImageUrl.isEmpty()) {
//            Toast.makeText(getContext(), "请先上传礼物图片", Toast.LENGTH_SHORT).show();
//            return;
//        }

        int count = Integer.parseInt(countStr);
        int score = Integer.parseInt(scoreStr);

        if (listener != null) {
            Gift updateGift=new Gift(gift.getId(),name,gift.getUrl(),score,count);
            listener.onSave(updateGift);
        }
        dismiss();
    }
}
