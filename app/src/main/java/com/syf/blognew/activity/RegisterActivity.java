 package com.syf.blognew.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.syf.blognew.R;
import com.syf.blognew.pojo.SendMailReq;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.pojo.req.UserAddReq;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.util.EmailValidator;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.util.RandomStringUtil;

import okhttp3.MediaType;
import okhttp3.RequestBody;

 public class RegisterActivity extends AppCompatActivity implements TextWatcher {

    private EditText name,password,password2,email,code;
    private Button send,ok,reset;
    private String verifyCode;
    private Context mContext;
    private int num=60;
    private boolean isSend=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        mContext=this;
        initView();

        reset.setOnClickListener(v->{
            setReset();
        });

        send.setOnClickListener(v->{
            //发送验证码前检查两次密码输入是否一致
            if(!password.getText().toString().equals(password2.getText().toString())){
                ToastHandler.showToast("两次密码输入不一致");
                password.setText("");
                password2.setText("");
            }else if(!EmailValidator.isValidEmail(email.getText().toString())){
                runOnUiThread(()->{
                    ToastHandler.showToast("邮箱格式不正确");
                    email.setText("");
                });
            } else{
                verifyCode= RandomStringUtil.getNumber(4);
                SendMailReq req=new SendMailReq();
                req.setEmail(email.getText().toString());
                req.setTitle("验证码");
                req.setContext("您的验证码为:"+verifyCode);

                RequestBody body=RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSONObject.toJSONString(req));
                NetClient.post(ApiConstant.SEND_MAIL, body, new NetCallBack() {
                    @Override
                    public void onFailure(int code, String msg) {
                        runOnUiThread(()->{
                            ToastHandler.showToast(msg);
                        });
                    }

                    @Override
                    public void onSuccess(String json) {
                        runOnUiThread(()->{
                            ToastHandler.showToast("发送验证码成功");
                            send.setEnabled(false);
                        });
                        isSend=true;
                    }
                });
            }
            if(isSend)new Thread(()->{
                while (num>0){
                    runOnUiThread(()->{
                        send.setText(num+"s");
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    num--;
                }
                if(num==0){
                    runOnUiThread(()->{
                        if(!email.getText().toString().isEmpty()){
                            send.setEnabled(true);
                        }
                        send.setText("发送验证码");
                    });
                    num=60;
                    isSend=false;
                }
            }).start();
        });

        ok.setOnClickListener(v->{
            if(!code.getText().toString().equals(verifyCode)){
                ToastHandler.showToast("验证码错误");
                code.setText("");
            }else {
                UserAddReq req=new UserAddReq();
                req.setEmail(email.getText().toString());
                req.setName(name.getText().toString());
                req.setPassword(password.getText().toString());
                req.setPower("用户");

                RequestBody body=RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JSONObject.toJSONString(req));

                NetClient.post(ApiConstant.USER_REGISTER, body, new NetCallBack() {
                    @Override
                    public void onFailure(int code, String msg) {
                        runOnUiThread(()->{
                            ToastHandler.showToast(msg);
                        });
                    }
                    @Override
                    public void onSuccess(String json) {
                        runOnUiThread(()->{
                            ToastHandler.showToast("注册成功");
                        });
                        Intent intent=new Intent();
                        intent.setClass(mContext,LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }

    private void initView(){
        name=findViewById(R.id.name);
        password=findViewById(R.id.password);
        password2=findViewById(R.id.password2);
        email=findViewById(R.id.email);
        code=findViewById(R.id.code);

        send=findViewById(R.id.send);
        ok=findViewById(R.id.ok);
        reset=findViewById(R.id.reset);

        name.addTextChangedListener(this);
        password.addTextChangedListener(this);
        password2.addTextChangedListener(this);
        email.addTextChangedListener(this);
        code.addTextChangedListener(this);

    }

    private void setReset(){
        name.setText("");
        password.setText("");
        password2.setText("");
        email.setText("");
        code.setText("");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            //隐藏键盘
            //1.获取系统输入的管理器
            InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            //2.隐藏键盘
            inputManager.hideSoftInputFromWindow(name.getWindowToken(),0);
            //3.取消焦点
            View focusView = getCurrentFocus();
            if (focusView != null) {
                focusView.clearFocus(); //取消焦点
            }
            //getCurrentFocus().clearFocus();
            //focusView.requestFocus();//请求焦点
        }
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        //判断两个输入框是否有内容
        ok.setEnabled(!name.getText().toString().isEmpty()
                && !password.getText().toString().isEmpty()
                && !password2.getText().toString().isEmpty()
                && !code.getText().toString().isEmpty());
        send.setEnabled(!email.getText().toString().isEmpty() && !isSend);
    }
}