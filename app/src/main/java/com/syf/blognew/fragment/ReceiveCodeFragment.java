package com.syf.blognew.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.syf.blognew.R;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.util.QrUtil;
import com.syf.blognew.websocket.WebSocketManager;

public class ReceiveCodeFragment extends QRCodeFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(
                getResources().getColor(R.color.receive_yellow)
        );
    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void initCode() {
        tvTip.setText("向我付款，请扫描此收款码");
        llPoint.setVisibility(View.VISIBLE);
        ivBarcode.setVisibility(View.GONE);
        tvNum.setVisibility(View.GONE);
        tvMoney.setVisibility(View.GONE);
        getReceiveQrCode(0);
        btnGenQr.setOnClickListener(v->{
            if(!etPoint.getText().toString().isEmpty()){
                tvMoney.setVisibility(View.VISIBLE);
                tvMoney.setText("$"+etPoint.getText().toString());
                getReceiveQrCode(Integer.parseInt(etPoint.getText().toString()));
            }else{
                tvMoney.setVisibility(View.GONE);
                getReceiveQrCode(0);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getReceiveQrCode(0);
    }

    private void getReceiveQrCode(int point) {
        Runnable thread=()->{
            NetClient.get(ApiConstant.GET_RECEIVE_QRCODE+point, new NetCallBack() {
                @Override
                public void onSuccess(String data) {
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    requireActivity().runOnUiThread(() ->  ivQrCode.setImageBitmap(QrUtil.createQRCode(data, 240, WebSocketManager.getInstance().getUser().getUrl())));
                }

                @Override
                public void onFailure(int code, String msg) {
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> {
                        ToastHandler.showToast("获取收款码失败");
                    });
                }
            });
        };
        new Thread(thread).start();
    }
}