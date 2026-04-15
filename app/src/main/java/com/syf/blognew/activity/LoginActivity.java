package com.syf.blognew.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.syf.blognew.R;
import com.syf.blognew.pojo.entity.User;
import com.syf.blognew.pojo.UserApplication;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.pojo.req.UserLoginReq;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.service.BackgroundNotificationService;
import com.syf.blognew.util.ClassUtil;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.util.SpUtil;
import com.syf.blognew.websocket.WebSocketManager;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * @author yfsun2
 * 登录页面
 */
public class LoginActivity extends AppCompatActivity implements TextWatcher {

    private EditText user,password;
    private Button loginBtn;
    private ImageView leftArm,rightArm,leftHand,rightHand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //判断之前是否存在登录信息
        if (SpUtil.isLogin()) {
            Intent intent = new Intent(UserApplication.getAppContext(), BackgroundNotificationService.class);
            startForegroundService(intent);

            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        initViews();
    }

    private void initViews(){
        user = findViewById(R.id.et_user);
        password = findViewById(R.id.et_password);
        loginBtn = findViewById(R.id.bt_login);
        Button registerBtn = findViewById(R.id.bt_register);
        leftArm = findViewById(R.id.iv_left_arm);
        rightArm = findViewById(R.id.iv_right_arm);
        leftHand = findViewById(R.id.iv_left_hand);
        rightHand = findViewById(R.id.iv_right_hand);

        //监听内容改变 -> 控制按钮的点击状态
        user.addTextChangedListener(this);
        password.addTextChangedListener(this);
        //监听EidtText的焦点变化 -> 控制是否需要捂住眼睛
        password.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                //捂住眼睛
                close();
            }else{
                //打开
                open();
            }
        });

        loginBtn.setOnClickListener(v->{
            UserLoginReq req=new UserLoginReq();
            req.setUserName(user.getText().toString());
            req.setPassword(password.getText().toString());
            RequestBody body = RequestBody.create(JSONObject.toJSONString(req),MediaType.parse("application/json; charset=utf-8"));

            NetClient.post(ApiConstant.USER_LOGIN, body, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(()-> ToastHandler.showToast(msg));
                }

                @Override
                public void onSuccess(String json) {

                    SpUtil.setToken(json);

                    NetClient.get(ApiConstant.USER_BY_NAME  + user.getText().toString(), new NetCallBack() {
                        @Override
                        public void onFailure(int code, String msg) {
                           runOnUiThread(()-> ToastHandler.showToast(msg));
                        }

                        @Override
                        public void onSuccess(String obj) {
                            User user1=JSON.parseObject(String.valueOf(obj), User.class);
                            SpUtil.login(user1);

                            Intent intent = new Intent(UserApplication.getAppContext(), BackgroundNotificationService.class);
                            startForegroundService(intent);

                            intent=new Intent();
                            intent.setClass(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            });
        });

        registerBtn.setOnClickListener(v->{
            Intent intent=new Intent();
            intent.setClass(this,RegisterActivity.class);
            startActivity(intent);
        });

    }

    /**
     * 当有控件获得焦点focus 自动弹出键盘
     * 1. 点击软键盘的enter键 自动收回键盘
     * 2. 代码控制 InputMethodManager
     *    requestFocus
     *    showSoftInput:显示键盘 必须先让这个view成为焦点requestFocus
     *    hideSoftInputFromWindow 隐藏键盘
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            //隐藏键盘
            //1.获取系统输入的管理器
            InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            //2.隐藏键盘
            inputManager.hideSoftInputFromWindow(user.getWindowToken(),0);
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
        //判断两个输入框是否有内容,决定登录按钮是否可以点击
        loginBtn.setEnabled(!user.getText().toString().isEmpty() && !password.getText().toString().isEmpty());
    }

    private void close(){
        //左边
        RotateAnimation lAnim = new RotateAnimation(0,170,leftArm.getWidth(),0f);
        lAnim.setDuration(500);
        lAnim.setFillAfter(true);

        leftArm.startAnimation(lAnim);

        RotateAnimation rAnim = new RotateAnimation(0, -170,0f,0f);
        rAnim.setDuration(500);
        rAnim.setFillAfter(true);

        rightArm.startAnimation(rAnim);

        TranslateAnimation down = (TranslateAnimation) AnimationUtils.loadAnimation(this, R.anim.translate_down);
        leftHand.startAnimation(down);
        rightHand.startAnimation(down);
    }

    private void open(){
        //左边
        RotateAnimation lAnim = new RotateAnimation(170,0,leftArm.getWidth(),0f);
        lAnim.setDuration(500);
        lAnim.setFillAfter(true);

        leftArm.startAnimation(lAnim);

        RotateAnimation rAnim = new RotateAnimation(-170,0,0f,0f);
        rAnim.setDuration(500);
        rAnim.setFillAfter(true);

        rightArm.startAnimation(rAnim);

        TranslateAnimation up = (TranslateAnimation) AnimationUtils.loadAnimation(this,R.anim.translate_up);
        leftHand.startAnimation(up);
        rightHand.startAnimation(up);
    }
}