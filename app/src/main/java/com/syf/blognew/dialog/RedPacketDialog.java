package com.syf.blognew.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.syf.blognew.R;

public class RedPacketDialog extends Dialog {

    private OnOpenListener listener;

    public interface OnOpenListener {
        // 拆开红包回调
        void onOpen();
    }

    public void setOnOpenListener(OnOpenListener listener) {
        this.listener = listener;
    }

    public RedPacketDialog(Context context) {
        super(context, android.R.style.Theme_NoTitleBar_Fullscreen);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_red_packet, null);
        setContentView(view);

        // 设置弹窗透明背景、点击外部不关闭
        setCanceledOnTouchOutside(false);

        TextView btnOpen = view.findViewById(R.id.btn_open_packet);
        ImageView ivClose = view.findViewById(R.id.iv_close);

        // 关闭弹窗
        ivClose.setOnClickListener(v -> dismiss());

        // 开红包点击
        btnOpen.setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onOpen();
            }
        });
    }
}