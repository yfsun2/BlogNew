package com.syf.blognew.util;

public class UnReadManager {
    private static UnReadManager instance;
    public int friendUnreadCount = 0;

    // 回调：数字变了就通知 Tab 更新红点
    public Runnable onUnreadChangeListener;

    public static UnReadManager getInstance() {
        if (instance == null) {
            instance = new UnReadManager();
        }
        return instance;
    }
}
