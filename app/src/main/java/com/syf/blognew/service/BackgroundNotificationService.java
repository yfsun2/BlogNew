package com.syf.blognew.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

import com.alibaba.fastjson.JSONObject;
import com.syf.blognew.R;
import com.syf.blognew.activity.MainActivity;
import com.syf.blognew.activity.NoticeActivity;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.websocket.WebSocketManager;

import java.util.Objects;

public class BackgroundNotificationService extends Service {
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "background_service_channel";
    public static final String ACTION_LOGOUT = "com.syf.blognew.LOGOUT";
    public static final String ACTION_NEW_MESSAGE = "com.syf.blognew.NEW_MESSAGE";

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        ToastHandler.showToast("通知服务已开启");
        startForeground(NOTIFICATION_ID, createForegroundNotification());
        // 注册消息广播
        registerReceiver(messageReceiver, new IntentFilter(ACTION_NEW_MESSAGE));
        // 注册退出登录广播
        registerReceiver(logoutReceiver, new IntentFilter(ACTION_LOGOUT));

        // 启动websocket
        new Thread(() -> {
            WebSocketManager.getInstance().loginSuccess();
        }, "WebSocket-Background-Thread").start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "后台消息服务",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private Notification createForegroundNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Blog")
                .setContentText("后台运行中，实时接收消息")
                .setSmallIcon(R.mipmap.app)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void sendLocalNotification(String title, String content,int type) {
        Intent intent;
        if(type==0){
            intent = new Intent(this, MainActivity.class);
        }else{
            intent = new Intent(this, NoticeActivity.class);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.app)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        manager.notify((int) System.currentTimeMillis(), notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(messageReceiver);
        unregisterReceiver(logoutReceiver);
        ToastHandler.showToast("通知服务被关闭");
        // 关闭websocket
        WebSocketManager.getInstance().logoutAndClose();
    }

    // ============== 接收聊天消息 ==============
    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra("message");
            JSONObject obj = JSONObject.parseObject(json);
            if (obj == null) return;

            String type = obj.getString("type");
            if (Objects.equals(type, "chat")) {
                String fromName = obj.getString("fromName");
                String content = obj.getString("msg");
                int fromId=obj.getIntValue("fromId");
                int online=obj.getInteger("online");
                int msgType=obj.getInteger("msgType");
                if(online!=fromId){//我的聊天界面不是对面
                    if(msgType==0){
                        sendLocalNotification("你有一条来自"+fromName+"的新消息", content,0);
                    }else{
                        sendLocalNotification("你有一条来自"+fromName+"的新消息", "[积分转账]"+content+"积分",0);
                    }
                }
            }else if(Objects.equals(type, "notice")){
                sendLocalNotification("你有一条新的通知", "点击查看详情",1);
            }
        }
    };

    // ============== 退出登录接收 ==============
    private final BroadcastReceiver logoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 停止服务，彻底注销
            stopForeground(true);
            stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}