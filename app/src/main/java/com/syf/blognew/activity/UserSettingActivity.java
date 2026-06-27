package com.syf.blognew.activity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.syf.blognew.R;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.pojo.PayPassword;
import com.syf.blognew.pojo.UserApplication;
import com.syf.blognew.pojo.entity.User;
import com.syf.blognew.service.BackgroundNotificationService;
import com.syf.blognew.util.SpUtil;
import com.syf.blognew.websocket.WebSocketManager;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class UserSettingActivity extends AppCompatActivity {

    private TextView tvBack, tvUsername;
    private ImageView ivAvatar;
    private View llAvatar, llUsername, llPwd;
    private static final int CODE_ALBUM = 1001;
    private static final int CODE_CAMERA = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);
        initView();
        initClick();
    }

    private void initView() {

        User currentUser= WebSocketManager.getInstance().getUser();

        tvBack = findViewById(R.id.tv_back);
        tvUsername = findViewById(R.id.tv_username);
        tvUsername.setText(currentUser.getName());
        ivAvatar = findViewById(R.id.iv_avatar);
        if(currentUser.getUrl()!=null&&!currentUser.getUrl().isEmpty()){
            Glide.with(this).load(currentUser.getUrl()).into(ivAvatar);
        }
        llAvatar = findViewById(R.id.ll_avatar);
        llUsername = findViewById(R.id.ll_username);
        llPwd = findViewById(R.id.ll_pwd);
    }

    private void initClick() {
        tvBack.setOnClickListener(v -> finish());
        llAvatar.setOnClickListener(v -> showAvatarDialog());
        llUsername.setOnClickListener(v -> showEditUsernameDialog());
        llPwd.setOnClickListener(v -> showEditPwdDialog());
    }

    private void showAvatarDialog() {
        String[] items = {"拍照", "从相册选择"};
        new AlertDialog.Builder(this)
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CODE_CAMERA);
                    } else {
                        Intent albumIntent = new Intent(Intent.ACTION_PICK);
                        albumIntent.setType("image/*");
                        startActivityForResult(albumIntent, CODE_ALBUM);
                    }
                }).show();
    }

    private void showEditUsernameDialog() {
        EditText etName = new EditText(this);
        etName.setText(tvUsername.getText());
        new AlertDialog.Builder(this)
                .setTitle("修改用户名")
                .setView(etName)
                .setPositiveButton("确定", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateUserName(this,name);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showEditPwdDialog() {
        View pwdView = getLayoutInflater().inflate(R.layout.dialog_edit_pwd, null);
        EditText etOldPwd = pwdView.findViewById(R.id.et_old_pwd);
        EditText etNewPwd = pwdView.findViewById(R.id.et_new_pwd);
        EditText etConfirmPwd = pwdView.findViewById(R.id.et_confirm_pwd);

        new AlertDialog.Builder(this)
                .setTitle("修改登录密码")
                .setView(pwdView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String oldPwd = etOldPwd.getText().toString().trim();
                    String newPwd = etNewPwd.getText().toString().trim();
                    String confirmPwd = etConfirmPwd.getText().toString().trim();

                    if (TextUtils.isEmpty(oldPwd) || TextUtils.isEmpty(newPwd) || TextUtils.isEmpty(confirmPwd)) {
                        Toast.makeText(this, "所有输入框不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPwd.equals(confirmPwd)) {
                        Toast.makeText(this, "两次新密码输入不一致", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updatePwd(this,oldPwd,newPwd);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            ivAvatar.setImageURI(uri);
        }
    }

    private void updateUserName(Context ctx, String name){
        Runnable updateThread=()->{
            NetClient.get(ApiConstant.UPDATE_USERNAME + name, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(()-> ToastHandler.showToast("修改失败"));
                }

                @Override
                public void onSuccess(String json) {
                    runOnUiThread(()-> {
                        ToastHandler.showToast("修改成功");
                        SpUtil.logout();
                        Intent intent=new Intent();
                        intent.setClass(ctx, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }
            });
        };
        new Thread(updateThread).start();
    }
    private void updatePwd(Context ctx,String oldPwd,String newPwd){
        Runnable updateThread=()->{
            PayPassword password=new PayPassword(oldPwd,newPwd);
            NetClient.post(ApiConstant.UPDATE_PWD, password, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(()-> ToastHandler.showToast(msg));
                }

                @Override
                public void onSuccess(String json) {
                    runOnUiThread(()-> {
                        ToastHandler.showToast("修改成功");
                        SpUtil.logout();
                        Intent intent=new Intent();
                        intent.setClass(ctx, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }
            });
        };
        new Thread(updateThread).start();
    }
    private void updateAvatar(String url){
        Runnable updateThread=()->{
            NetClient.get(ApiConstant.UPDATE_AVATAR + url, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {

                }

                @Override
                public void onSuccess(String json) {

                }
            });
        };
        new Thread(updateThread).start();
    }
}
