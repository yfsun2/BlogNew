package com.syf.blognew.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.syf.blognew.R;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.util.ScratchCardGenerator;

public class GglActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button btnGen,btnCheck;
    ScratchCardGenerator generator;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ggl);
        context=this;
        imageView = findViewById(R.id.imageView);
        findViewById(R.id.tv_back).setOnClickListener(v->finish());
        btnGen=findViewById(R.id.btn_gen);
        btnCheck=findViewById(R.id.btn_check);
        generator= new ScratchCardGenerator(this);
        btnGen.setOnClickListener(v->{
            updateScore(0,5);
        });

        btnCheck.setOnClickListener(v->{
            btnGen.setEnabled(true);
            btnCheck.setEnabled(false);
            check();
        });

    }

    public void check(){
        int money=generator.getMoneyInt();
        if(money>0){
            updateScore(1,money);
        }else{
            ToastHandler.showToast("未中奖");
        }
    }

    public void updateScore(int type,int moneyInt){
        Runnable thread=()->{
            NetClient.get(ApiConstant.UPDATE_SCORE + "?type=" + type + "&score=" + moneyInt, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(()-> ToastHandler.showToast(msg));
                }

                @Override
                public void onSuccess(String json) {
                    runOnUiThread(()->{
                        if (type == 0) {
                            Bitmap card= generator.generateScratchCard(context);
                            imageView.setImageBitmap(card);
                            btnGen.setEnabled(false);
                            btnCheck.setEnabled(true);
                        }
                        else if(type==1){
                            ToastHandler.showToast("恭喜您，中奖"+moneyInt+"积分");
                        }
                    });

                }
            });
        };
        new Thread(thread).start();
    }
}