package com.syf.blognew.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class QrUtil {

    /**
     * 生成二维码 + 中间网络URL Logo
     * @param content 二维码内容（网址/文字）
     * @param size 二维码大小
     * @param logoUrl 网络图片Logo地址（https://xxx.png）
     * @return 带Logo的二维码Bitmap
     */
    public static Bitmap createQRCode(String content, int size, String logoUrl) {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        try {
            // 1. 生成二维码点阵（你原来的代码）
            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    pixels[y * size + x] = matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                }
            }
            Bitmap qrBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            qrBitmap.setPixels(pixels, 0, size, 0, 0, size, size);

            // ==============================================
            // 下面是我加的：从网络URL获取Logo并画到中间
            // ==============================================
            if (logoUrl != null && !logoUrl.isEmpty()) {
                Bitmap logoBitmap = getLogoBitmapFromUrl(logoUrl); // 下载网络图片
                if (logoBitmap != null) {
                    qrBitmap = addLogoToQRCode(qrBitmap, logoBitmap); // 叠加到二维码中间
                }
            }

            return qrBitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ------------------------------
    // 从网络URL下载图片
    // ------------------------------
    private static Bitmap getLogoBitmapFromUrl(String logoUrl) {
        try {
            URL url = new URL(logoUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------------------------------
    // 将Logo画到二维码正中间
    // ------------------------------
    private static Bitmap addLogoToQRCode(Bitmap qrBitmap, Bitmap logoBitmap) {
        int qrWidth = qrBitmap.getWidth();
        int logoSize = qrWidth / 5; // Logo大小 = 二维码 1/5

        // 缩放Logo
        Bitmap scaleLogo = Bitmap.createScaledBitmap(logoBitmap, logoSize, logoSize, true);

        // 创建新画布
        Bitmap result = Bitmap.createBitmap(qrWidth, qrWidth, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(qrBitmap, 0, 0, null);

        // 居中绘制
        int left = (qrWidth - logoSize) / 2;
        int top = (qrWidth - logoSize) / 2;
        canvas.drawBitmap(scaleLogo, left, top, null);

        return result;
    }
}

