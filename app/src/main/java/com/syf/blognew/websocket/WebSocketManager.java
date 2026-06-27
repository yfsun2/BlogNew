package com.syf.blognew.websocket;

import static com.syf.blognew.service.BackgroundNotificationService.ACTION_NEW_MESSAGE;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.pojo.UserApplication;
import com.syf.blognew.pojo.entity.User;
import com.syf.blognew.util.SpUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

import lombok.Getter;
import lombok.Setter;

public class WebSocketManager {

    private static WebSocketManager instance;
    @Getter
    private WebSocketClient webSocketClient;

    @Getter
    @Setter
    private User user;  // 当前登录用户
    public boolean isConnecting = false;
    private boolean isLogout = false; // 退出登录标记

    private final Handler handler = new Handler(Looper.getMainLooper());

    public static WebSocketManager getInstance() {
        if (instance == null) {
            synchronized (WebSocketManager.class){
                if(instance==null) instance = new WebSocketManager();
            }
        }
        return instance;
    }

    // 【关键】每次连接都获取最新用户！！！
    public void connectWebSocket() {
        // 退出登录状态，不连接
        if (isLogout) return;

        // 已连接 / 连接中 → 不重复连
        if (webSocketClient != null && webSocketClient.isOpen()) return;
        if (isConnecting) return;

        isConnecting = true;

        // 每次连接都重新获取最新用户（解决切换账号不变问题）
        if (SpUtil.getUser() != null ) {
            user = SpUtil.getUser();
        } else {
            user = null;
        }

        if (user == null || user.getId() <= 0) {
            isConnecting = false;
            return;
        }

        URI uri = URI.create("wss://api.yfsun2.com/websocketChat?fromId=" + user.getId());
        webSocketClient = new WebSocketClient(uri, new Draft_6455()) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                isConnecting = false;
                Log.e("WebSocket", "连接成功 用户ID：" + user.getId());
            }

            @Override
            public void onMessage(String message) {
                Intent intent = new Intent(ACTION_NEW_MESSAGE);
                intent.putExtra("message", message);
                UserApplication.getAppContext().sendBroadcast(intent);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                isConnecting = false;
                // 退出登录 → 不再重连！！！
                if (isLogout) {
                    return;
                }
                // 否则 3 秒重连
                handler.postDelayed(() -> connectWebSocket(), 3000);
            }

            @Override
            public void onError(Exception ex) {
                isConnecting = false;
                if (!isLogout) {
                    handler.postDelayed(() -> connectWebSocket(), 3000);
                }
            }
        };
        webSocketClient.connect();
    }

    public boolean sendMessageToServer(String message) {
        try {
            if (webSocketClient != null && webSocketClient.isOpen()) {
                webSocketClient.send(message);
                return true;
            } else {
                Log.e("WebSocket", "发送失败：WebSocket未连接");
            }
        } catch (Exception e) {
            Log.e("WebSocket", "发送消息异常：" + e.getMessage());
        }
        return false;
    }

    // 退出登录时调用（关键！）
    public void logoutAndClose() {
        isLogout = true;
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
        user = null;
        ToastHandler.showToast("退出登录");
    }

    // 登录成功后调用（重置状态）
    public void loginSuccess() {
        isLogout = false;
        connectWebSocket();
    }
}