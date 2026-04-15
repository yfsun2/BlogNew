package com.syf.blognew.activity;


import static java.lang.Math.max;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.syf.blognew.R;
import com.syf.blognew.adapter.AbstractMultiAdapter;
import com.syf.blognew.adapter.MessageAdapter;
import com.syf.blognew.pojo.vo.ChatMessage;
import com.syf.blognew.pojo.vo.MessageVO;
import com.syf.blognew.pojo.PageResult;
import com.syf.blognew.pojo.UserApplication;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.service.BackgroundNotificationService;
import com.syf.blognew.util.UnReadManager;
import com.syf.blognew.websocket.WebSocketManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ChatActivity extends AppCompatActivity{
    private EditText et_content;
    private ListView listView;
    private Button btn_send;
    private final List<ChatMessage> mData = new ArrayList<>();
    private MessageAdapter mAdapter;
    private String userName,url;
    private int toUserId;
    private int current=1;
    private final int size=20;

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msgJson = intent.getStringExtra("message");
            if (TextUtils.isEmpty(msgJson)) return;
            JSONObject obj = JSONObject.parseObject(msgJson);
            int fromId = obj.getInteger("fromId");
            // 判断：这条消息是不是发给当前聊天的,这要根据对方Id判断
            if (fromId == toUserId) {
                showMessage(msgJson);
            }
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //聊天对象
        toUserId = getIntent().getIntExtra("userId",1);
        userName= getIntent().getStringExtra("userName");
        url= getIntent().getStringExtra("url");
        // 注册全局消息监听

        registerReceiver(mMessageReceiver, new IntentFilter("NEW_MESSAGE"));

        //向服务器发送,当前用户点开了toId的对话框
        JSONObject receipt = new JSONObject();
        receipt.put("type", "online");
        receipt.put("toId", toUserId);
        receipt.put("msg",1);
        sendMessageToServer(receipt.toJSONString());

        TextView textView=findViewById(R.id.tv_groupOrContactName);
        textView.setText(userName);
        findViewById();
        initView();
        initChatMsgListView();
        pageMessage(toUserId, current);
    }

    private boolean sendMessageToServer(String msg) {
        if (WebSocketManager.getInstance().getWebSocketClient() != null && WebSocketManager.getInstance().getWebSocketClient().isOpen()) {
            WebSocketManager.getInstance().getWebSocketClient().send(msg);
            return true;
        }else {
            ToastHandler.showToast("连接已断开");
            return false;
        }
    }

    private void showMessage(String message){
        JSONObject json = JSONObject.parseObject(message);
        String content=json.getString("msg");
        String type=json.getString("type");

        if(Objects.equals(type, "chat")){
            ChatMessage chatMessage=new ChatMessage();
            chatMessage.setContent(content);
            chatMessage.setIsMeSend(0);
            chatMessage.setIsRead(1);
            chatMessage.setUserName(userName);
            chatMessage.setTime(new Date());
            chatMessage.setUrl(url);
            mData.add(chatMessage);
            mAdapter.notifyDataSetChanged();
            listView.setSelection(mData.size());

//            Intent intent = new Intent(BackgroundNotificationService.ACTION_SHOW_NOTIFICATION);
//            intent.putExtra(BackgroundNotificationService.EXTRA_TITLE, "Blog来了一条新消息");
//            intent.putExtra(BackgroundNotificationService.EXTRA_CONTENT, userName+":"+content);
//            sendBroadcast(intent);

        }else if(Objects.equals(type, "send")){
            mData.get(mData.size()-1).setIsRead(0);
            mAdapter.notifyDataSetChanged();
        }else{
            int count=Integer.parseInt(content);
            UnReadManager.getInstance().friendUnreadCount=max(0,UnReadManager.getInstance().friendUnreadCount-count);
            if (UnReadManager.getInstance().onUnreadChangeListener != null) {
                UnReadManager.getInstance().onUnreadChangeListener.run();
            }
            for(int i=1;i<=count;i++)
                mData.get(mData.size()-i).setIsRead(1);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void findViewById() {
        RefreshLayout refreshLayout = findViewById(R.id.smart_refresh_layout);
        listView = findViewById(R.id.chatmsg_listView);
        btn_send = findViewById(R.id.btn_send);
        et_content = findViewById(R.id.et_content);
        ImageButton btn_back = findViewById(R.id.iv_return);
        btn_back.setOnClickListener(v-> finish());

        btn_send.setOnClickListener(v -> {
            String content = et_content.getText().toString();
            ChatMessage chatMessage=new ChatMessage();
            chatMessage.setContent(content);
            chatMessage.setIsMeSend(1);
            chatMessage.setIsRead(2);
            chatMessage.setTime(new Date());
            chatMessage.setUserName(WebSocketManager.getInstance().getUser().getName());
            chatMessage.setUrl(WebSocketManager.getInstance().getUser().getUrl());

            JSONObject receipt = new JSONObject();
            receipt.put("type", "chat");
            receipt.put("toId", toUserId);
            receipt.put("msg",content);
            if(!sendMessageToServer(receipt.toJSONString())) chatMessage.setIsRead(3);
            mData.add(chatMessage);
            mAdapter.notifyDataSetChanged();
            listView.setSelection(mData.size());
            et_content.setText("");
        });

        // 下拉：加载更早历史
        refreshLayout.setOnRefreshListener(v -> {
            current++;
            loadMoreHistory();
            v.finishRefresh(100);
        });

        // 禁用上拉
        refreshLayout.setEnableLoadMore(false);
    }

    private void initView() {
        et_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                btn_send.setEnabled(!et_content.getText().toString().isEmpty());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void initChatMsgListView(){
        mAdapter=new MessageAdapter(mData);
        mAdapter.setMultiItemTypeSupportListener(new AbstractMultiAdapter.MultiItemTypeSupportListener() {
            @Override
            public int getItemViewType(int position) {
                return mData.get(position).getIsMeSend()==1 ? 0 : 1;
            }

            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getLayoutId(int position) {
                return mData.get(position).getIsMeSend()==1 ?
                        R.layout.item_chat_send_text :
                        R.layout.item_chat_receive_text;
            }
        });

        mAdapter.setOnRetryClickListener((position, failedMessage) -> {
            String content=mData.get(position).getContent();
            JSONObject receipt = new JSONObject();
            receipt.put("type", "chat");
            receipt.put("toId", toUserId);
            mData.remove(position);

            ChatMessage chatMessage=new ChatMessage();
            chatMessage.setContent(content);
            chatMessage.setIsMeSend(1);
            chatMessage.setIsRead(sendMessageToServer(receipt.toJSONString()) ? 2 : 3);
            chatMessage.setTime(new Date());
            chatMessage.setUserName(WebSocketManager.getInstance().getUser().getName());
            chatMessage.setUrl(WebSocketManager.getInstance().getUser().getUrl());

            mData.add(chatMessage);
            mAdapter.notifyDataSetChanged();
            listView.setSelection(mData.size());
            et_content.setText("");
        });

        listView.setAdapter(mAdapter);
    }

    private void loadMoreHistory() {
        NetClient.get(ApiConstant.MESSAGE_PAGE + toUserId +","+current + "," + size, new NetCallBack() {
            @Override
            public void onFailure(int code, String msg) {
                runOnUiThread(()->ToastHandler.showToast(msg));
            }

            @Override
            public void onSuccess(String json) {
                PageResult<MessageVO> messageVOPageResult=JSONObject.parseObject(json, new TypeReference<>() {});
                List<ChatMessage> chatMessageList=messageVOPageResult.getRecords().stream().map(data->{
                    ChatMessage chatMessage=new ChatMessage();
                    chatMessage.setContent(data.getContent());
                    chatMessage.setTime(data.getCreateTime());
                    chatMessage.setUrl(data.getUrl());
                    chatMessage.setUserName(data.getUserName());
                    chatMessage.setIsRead(1);
                    chatMessage.setIsMeSend(Objects.equals(data.getFromUid(), WebSocketManager.getInstance().getUser().getId()) ? 1 : 0);
                    return chatMessage;
                }).collect(Collectors.toList());
                if(chatMessageList.isEmpty()) {
                    runOnUiThread(()->ToastHandler.showToast("没有更多消息了"));
                    return;
                }
                runOnUiThread(()-> {
                    Collections.reverse(chatMessageList);
                    mData.addAll(0, chatMessageList);
                    mAdapter.notifyDataSetChanged();
                    listView.setSelection(chatMessageList.size());
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销消息监听
        unregisterReceiver(mMessageReceiver);
        //向服务器发送,当前用户离开了toId的对话框
        JSONObject receipt = new JSONObject();
        receipt.put("type", "online");
        receipt.put("toId", toUserId);
        receipt.put("msg",0);
        sendMessageToServer(receipt.toJSONString());
    }

    private void pageMessage(int toId,int current){
        NetClient.get(ApiConstant.MESSAGE_PAGE + toId+","+current + "," + size, new NetCallBack() {
            @Override
            public void onFailure(int code, String msg) {
                runOnUiThread(()->ToastHandler.showToast(msg));
            }

            @Override
            public void onSuccess(String json) {
                PageResult<MessageVO> messageVOPageResult=JSONObject.parseObject(json, new TypeReference<>() {});
                List<ChatMessage> chatMessageList=messageVOPageResult.getRecords().stream().map(data->{
                    ChatMessage chatMessage=new ChatMessage();
                    chatMessage.setContent(data.getContent());
                    chatMessage.setTime(data.getCreateTime());
                    chatMessage.setUrl(data.getUrl());
                    chatMessage.setUserName(data.getUserName());
                    chatMessage.setIsRead(data.getIsRead());
                    chatMessage.setIsMeSend(Objects.equals(data.getFromUid(), WebSocketManager.getInstance().getUser().getId()) ? 1 : 0);
                    return chatMessage;
                }).collect(Collectors.toList());

                Collections.reverse(chatMessageList);
                mData.addAll(chatMessageList);
                runOnUiThread(()->{
                    mAdapter.notifyDataSetChanged();
                    listView.setSelection(mData.size());
                });
            }
        });
    }

}