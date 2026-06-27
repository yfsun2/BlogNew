package com.syf.blognew.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.syf.blognew.R;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.dialog.RedPacketDialog;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.websocket.WebSocketManager;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class NfcActivity extends AppCompatActivity {

    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private TextView tvTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        tvTip = findViewById(R.id.tv_tip);
        findViewById(R.id.tv_back).setOnClickListener(v->finish());
        initNFC();
    }

    private void initNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "设备不支持NFC", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "请先开启NFC", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
        }

        // ---------------- 【官方标准写法，无任何多余 flag】----------------
        mPendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, NfcActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            // 前台调度 —— 必须这样写才会把TAG传给你
            // 强制前台调度
            mNfcAdapter.enableForegroundDispatch(
                    this,
                    mPendingIntent,
                    null,
                    null
            );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    // ========== 【官方标准：能100%拿到TAG】 ==========
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 不判断、不筛选、直接暴力拿
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (tag == null) {
            ToastHandler.showToast("前台调度未生效！");
            return;
        }
        if (Objects.equals(WebSocketManager.getInstance().getUser().getPower(), "ADMIN")) {
            showWriteMoneyDialog(tag);
        } else {
            readNfcMoney(tag);
        }
    }

    // ========== 管理员：弹出输入金额对话框 写入NFC ==========
    private void showWriteMoneyDialog(Tag tag) {
        final EditText etInput = new EditText(this);
        etInput.setHint("请输入红包金额");
        etInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle("管理员写入金额")
                .setView(etInput)
                .setPositiveButton("确定写入", (dialog, which) -> {
                    String money = etInput.getText().toString().trim();
                    if (money.isEmpty()) {
                        nfcHongbao(tag, null);
                    } else {
                        nfcHongbao(tag, Integer.parseInt(money));
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ========== 写入NDEF数据到NFC标签 ==========
    private void writeNfcTag(Tag tag, String content) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                Toast.makeText(this, "该标签不支持NDEF写入", Toast.LENGTH_SHORT).show();
                return;
            }
            ndef.connect();
            if (!ndef.isWritable()) {
                Toast.makeText(this, "标签不可写入", Toast.LENGTH_SHORT).show();
                ndef.close();
                return;
            }

            NdefRecord record = NdefRecord.createTextRecord("en", content);
            NdefMessage message = new NdefMessage(record);
            ndef.writeNdefMessage(message);
            ndef.close();

            Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "写入失败", Toast.LENGTH_SHORT).show();
        }
    }

    // ========== 普通用户：读取NFC金额 弹出领红包 ==========
    private void readNfcMoney(Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                Toast.makeText(this, "无效红包标签", Toast.LENGTH_SHORT).show();
                return;
            }
            ndef.connect();
            NdefMessage msg = ndef.getNdefMessage();
            ndef.close();

            if (msg == null) {
                Toast.makeText(this, "标签无金额数据", Toast.LENGTH_SHORT).show();
                return;
            }

            // 解析金额
            NdefRecord[] records = msg.getRecords();
            String json = new String(records[0].getPayload(), StandardCharsets.UTF_8).substring(3);

            JSONObject obj = JSONObject.parseObject(json);
            String type = obj.getString("type");

            if (Objects.equals(type, "hongbao")) {
                String orderNo = obj.getString("orderNo");
                RedPacketDialog packetDialog = new RedPacketDialog(this);
                packetDialog.setOnOpenListener(() -> getHongbao(orderNo));
                packetDialog.show();
            } else {
                ToastHandler.showToast("不是本软件官方NFC红包");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "读取失败或不是红包标签", Toast.LENGTH_SHORT).show();
        }
    }

    private void nfcHongbao(Tag tag, Integer points) {
        Runnable t = () -> {
            String url = ApiConstant.NFC_HONGBAO;
            if (points != null) {
                url += "?points=" + points;
            }
            NetClient.get(url, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(() -> {
                        ToastHandler.showToast(msg);
                    });
                }

                @Override
                public void onSuccess(String json) {
                    runOnUiThread(()->{
                        writeNfcTag(tag, json);
                    });
                }
            });
        };
        new Thread(t).start();
    }

    public void getHongbao(String orderNo) {
        Runnable thread = () -> {
            NetClient.get(ApiConstant.GET_HONGBAO + orderNo, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(() -> ToastHandler.showToast(msg));
                }

                @Override
                public void onSuccess(String json) {
                    runOnUiThread(() -> {
                        int num = Integer.parseInt(json);
                        if (num == 0) {
                            ToastHandler.showToast("未中奖");
                        } else {
                            ToastHandler.showToast("领取成功," + json + "积分已到账");
                        }
                    });
                }
            });
        };
        new Thread(thread).start();
    }
}