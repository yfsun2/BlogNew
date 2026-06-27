package com.syf.blognew.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

public class BarcodeUtil {
    public static Bitmap createBarCode(String content,int width,int height){
        try {
            BitMatrix matrix = new MultiFormatWriter()
                    .encode(content, BarcodeFormat.CODE_128,width,height);
            Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
            for(int x=0;x<width;x++){
                for(int y=0;y<height;y++){
                    bitmap.setPixel(x,y,matrix.get(x,y)? Color.BLACK:Color.WHITE);
                }
            }
            return bitmap;
        }catch (Exception e){
            return null;
        }
    }
}