package com.syf.blognew.util;

import android.view.MotionEvent;
import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

import com.syf.blognew.handler.ToastHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class Utils {

    // 邮箱正则
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter FORMATTER_SIMPLE = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    /**
     * 验证邮箱是否合法
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static String timeFormatter(LocalDateTime time){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter todayFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        DateTimeFormatter otherFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 1. 判断是不是 今天
        if (time.toLocalDate().isEqual(now.toLocalDate())) {
            return time.format(todayFormatter);
        }
        // 2. 判断是不是 今年
        else if (time.getYear() == now.getYear()) {
            return time.format(yearFormatter);
        }
        // 3. 不是今年 → 显示完整年月日
        else {
            return time.format(otherFormatter);
        }
    }
}
