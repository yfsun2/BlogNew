package com.syf.blognew.activity;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import com.journeyapps.barcodescanner.CaptureActivity;

/**
 * 强制竖屏的扫码界面
 */
public class MyCaptureActivity extends CaptureActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 强制竖屏（这句直接锁死，不会有警告）
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
    }
}