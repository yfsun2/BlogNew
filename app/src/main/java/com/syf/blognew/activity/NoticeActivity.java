package com.syf.blognew.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.syf.blognew.R;
import com.syf.blognew.adapter.NoticeAdapter;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.pojo.vo.NoticeVO;
import com.syf.blognew.websocket.WebSocketManager;

import java.util.ArrayList;
import java.util.List;

public class NoticeActivity extends AppCompatActivity {

    private ListView lvNotice;
    private List<NoticeVO> noticeList;
    private NoticeAdapter noticeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        lvNotice = findViewById(R.id.lv_notice);
        noticeList = new ArrayList<>();

        loadNotice();
        noticeAdapter = new NoticeAdapter(this, noticeList);
        lvNotice.setAdapter(noticeAdapter);

        TextView ivBack=findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v->{
            finish();
        });
        // 监听回调
        noticeAdapter.setOnNoticeOperateListener(new NoticeAdapter.OnNoticeOperateListener() {
            @Override
            public void onFriendOperate(int position, boolean isAgree) {
                // 好友申请 同意/拒绝 业务逻辑
                if(isAgree){
                    agree(position);
                }else{
                    refuse(position);
                }
                noticeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onGiftSend(int position) {
                Runnable setThread=()->{
                    NetClient.get(ApiConstant.NOTICE_GOODS_SEND + noticeList.get(position).getId(), new NetCallBack() {
                        @Override
                        public void onFailure(int code, String msg) {
                            runOnUiThread(()->ToastHandler.showToast(msg));
                        }
                        @Override
                        public void onSuccess(String json) {
                            runOnUiThread(()->{
                                ToastHandler.showToast("物品已发货");
                                noticeList.get(position).setGoodsStatus(1);
                                noticeAdapter.notifyDataSetChanged();
                            });
                        }
                    });
                };
                new Thread(setThread).start();
            }

            @Override
            public void onGiftFinish(int position) {
                Runnable setThread=()->{
                    NetClient.get(ApiConstant.NOTICE_GOODS_FINISH + noticeList.get(position).getId(), new NetCallBack() {
                        @Override
                        public void onFailure(int code, String msg) {
                            runOnUiThread(()->ToastHandler.showToast(msg));
                        }
                        @Override
                        public void onSuccess(String json) {
                            runOnUiThread(()->{
                                ToastHandler.showToast("物品已签收");
                                noticeList.get(position).setGoodsStatus(2);
                                noticeAdapter.notifyDataSetChanged();
                            });
                        }
                    });
                };
                new Thread(setThread).start();
            }

            @Override
            public void onGiftRefuse(int position) {
                Runnable setThread=()->{
                    NetClient.get(ApiConstant.NOTICE_GOODS_REFUSE + noticeList.get(position).getId(), new NetCallBack() {
                        @Override
                        public void onFailure(int code, String msg) {
                            runOnUiThread(()->ToastHandler.showToast(msg));
                        }
                        @Override
                        public void onSuccess(String json) {
                            runOnUiThread(()->{
                                ToastHandler.showToast("已拒绝发货");
                                noticeList.get(position).setGoodsStatus(3);
                                noticeAdapter.notifyDataSetChanged();
                            });
                        }
                    });
                };
                new Thread(setThread).start();
            }

            @Override
            public void onItemClick(int position) {
                // 点击条目标记为已读
                noticeList.get(position).setIsRead(1);
                noticeAdapter.notifyDataSetChanged();

                Runnable setReadThread=()->{
                    NetClient.get(ApiConstant.NOTICE_SET_READ + noticeList.get(position).getId(), new NetCallBack() {
                        @Override
                        public void onFailure(int code, String msg) {
                            runOnUiThread(()->ToastHandler.showToast(msg));
                        }

                        @Override
                        public void onSuccess(String json) {

                        }
                    });
                };
                new Thread(setReadThread).start();

            }
        });
    }

    private void loadNotice(){
        String url;
        if(WebSocketManager.getInstance().getUser().getPower().equals("ADMIN")){
            url=ApiConstant.NOTICE_LIST_ALL;
        }else{
            url=ApiConstant.NOTICE_LIST;
        }
        Runnable loadThread=()->{
            NetClient.get(url, new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(()-> ToastHandler.showToast(msg));
                }

                @Override
                public void onSuccess(String json) {
                    List<NoticeVO> newData= JSONObject.parseArray(json, NoticeVO.class);
                    noticeList.clear();
                    noticeList.addAll(0,newData);
                    runOnUiThread(()->{
                        noticeAdapter.notifyDataSetChanged();
                    });
                }
            });
        };
        new Thread(loadThread).start();
    }

    private void agree(int position){
        Runnable agreeThread=()->{
            NetClient.get(ApiConstant.FRIEND_AGREE + noticeList.get(position).getId(), new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(()->ToastHandler.showToast(msg));
                }

                @Override
                public void onSuccess(String json) {
                    runOnUiThread(()->{
                        ToastHandler.showToast("已同意对方好友申请");
                        noticeList.get(position).setFriendStatus(1);
                        noticeAdapter.notifyDataSetChanged();
                    });
                }
            });
        };
        new Thread(agreeThread).start();
    }

    private void refuse(Integer position){
        Runnable thread=()->{
            NetClient.get(ApiConstant.FRIEND_REFUSE + noticeList.get(position).getId(), new NetCallBack() {
                @Override
                public void onFailure(int code, String msg) {
                    runOnUiThread(()->ToastHandler.showToast(msg));
                }

                @Override
                public void onSuccess(String json) {
                    runOnUiThread(()->{
                        ToastHandler.showToast("已拒绝对方好友申请");
                        noticeList.get(position).setFriendStatus(2);
                        noticeAdapter.notifyDataSetChanged();
                    });
                }
            });
        };
        new Thread(thread).start();
    }
}