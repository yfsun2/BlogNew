package com.syf.blognew.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.syf.blognew.R;

public abstract class QRCodeFragment extends Fragment {
    protected ImageView ivQrCode,ivBarcode;
    protected TextView tvTip,tvMoney,tvNum;
    protected EditText etPoint;
    protected LinearLayout llPoint;
    protected Button btnGenQr;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_code, container, false);
        ivQrCode = view.findViewById(R.id.iv_qr_code);
        tvTip = view.findViewById(R.id.tv_tip);
        tvMoney = view.findViewById(R.id.tv_money);
        tvNum = view.findViewById(R.id.tv_num);
        etPoint=view.findViewById(R.id.et_point);
        llPoint=view.findViewById(R.id.ll_point);
        btnGenQr=view.findViewById(R.id.btn_genqr);
        ivBarcode=view.findViewById(R.id.iv_barcode);

        initCode(); // 子类实现
        return view;
    }
    protected abstract void initCode();
}