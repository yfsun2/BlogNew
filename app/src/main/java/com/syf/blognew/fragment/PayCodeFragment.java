package com.syf.blognew.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.syf.blognew.R;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.util.BarcodeUtil;
import com.syf.blognew.util.QrUtil;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PayCodeFragment extends QRCodeFragment {

    private Timer refreshTimer;
    private TimerTask timerTask;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(
                getResources().getColor(R.color.pay_green)
        );
    }

    @Override
    protected void initCode() {
        tvTip.setText("向商家付款，请出示此付款码");
        llPoint.setVisibility(View.GONE);
        ivBarcode.setVisibility(View.VISIBLE);
        tvNum.setVisibility(View.VISIBLE);
    }

    private void startRefresh() {
        stopRefresh();
        refreshTimer = new Timer();
        timerTask=new TimerTask() {
            @Override
            public void run() {
                getPayQrCode();
            }
        };
        refreshTimer.schedule(timerTask,0,60*1000);
    }

    private void stopRefresh() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer.purge();
            refreshTimer = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRefresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        startRefresh();
    }

    private void getPayQrCode() {
        Runnable thread=()->{
            NetClient.get(ApiConstant.GET_PAY_QRCODE, new NetCallBack() {
                @Override
                public void onSuccess(String data) {
                    Map<String, String> map = JSON.parseObject(data, new TypeReference<Map<String, String>>() {});
                    String qrCode = map.get("qrCode");
                    String barcode = map.get("barcode");
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    requireActivity().runOnUiThread(() ->  {
                        ivQrCode.setImageBitmap(QrUtil.createQRCode(qrCode, 240,null));
                        ivBarcode.setImageBitmap(BarcodeUtil.createBarCode(barcode,480,80));
                        tvNum.setText(barcode);
                    });
                }

                @Override
                public void onFailure(int code, String msg) {
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    requireActivity().runOnUiThread(()->{
                        ToastHandler.showToast("获取付款码失败");
                    });
                }
            });
        };
        new Thread(thread).start();
    }
}