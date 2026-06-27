package com.syf.blognew.activity;


import static com.syf.blognew.service.BackgroundNotificationService.ACTION_NEW_MESSAGE;
import static java.lang.Math.max;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.syf.blognew.R;
import com.syf.blognew.adapter.AbstractMultiAdapter;
import com.syf.blognew.adapter.MessageAdapter;
import com.syf.blognew.pojo.vo.MessageVO;
import com.syf.blognew.pojo.PageResult;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.util.UnReadManager;
import com.syf.blognew.websocket.WebSocketManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ChatActivity extends AppCompatActivity{
    private EditText etContent;//输入框
    private ListView lvMessage;//消息列表
    private Button btnSend;//发送按钮
    private final List<MessageVO> messageList = new ArrayList<>();//消息数据
    private MessageAdapter messageAdapter;//消息适配器
    private GridView gvMediaPanel;
    private String userName,url;
    private int toUserId;
    private int current=1;
    private final int size=20;
    private int keyboardHeight = 0; // 缓存键盘高度
    private boolean isMediaPanelShow = false; // 多媒体面板是否显示
    private PopupWindow popupWindow;
    
    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msgJson = intent.getStringExtra("message");
            if (TextUtils.isEmpty(msgJson)) return;
            JSONObject obj = JSONObject.parseObject(msgJson);
            Integer fromId = obj.getInteger("fromId");
            if(fromId==null) return;
            // 判断：这条消息是不是发给当前聊天的,这要根据对方Id判断
            if (fromId == toUserId) {
                showMessageToListView(msgJson);
            }
        }
    };

    private final BroadcastReceiver paySuccessReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int money=intent.getIntExtra("money",0);
            MessageVO chatMessage=new MessageVO();
            chatMessage.setContent(Integer.toString(money));
            chatMessage.setIsMeSend(1);
            chatMessage.setIsReceive(0);
            chatMessage.setMsgType(1);
            chatMessage.setIsRead(2);
            chatMessage.setCreateTime(LocalDateTime.now());
            chatMessage.setUserName(WebSocketManager.getInstance().getUser().getName());
            chatMessage.setUrl(WebSocketManager.getInstance().getUser().getUrl());

            JSONObject receipt = new JSONObject();
            receipt.put("type", "chat");
            receipt.put("msgType",1);//转账
            receipt.put("toId", toUserId);
            receipt.put("msg",Integer.toString(money));
            if(!WebSocketManager.getInstance().sendMessageToServer(receipt.toJSONString())) {
                chatMessage.setIsRead(3);
            }
            messageList.add(chatMessage);
            messageAdapter.notifyDataSetChanged();
            lvMessage.setSelection(messageList.size());
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
        registerReceiver(messageReceiver, new IntentFilter(ACTION_NEW_MESSAGE));
        registerReceiver(paySuccessReceiver, new IntentFilter("PAY_SUCCESS"));
        //向服务器发送,当前用户点开了与toId的对话框
        JSONObject receipt = new JSONObject();
        receipt.put("type", "online");
        receipt.put("toId", toUserId);
        receipt.put("msg",1);
        WebSocketManager.getInstance().sendMessageToServer(receipt.toJSONString());

        findViewById();
        initMediaPanel();
        initChatMsgListView();

        pageMessage(toUserId, current);
    }

    private void showMessageToListView(String message){
        JSONObject json = JSONObject.parseObject(message);
        String content=json.getString("msg");
        String type=json.getString("type");
        Integer msgType=json.getInteger("msgType");
        if(Objects.equals(type, "chat")){
            Integer msgId=json.getInteger("msgId");
            MessageVO chatMessage=new MessageVO();
            chatMessage.setContent(content);
            chatMessage.setIsMeSend(0);
            chatMessage.setIsRead(1);
            chatMessage.setMsgType(msgType);
            chatMessage.setUserName(userName);
            chatMessage.setCreateTime(LocalDateTime.now());
            chatMessage.setUrl(url);
            chatMessage.setId(msgId);
            messageList.add(chatMessage);
            messageAdapter.notifyDataSetChanged();
            lvMessage.setSelection(messageList.size());
        }else if(Objects.equals(type, "send")){
            Integer msgId=json.getInteger("msgId");
            messageList.get(messageList.size()-1).setIsRead(0);
            messageList.get(messageList.size()-1).setId(msgId);
            messageAdapter.notifyDataSetChanged();
        }else if(Objects.equals(type,"count")){
            int count=Integer.parseInt(content);
            UnReadManager.getInstance().friendUnreadCount=max(0,UnReadManager.getInstance().friendUnreadCount-count);
            if (UnReadManager.getInstance().onUnreadChangeListener != null) {
                UnReadManager.getInstance().onUnreadChangeListener.run();
            }
            for(int i=1;i<=Math.min(count,messageList.size());i++)
                messageList.get(messageList.size()-i).setIsRead(1);
            messageAdapter.notifyDataSetChanged();
        }else if(Objects.equals(type,"transfer")){
            MessageVO message1=new MessageVO();
            for(MessageVO vo:messageList){
                if(String.valueOf(vo.getId()).equals(content)){
                    vo.setIsReceive(1);
                    message1.setCreateTime(LocalDateTime.now());
                    message1.setContent(vo.getContent());
                    message1.setUrl(url);
                    message1.setIsReceive(2);
                    message1.setMsgType(1);
                    message1.setUserName(userName);
                    message1.setIsMeSend(0);
                }
            }
            messageList.add(message1);
            messageAdapter.notifyDataSetChanged();
            lvMessage.setSelection(messageList.size());
        }else if(Objects.equals(type,"withdraw")){
            Integer msgId=json.getInteger("msgId");
            for(MessageVO vo:messageList){
                if(vo.getId().equals(msgId)){
                    vo.setIsWithdraw(1);
                    break;
                }
            }
            messageAdapter.notifyDataSetChanged();
        }
    }

    private void findViewById() {
        TextView textView=findViewById(R.id.tv_groupOrContactName);
        textView.setText(userName);
        RefreshLayout refreshLayout = findViewById(R.id.smart_refresh_layout);
        lvMessage = findViewById(R.id.lv_chat_msg);
        btnSend = findViewById(R.id.btn_send);
        etContent = findViewById(R.id.et_content);
        ImageView ivPlus = findViewById(R.id.btn_multimedia);
        gvMediaPanel=findViewById(R.id.gv_media_panel);
        ImageButton btn_back = findViewById(R.id.iv_return);
        btn_back.setOnClickListener(v-> finish());

        ivPlus.setOnClickListener(v -> {
            if (isMediaPanelShow) {
                // 当前显示面板 → 隐藏面板，显示输入法
                showKeyboard(etContent);
                hideMediaPanel();

            } else {
                // 当前显示输入法 → 隐藏输入法，显示面板
                showMediaPanel();
                hideKeyboard(etContent);
            }
            isMediaPanelShow = !isMediaPanelShow;
        });

        btnSend.setOnClickListener(v -> {
            String content = etContent.getText().toString();
            MessageVO chatMessage=new MessageVO();
            chatMessage.setContent(content);
            chatMessage.setIsMeSend(1);
            chatMessage.setIsRead(2);
            chatMessage.setCreateTime(LocalDateTime.now());
            chatMessage.setUserName(WebSocketManager.getInstance().getUser().getName());
            chatMessage.setUrl(WebSocketManager.getInstance().getUser().getUrl());

            JSONObject receipt = new JSONObject();
            receipt.put("type", "chat");
            receipt.put("toId", toUserId);
            receipt.put("msgType",0);
            receipt.put("msg",content);
            if(!WebSocketManager.getInstance().sendMessageToServer(receipt.toJSONString())) {
                chatMessage.setIsRead(3);
            }
            messageList.add(chatMessage);
            messageAdapter.notifyDataSetChanged();
            lvMessage.setSelection(messageList.size());
            etContent.setText("");
        });

        // 下拉：加载更早历史
        refreshLayout.setOnRefreshListener(v -> {
            current++;
            loadMoreHistory();
            v.finishRefresh(100);
        });

        // 禁用上拉
        refreshLayout.setEnableLoadMore(false);

        getWindow().getDecorView().getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    Rect r = new Rect();
                    getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                    int screenH = getWindow().getDecorView().getHeight();
                    // 实时键盘高度
                    int keyH = screenH - r.bottom;

                    // 过滤无效高度（排除导航栏、状态栏）
                    if (keyH > dp2px()) {
                        keyboardHeight = keyH;
                        // 面板高度 强制 = 键盘高度
                        android.view.ViewGroup.LayoutParams params = gvMediaPanel.getLayoutParams();
                        params.height = keyboardHeight;
                        gvMediaPanel.setLayoutParams(params);
                    }
                });

        etContent.setOnClickListener(v->{
            hideMediaPanel();
            showKeyboard(etContent);
        });

        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(etContent.getText().toString().isEmpty()){
                    btnSend.setVisibility(ListView.GONE);
                }else{
                    btnSend.setVisibility(ListView.VISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void initChatMsgListView(){
        messageAdapter =new MessageAdapter(messageList);
        messageAdapter.setMultiItemTypeSupportListener(new AbstractMultiAdapter.MultiItemTypeSupportListener() {
            @Override
            public int getItemViewType(int position) {
                return messageList.get(position).getIsMeSend()==1 ? 0 : 1;
            }

            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getLayoutId(int position) {
                return messageList.get(position).getIsMeSend()==1 ?
                        R.layout.item_chat_send_text :
                        R.layout.item_chat_receive_text;
            }
        });

        messageAdapter.setOnRetryClickListener(new MessageAdapter.OnMessageClickListener() {
            @Override
            public void onRetryClick(int position, MessageVO failedMessage) {
                String content=messageList.get(position).getContent();
                JSONObject receipt = new JSONObject();
                receipt.put("type", "chat");
                receipt.put("toId", toUserId);
                messageList.remove(position);

                MessageVO chatMessage=new MessageVO();
                chatMessage.setContent(content);
                chatMessage.setIsMeSend(1);
                chatMessage.setIsRead(WebSocketManager.getInstance().sendMessageToServer(receipt.toJSONString()) ? 2 : 3);
                chatMessage.setCreateTime(LocalDateTime.now());
                chatMessage.setUserName(WebSocketManager.getInstance().getUser().getName());
                chatMessage.setUrl(WebSocketManager.getInstance().getUser().getUrl());

                messageList.add(chatMessage);
                messageAdapter.notifyDataSetChanged();
                lvMessage.setSelection(messageList.size());
                etContent.setText("");
            }

            @Override
            public void onTransferClick(int position, MessageVO transferMessage) {
                //向websocket发送信息
                JSONObject receipt=new JSONObject();
                receipt.put("type", "transfer");
                receipt.put("toId", toUserId);//收了toUserId给我的积分
                receipt.put("msg",messageList.get(position).getId());//转账消息ID
                WebSocketManager.getInstance().sendMessageToServer(receipt.toJSONString());

                messageList.get(position).setIsReceive(1);//未收变为已收款

                MessageVO message=new MessageVO();
                message.setContent(transferMessage.getContent());
                message.setUrl(WebSocketManager.getInstance().getUser().getUrl());
                message.setMsgType(transferMessage.getMsgType());
                message.setIsMeSend(1);
                message.setIsReceive(2);
                message.setCreateTime(LocalDateTime.now());
                message.setIsRead(1);
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
                lvMessage.setSelection(messageList.size());
            }

            @Override
            public void onLongClick(View view,int position, MessageVO message) {
                showPopMenu(view,position);
            }
        });

        lvMessage.setAdapter(messageAdapter);

        // 点击空白关闭弹窗
        lvMessage.setOnItemClickListener((parent, view, position, id) -> dismissPop());
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
                List<MessageVO> chatMessageList=messageVOPageResult.getRecords().stream().map(data->{
                    MessageVO chatMessage=new MessageVO();
                    chatMessage.setId(data.getId());
                    chatMessage.setContent(data.getContent());
                    chatMessage.setCreateTime(data.getCreateTime());
                    chatMessage.setUrl(data.getUrl());
                    chatMessage.setMsgType(data.getMsgType());
                    chatMessage.setIsReceive(data.getIsReceive());
                    chatMessage.setUserName(data.getUserName());
                    chatMessage.setIsRead(data.getIsRead());
                    chatMessage.setIsMeSend(Objects.equals(data.getFromUid(), WebSocketManager.getInstance().getUser().getId()) ? 1 : 0);
                    return chatMessage;
                }).collect(Collectors.toList());
                if(chatMessageList.isEmpty()) {
                    runOnUiThread(()->ToastHandler.showToast("没有更多消息了"));
                    return;
                }
                runOnUiThread(()-> {
                    Collections.reverse(chatMessageList);
                    messageList.addAll(0, chatMessageList);
                    messageAdapter.notifyDataSetChanged();
                    lvMessage.setSelection(chatMessageList.size());
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销消息监听
        unregisterReceiver(messageReceiver);
        unregisterReceiver(paySuccessReceiver);
        //向服务器发送,当前用户离开了toId的对话框
        JSONObject receipt = new JSONObject();
        receipt.put("type", "online");
        receipt.put("toId", toUserId);
        receipt.put("msg",0);
        WebSocketManager.getInstance().sendMessageToServer(receipt.toJSONString());
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
                List<MessageVO> chatMessageList=messageVOPageResult.getRecords().stream().map(data->{
                    MessageVO chatMessage=new MessageVO();
                    chatMessage.setId(data.getId());
                    chatMessage.setContent(data.getContent());
                    chatMessage.setCreateTime(data.getCreateTime());
                    chatMessage.setMsgType(data.getMsgType());
                    chatMessage.setUrl(data.getUrl());
                    chatMessage.setUserName(data.getUserName());
                    chatMessage.setIsRead(data.getIsRead());
                    chatMessage.setIsReceive(data.getIsReceive());
                    chatMessage.setIsWithdraw(data.getIsWithdraw());
                    chatMessage.setIsMeSend(Objects.equals(data.getFromUid(), WebSocketManager.getInstance().getUser().getId()) ? 1 : 0);
                    return chatMessage;
                }).collect(Collectors.toList());

                Collections.reverse(chatMessageList);
                messageList.addAll(chatMessageList);
                runOnUiThread(()->{
                    messageAdapter.notifyDataSetChanged();
                    lvMessage.setSelection(messageList.size());
                });
            }
        });
    }

    // dp转px
    private int dp2px() {
        float density = getResources().getDisplayMetrics().density;
        return (int) (120 * density + 0.5f);
    }

    private void initMediaPanel() {
        SimpleAdapter adapter = getSimpleAdapter();
        gvMediaPanel.setAdapter(adapter);
        gvMediaPanel.setOnItemClickListener((parent, view, position, id) -> {
            // position 就是点击的下标：0=位置，1=转账，2=相册，3=拍摄
            switch (position) {
                case 0:
                    // 点击 位置
//                    onLocationClick();
                    break;
                case 1:
                    // 点击 转账 → 直接弹你之前的密码框！
                    onTransferClick();
                    break;
                case 2:
                    // 点击 相册
//                    onAlbumClick();
                    break;
                case 3:
                    // 点击 拍摄
//                    onCameraClick();
                    break;
            }
        });
    }

    @NonNull
    private SimpleAdapter getSimpleAdapter() {
        List<HashMap<String, Object>> data = new ArrayList<>();
        // 图标和文字，自行替换资源
        int[] icons = {R.mipmap.ic_location, R.mipmap.ic_transfer, R.mipmap.ic_album, R.mipmap.ic_camera};
        String[] names = {"位置", "转账", "相册", "拍摄"};

        for (int i = 0; i < icons.length; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("icon", icons[i]);
            map.put("name", names[i]);
            data.add(map);
        }

        return new SimpleAdapter(this, data, R.layout.item_media,
                new String[]{"icon", "name"}, new int[]{R.id.iv_icon, R.id.tv_name});
    }

    // 显示多媒体面板
    private void showMediaPanel() {
        if (keyboardHeight == 0) return;
        gvMediaPanel.setVisibility(View.VISIBLE);
        // 切换模式，避免布局跳动
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    // 隐藏多媒体面板
    private void hideMediaPanel() {
        gvMediaPanel.setVisibility(View.GONE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    // 显示输入法
    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            view.requestFocus();
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    // 隐藏输入法
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void onTransferClick(){
        Intent intent=new Intent(this, TransferActivity.class);
        intent.putExtra("userName",userName);
        intent.putExtra("toUserId",toUserId);
        intent.putExtra("url",url);
        startActivity(intent);
    }

    // 消息气泡菜单
    private void showPopMenu(View itemView, int position) {
        dismissPop();
        @SuppressLint("InflateParams") View popView = LayoutInflater.from(this).inflate(R.layout.pop_menu, null);
        LinearLayout tvCancel = popView.findViewById(R.id.tv_cancel);
        LinearLayout tvDelete = popView.findViewById(R.id.tv_delete);

        // 判断是否超过2分钟，隐藏撤回
        boolean overTwoMin = Math.abs(Duration.between(messageList.get(position).getCreateTime(), LocalDateTime.now()).getSeconds()) >= 120;
        if (overTwoMin) {
            tvCancel.setVisibility(View.GONE);
        } else {
            tvCancel.setVisibility(View.VISIBLE);
        }

        // 撤回监听
        tvCancel.setOnClickListener(v -> {
            withdraw(position);
            dismissPop();
        });
        // 删除监听
        tvDelete.setOnClickListener(v -> {
            messageList.remove(position);
            messageAdapter.notifyDataSetChanged();
            dismissPop();
        });

        // 先测量popView真实宽高
        popView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popWidth = popView.getMeasuredWidth();
        int popHeight = popView.getMeasuredHeight();

        // 获取屏幕高度
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        // 获取item在屏幕上的坐标
        int[] location = new int[2];
        itemView.getLocationOnScreen(location);
        int itemY = location[1];
        int itemHeight = itemView.getHeight();
        int itemWidth = itemView.getWidth();

        popupWindow = new PopupWindow(
                popView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // 关键：判断Item在屏幕上半部分还是下半部分
        if (itemY > screenHeight / 2) {
            // 下半屏：弹窗显示在Item 上方
            popView.setBackgroundResource(R.mipmap.ic_bubble_up);
            popupWindow.showAsDropDown(itemView, -popWidth/2+itemWidth/2, -popHeight - itemHeight);
        } else {
            // 上半屏：弹窗显示在Item 下方
            popView.setBackgroundResource(R.mipmap.ic_bubble_down);
            popupWindow.showAsDropDown(itemView, -popWidth/2+itemWidth/2, 0);
        }
    }

    private void dismissPop() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    //消息撤回
    private void withdraw(int position){
        Runnable withdrawThread=()-> NetClient.get(ApiConstant.MESSAGE_WITHDRAW + messageList.get(position).getId(), new NetCallBack() {
            @Override
            public void onFailure(int code, String msg) {
                runOnUiThread(()-> ToastHandler.showToast("撤回失败"));
            }

            @Override
            public void onSuccess(String json) {
                runOnUiThread(()->{
                    messageList.get(position).setIsWithdraw(1);
                    messageAdapter.notifyDataSetChanged();
                });
            }
        });
        new Thread(withdrawThread).start();
    }
}