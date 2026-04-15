package com.syf.blognew.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ImageUtil {
    private static final int[] colors = {
            0xff1abc9c, 0xff16a085, 0xfff1c40f, 0xfff39c12, 0xff2ecc71,
            0xff27ae60, 0xffe67e22, 0xffd35400, 0xff3498db, 0xff2980b9,
            0xffe74c3c, 0xffc0392b, 0xff9b59b6, 0xff8e44ad, 0xffbdc3c7,
            0xff34495e, 0xff2c3e50, 0xff95a5a6, 0xff7f8c8d, 0xffec87bf,
            0xffd870ad, 0xfff69785, 0xff9ba37e, 0xffb49255, 0xffb49255, 0xffa94136
    };

    public static Bitmap getBitmapByFirst(String first){
        Bitmap bitmap = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);//创建一个宽度和高度都是400、32位ARGB图
        Canvas canvas =new Canvas(bitmap);//初始化画布绘制的图像到icon上
        int charHash = first.hashCode();
        int color = colors[charHash % colors.length];
        canvas.drawColor(color);//图层的背景色
        Paint paint =new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DEV_KERN_TEXT_FLAG);//创建画笔
        paint.setTextSize(50.0f);
        paint.setStrokeWidth(3);
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int baseline = (100 - fontMetrics.bottom - fontMetrics.top) / 2;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        canvas.drawText(first.toUpperCase(),50, baseline, paint);//将文字写入
        canvas.save();//保存所有图层
        canvas.restore();
        return bitmap;
    }
}
