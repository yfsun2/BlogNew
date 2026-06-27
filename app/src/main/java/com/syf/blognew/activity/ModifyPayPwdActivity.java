package com.syf.blognew.activity;

import android.os.Bundle;
import android.text.InputType;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson2.JSON;
import com.syf.blognew.R;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.pojo.PayPassword;

import okhttp3.MediaType;
import okhttp3.RequestBody;


public class ModifyPayPwdActivity extends AppCompatActivity {

    private EditText etOldPwd, etNewPwd, etConfirmPwd;
    private TextView ivBack;
    private ImageView ivOldEye, ivNewEye, ivConfirmEye;
    private ImageView ivOldClear, ivNewClear, ivConfirmClear;
    private TextView tvErrorTip;
    private Button btnSubmit;

    private boolean oldPwdShow = false;
    private boolean newPwdShow = false;
    private boolean confirmPwdShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_pay_pwd);
        initView();
        initListener();
        addTextWatch();
    }

    private void initView() {
        ivBack = findViewById(R.id.iv_back);

        etOldPwd = findViewById(R.id.et_old_pwd);
        etNewPwd = findViewById(R.id.et_new_pwd);
        etConfirmPwd = findViewById(R.id.et_confirm_pwd);

        ivOldEye = findViewById(R.id.iv_old_pwd_eye);
        ivNewEye = findViewById(R.id.iv_new_pwd_eye);
        ivConfirmEye = findViewById(R.id.iv_confirm_pwd_eye);

        ivOldClear = findViewById(R.id.iv_old_clear);
        ivNewClear = findViewById(R.id.iv_new_clear);
        ivConfirmClear = findViewById(R.id.iv_confirm_clear);

        tvErrorTip = findViewById(R.id.tv_error_tip);
        btnSubmit = findViewById(R.id.btn_submit);
    }

    private void initListener() {
        // 返回按钮
        ivBack.setOnClickListener(v -> finish());

        // 密码显隐
        ivOldEye.setOnClickListener(v -> {
            oldPwdShow = !oldPwdShow;
            switchPwdShow(etOldPwd, ivOldEye, oldPwdShow);
        });
        ivNewEye.setOnClickListener(v -> {
            newPwdShow = !newPwdShow;
            switchPwdShow(etNewPwd, ivNewEye, newPwdShow);
        });
        ivConfirmEye.setOnClickListener(v -> {
            confirmPwdShow = !confirmPwdShow;
            switchPwdShow(etConfirmPwd, ivConfirmEye, confirmPwdShow);
        });

        // 清空按钮
        ivOldClear.setOnClickListener(v -> etOldPwd.setText(""));
        ivNewClear.setOnClickListener(v -> etNewPwd.setText(""));
        ivConfirmClear.setOnClickListener(v -> etConfirmPwd.setText(""));

        // 提交
        btnSubmit.setOnClickListener(v -> checkPwdAndSubmit());
    }

    // 输入框内容监听，控制清空按钮显示/隐藏
    private void addTextWatch() {
        etOldPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivOldClear.setVisibility(s.length()>0 ? View.VISIBLE : View.GONE);
                tvErrorTip.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        etNewPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivNewClear.setVisibility(s.length()>0 ? View.VISIBLE : View.GONE);
                tvErrorTip.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        etConfirmPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivConfirmClear.setVisibility(s.length()>0 ? View.VISIBLE : View.GONE);
                tvErrorTip.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void switchPwdShow(EditText et, ImageView iv, boolean isShow) {
        if (isShow) {
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
            iv.setImageResource(R.mipmap.ic_eye_open);
        } else {
            et.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
            iv.setImageResource(R.mipmap.ic_eye_close);
        }
        et.setSelection(et.getText().length());
    }

    private void checkPwdAndSubmit() {
        String oldPwd = etOldPwd.getText().toString().trim();
        String newPwd = etNewPwd.getText().toString().trim();
        String confirmPwd = etConfirmPwd.getText().toString().trim();

        tvErrorTip.setVisibility(View.GONE);

        if (oldPwd.length() != 6) {
            showError("请输入6位原支付密码");
            return;
        }
//        if (!oldPwd.equals(LOCAL_OLD_PAY_PWD)) {
//            showError("原支付密码输入错误");
//            return;
//        }
        if (newPwd.length() != 6) {
            showError("新密码必须为6位数字");
            return;
        }
        if (confirmPwd.length() != 6) {
            showError("请确认6位新密码");
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            showError("两次输入的新密码不一致");
            return;
        }
        if (newPwd.equals(oldPwd)) {
            showError("新密码不能与旧密码相同");
            return;
        }

        submitModifyPwd(oldPwd,newPwd);
    }

    private void showError(String msg) {
        tvErrorTip.setText(msg);
        tvErrorTip.setVisibility(View.VISIBLE);
    }

    private void submitModifyPwd(String oldPwd,String newPwd) {

        Runnable updatePwdThread=()->{
            PayPassword password=new PayPassword(oldPwd,newPwd);
            NetClient.post(ApiConstant.UPDATE_PAY_PWD, password, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(()-> showError("原支付密码输入错误"));
                }

                @Override
                public void onSuccess(String json) {
                    runOnUiThread(()-> ToastHandler.showToast("支付密码修改成功"));
                    finish();
                }
            });
        };

        new Thread(updatePwdThread).start();
    }
}