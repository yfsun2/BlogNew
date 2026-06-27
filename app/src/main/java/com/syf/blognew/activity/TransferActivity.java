package com.syf.blognew.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson2.JSONObject;
import com.bumptech.glide.Glide;
import com.syf.blognew.R;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.dialog.PasswordDialog;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.pojo.req.TransferReq;
import com.syf.blognew.util.ImageUtil;
import com.syf.blognew.websocket.WebSocketManager;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class TransferActivity extends AppCompatActivity {

    private EditText etMoney;
    private Integer toUserId;
    int point=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        String userName= getIntent().getStringExtra("userName");
        toUserId=getIntent().getIntExtra("toUserId",0);
        String url=getIntent().getStringExtra("url");
        point=getIntent().getIntExtra("point",-1);

        etMoney = findViewById(R.id.et_money);
        if(point!=-1){
            etMoney.setText(String.valueOf(point));
        }
        Button btnTransfer = findViewById(R.id.btn_transfer);

        ImageView avatar=findViewById(R.id.iv_avatar);

        if(url!=null&&!url.isEmpty()){
            Glide.with(this).load(url).into(avatar);
        }else{
            assert userName != null;
            avatar.setImageBitmap(ImageUtil.getBitmapByFirst(String.valueOf(userName.charAt(0))));
        }


        TextView tv_username=findViewById(R.id.tv_username);
        tv_username.setText(userName);

        // 转账按钮点击
        btnTransfer.setOnClickListener(v -> {
            String money = etMoney.getText().toString().trim();
            if (TextUtils.isEmpty(money) || "0".equals(money)) {
                Toast.makeText(TransferActivity.this, "请输入正确金额", Toast.LENGTH_SHORT).show();
                return;
            }
            // 弹出密码输入框
            showPasswordDialog(money);
        });
    }

    // 显示密码弹窗
    private void showPasswordDialog(String money) {
        PasswordDialog dialog = new PasswordDialog(this);
        dialog.setMoney(money);
        dialog.setOnPasswordInputListener(password -> {
            if(point==-1){
                transfer(password,Integer.parseInt(money));
            }else{
                payToUser(password,Integer.parseInt(money));
            }
        });
        dialog.show();
    }

    //消息转账
    private void transfer(String password,int money){
        Runnable payThread=()->{
            TransferReq req=new TransferReq();
            req.setPassword(password);
            req.setScore(money);
            req.setToId(toUserId);

            NetClient.post(ApiConstant.TRANSFER, req, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(()->ToastHandler.showToast(msg));
                }

                @Override
                public void onSuccess(String json) {
                    //转账成功
                    Intent intent=new Intent("PAY_SUCCESS");
                    intent.putExtra("money",money);
                    sendBroadcast(intent);
                    finish();
                }
            });
        };
        new Thread(payThread).start();
    }

    private void payToUser(String password, int money) {
        Runnable th=()->{
            TransferReq req=new TransferReq();
            req.setPassword(password);
            req.setScore(money);
            req.setToId(toUserId);
            NetClient.post(ApiConstant.PAY_BY_RECEIVE, req,new NetCallBack() {
                @Override
                public void onSuccess(String data) {
                    runOnUiThread(() ->  {
                        ToastHandler.showToast("支付成功");
                        finish();
                    });
                }

                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(() ->  ToastHandler.showToast(msg));
                }
            });
        };
        new Thread(th).start();
    }
}