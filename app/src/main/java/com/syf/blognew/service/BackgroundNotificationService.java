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
import com.syf.blognew.websocket.WebSocketManager;

import java.util.Objects;

public class BackgroundNotificationService extends Service {
    private static final String CHANNEL_ID = "background_service_channel";
    private static final int NOTIFICATION_ID = 1001;

    // 退出登录的广播Action
    public static final String ACTION_LOGOUT = "com.syf.blognew.LOGOUT";

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createForegroundNotification());

        // 注册手动通知广播
        IntentFilter filter = new IntentFilter(ACTION_SHOW_NOTIFICATION);
        registerReceiver(notificationReceiver, filter);

        // 注册消息广播
        registerReceiver(mMsgReceiver, new IntentFilter("NEW_MESSAGE"));

        // 注册退出登录广播
        IntentFilter logoutFilter = new IntentFilter(ACTION_LOGOUT);
        registerReceiver(logoutReceiver, logoutFilter);

        // 启动websocket
        WebSocketManager.getInstance().loginSuccess();
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
                .setSmallIcon(R.drawable.app)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void sendLocalNotification(String title, String content) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.app)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        manager.notify((int) System.currentTimeMillis(), notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationReceiver);
        unregisterReceiver(mMsgReceiver);
        unregisterReceiver(logoutReceiver);

        // 关闭websocket
        WebSocketManager.getInstance().logoutAndClose();
    }

    // ============== 手动发通知 ==============
    public static final String ACTION_SHOW_NOTIFICATION = "com.syf.blognew.SHOW_NOTIFICATION";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_CONTENT = "content";

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra(EXTRA_TITLE);
            String content = intent.getStringExtra(EXTRA_CONTENT);
            sendLocalNotification(title, content);
        }
    };

    // ============== 接收聊天消息 ==============
    private final BroadcastReceiver mMsgReceiver = new BroadcastReceiver() {
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
                if(online!=fromId){//我的聊天界面不是对面
                    sendLocalNotification("你有1条来自"+fromName+"的新消息", content);
                }
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