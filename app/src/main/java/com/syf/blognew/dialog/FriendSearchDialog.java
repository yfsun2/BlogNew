package com.syf.blognew.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.syf.blognew.R;
import com.syf.blognew.adapter.FriendSearchAdapter;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.pojo.entity.User;
import com.syf.blognew.pojo.vo.UserVO;

import java.util.List;

public class FriendSearchDialog {

    private Context mContext;
    private Dialog mDialog;
    private EditText etSearchKey;
    private ListView lvResult;

    public FriendSearchDialog(Context context) {
        this.mContext = context;
        initDialog();
    }

    private void initDialog() {
        // 初始化Dialog
        mDialog = new Dialog(mContext);
        View dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_search_friend, null);
        mDialog.setContentView(dialogView);
        // 设置弹窗宽度铺满
        mDialog.getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        // 关键：去掉 dialog 默认的背景、边框、留白
        mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // 绑定控件
        etSearchKey = dialogView.findViewById(R.id.et_search_key);
        Button btnSearch = dialogView.findViewById(R.id.btn_search);
        ImageView btnCancel = dialogView.findViewById(R.id.iv_close);
        lvResult = dialogView.findViewById(R.id.lv_search_result);

        View emptyView = LayoutInflater.from(mContext).inflate(R.layout.empty_view, null);
        ((ViewGroup)lvResult.getParent()).addView(emptyView,lvResult.getLayoutParams());
        lvResult.setEmptyView(emptyView);

        btnCancel.setOnClickListener(v -> dismiss());

        // 搜索按钮点击
        btnSearch.setOnClickListener(v -> {
            String key = etSearchKey.getText().toString().trim();
            if (key.isEmpty()) {
                return;
            }
            searchFriend(key);
        });
    }

    // 搜索好友逻辑
    private void searchFriend(String searchKey) {
        Runnable queryThread=()->{
            NetClient.get(ApiConstant.USER_LIST+searchKey, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    ((AppCompatActivity)mContext).runOnUiThread(()->{
                        ToastHandler.showToast(msg);
                    });
                }

                @Override
                public void onSuccess(String json) {
                    List<UserVO> newData= JSONObject.parseArray(json,UserVO.class);
                    FriendSearchAdapter adapter = new FriendSearchAdapter(mContext,newData);
                    adapter.setOnAddFriendListener((position, bean) -> {
                        addFriend(bean.getId());
                        dismiss();
                    });
                    ((AppCompatActivity)mContext).runOnUiThread(()->{
                        lvResult.setAdapter(adapter);
                    });
                }
            });
        };
        new Thread(queryThread).start();
    }

    private void addFriend(Integer friendId){
        Runnable addThread=()->{
            NetClient.get(ApiConstant.FRIEND_ADD + friendId, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    ((AppCompatActivity)mContext).runOnUiThread(()->{
                        ToastHandler.showToast(msg);
                    });
                }

                @Override
                public void onSuccess(String json) {
                    ((AppCompatActivity)mContext).runOnUiThread(()->{
                        ToastHandler.showToast("请求成功，请等待对方同意");
                    });
                }
            });
        };
        new Thread(addThread).start();
    }

    // 显示弹窗
    public void show() {
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }
    }

    // 关闭弹窗
    public void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
}