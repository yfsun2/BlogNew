package com.syf.blognew.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.syf.blognew.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import lombok.Getter;

public class ScratchCardGenerator {

    private Context context;
    private final Random random = new Random();
    @Getter
    private Integer moneyInt=0;
    // 坐标常量
    private final List<int[]> winBox = new ArrayList<>();
    private final List<int[]> moneyBox = new ArrayList<>();
    private final List<List<int[]>> numberBox = new ArrayList<>();

    private final Typeface font_cy,font_hp,font_simhei,font_msyh;

    // 数据常量
    private final String[] money = {"5", "10", "25", "50", "100", "200", "1000", "5000", "200000"};
    private final String[] moneyPinyin = {"WU", "YISHI", "ERSHIWU", "WUSHI", "YIBAI", "ERBAI", "YIQIAN", "WUQIAN", "ERSHIWAN"};
    private final String[] pinyin = {
            "", "LINGYI", "LINGER", "LINGSAN", "LINGSI", "LINGWU", "LINGLIU", "LINGQI", "LINGBA", "LINGJIU",
            "YILING", "YIYI", "YIER", "YISAN", "YISI", "YIWU", "YILIU", "YIQI", "YIBA", "YIJIU",
            "ERLING", "ERYI", "ERER", "ERSAN", "ERSI", "ERWU", "ERLIU", "ERQI", "ERBA", "ERJIU",
            "SANLING", "SANYI", "SANER", "SANSAN", "SANSI", "SANWU", "SANLIU", "SANQI", "SANBA", "SANJIU"
    };
    List<String> numbers,unNumbers;
    String winNum;

    public ScratchCardGenerator(Context context) {
        this.context=context;
        font_cy=Typeface.createFromAsset(context.getAssets(), "cy.TTF");
        font_hp=Typeface.createFromAsset(context.getAssets(), "hp.TTF");
        font_simhei=Typeface.createFromAsset(context.getAssets(), "simhei.ttf");
        font_msyh=Typeface.createFromAsset(context.getAssets(), "msyh.ttc");
        initPositions();
    }

    private void initPositions() {
        // 中奖号码位置
        winBox.add(new int[]{801, 215});

        // 金额位置
        List<int[]> moneyBoxTemp = new ArrayList<>();
        moneyBoxTemp.add(new int[]{931, 287});
        moneyBoxTemp.add(new int[]{931, 359});
        moneyBoxTemp.add(new int[]{931, 431});
        moneyBoxTemp.add(new int[]{931, 503});
        moneyBoxTemp.add(new int[]{931, 575});
        moneyBox.addAll(moneyBoxTemp);

        // 数字区 5行
        List<int[]> box1 = new ArrayList<>();
        box1.add(new int[]{538, 287});
        box1.add(new int[]{669, 287});
        box1.add(new int[]{800, 287});

        List<int[]> box2 = new ArrayList<>();
        box2.add(new int[]{407, 359});
        box2.add(new int[]{538, 359});
        box2.add(new int[]{669, 359});
        box2.add(new int[]{800, 359});

        List<int[]> box3 = new ArrayList<>();
        box3.add(new int[]{276, 431});
        box3.add(new int[]{407, 431});
        box3.add(new int[]{538, 431});
        box3.add(new int[]{669, 431});
        box3.add(new int[]{800, 431});

        List<int[]> box4 = new ArrayList<>();
        box4.add(new int[]{145, 503});
        box4.add(new int[]{276, 503});
        box4.add(new int[]{407, 503});
        box4.add(new int[]{538, 503});
        box4.add(new int[]{669, 503});
        box4.add(new int[]{800, 503});

        List<int[]> box5 = new ArrayList<>();
        box5.add(new int[]{14, 575});
        box5.add(new int[]{145, 575});
        box5.add(new int[]{276, 575});
        box5.add(new int[]{407, 575});
        box5.add(new int[]{538, 575});
        box5.add(new int[]{669, 575});
        box5.add(new int[]{800, 575});

        numberBox.add(box1);
        numberBox.add(box2);
        numberBox.add(box3);
        numberBox.add(box4);
        numberBox.add(box5);

        // 合并所有位置到 winBox（字母标记用）
        winBox.addAll(moneyBoxTemp);
        winBox.addAll(box1);
        winBox.addAll(box2);
        winBox.addAll(box3);
        winBox.addAll(box4);
        winBox.addAll(box5);
    }

    public Bitmap generateScratchCard(Context context) {
        // 1. 加载模板图片
        Bitmap template = loadTemplate(context);
        Bitmap result = template.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);

        // 2.从39个数字中生成25个中奖区数字和14个非中奖区数字
        generateNumbers();
        //3.计算奖金生成中奖号码
        List<Integer> moneyIndex = generateMoneyIndex();
        boolean[] isSpecial = {false};
        String moneyValue = calculateMoney(numbers, moneyIndex, isSpecial);
        // 3. 绘制中奖号码
        drawWinNumber(canvas, paint, Integer.parseInt(winNum), pinyin[Integer.parseInt(winNum)]);
        // 4. 绘制数字区
        drawNumberArea(canvas, paint, numbers, isSpecial[0]);
        // 5. 绘制奖金区
        drawMoneyArea(canvas, paint, moneyIndex);
        // 6. 绘制标记字母
        drawMarkLetters(canvas, paint, moneyValue);
        // 7. 生成并绘制PDF417条码
        String code = String.format("%s,%s", UUID.randomUUID().toString(), moneyValue);
        drawBarcode(canvas, paint, code);
        moneyInt=Integer.parseInt(moneyValue);
        return result;
    }

    // ====================== 工具方法 ======================
    private Bitmap loadTemplate(Context context) {
        int model = random.nextInt(3) + 1;
        int resId = R.mipmap.model;
        if (model == 2) resId = R.mipmap.model1;
        if (model == 3) resId = R.mipmap.model2;
        return ((BitmapDrawable) context.getDrawable(resId)).getBitmap();
    }

    private void generateNumbers() {
        numbers = new ArrayList<>();
        while (numbers.size() < 25) {
            String num=String.valueOf(random.nextInt(39) + 1);
            if (!numbers.contains(num)) numbers.add(num);
        }
        unNumbers = new ArrayList<>();
        for(int i=1;i<=39;i++){
            String num=String.valueOf(i);
            if(!numbers.contains(num)){
                unNumbers.add(num);
            }
        }
    }

    private List<Integer> generateMoneyIndex() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 9; i++) list.add(i);
        Collections.shuffle(list);
        return list.subList(0, 5);
    }

    private String calculateMoney(List<String> numbers, List<Integer> moneyIndex, boolean[] special) {
        int q = random.nextInt(2000000);
        if(q<1+2+10+1000+7000+22000+26000+180000+366000){//中奖
            winNum=numbers.get(random.nextInt(numbers.size()));
            int idx = getWinRow(numbers.indexOf(winNum));
            if (q < 1) moneyIndex.set(idx, 8);//20,0000
            else if (q < 1+2) moneyIndex.set(idx, 7);//5000
            else if (q < 1+2+10) moneyIndex.set(idx, 6);//1000
            else if (q < 1+2+10+1000) moneyIndex.set(idx, 5);//200
            else if (q < 1+2+10+1000+7000) moneyIndex.set(idx, 4);//100
            else if (q < 1+2+10+1000+7000+22000) moneyIndex.set(idx, 3);//50
            else if (q < 1+2+10+1000+7000+22000+26000) moneyIndex.set(idx, 2);//25
            else if (q < 1+2+10+1000+7000+22000+26000+180000) moneyIndex.set(idx, 1);//10
            else {//5
                moneyIndex.set(idx, 0);
                int rand=random.nextInt(1000);
                if (rand< 50) {
                    numbers.set(numbers.indexOf(winNum), "S5");
                    special[0] = true;
                }else if(rand>950){
                    int index0 = random.nextInt(3);
                    int index1 = random.nextInt(4)+3;
                    int index2 = random.nextInt(5)+7;
                    int index3 = random.nextInt(6)+12;
                    int index4 = random.nextInt(7)+18;
                    int[] index_list=new int[]{index0,index1,index2,index3,index4};
                    for(int i=0;i<5;i++){
                        if(i!=idx){
                            numbers.set(index_list[i],winNum);
                        }
                        moneyIndex.set(i,0);
                    }
                    return "25";
                }
            }
            return special[0] ? "25" : money[moneyIndex.get(idx)];
        }else{//未中奖
            winNum=unNumbers.get(random.nextInt(unNumbers.size()));
            return "0";
        }
    }

    private int getWinRow(int pos) {
        if (pos < 3) return 0;
        if (pos < 7) return 1;
        if (pos < 12) return 2;
        if (pos < 18) return 3;
        return 4;
    }

    // 绘制方法（完整实现见下方）
    @SuppressLint("DefaultLocale")
    private void drawWinNumber(Canvas canvas, Paint paint, int num, String text) {
        paint.setTextSize(50);
        paint.setTypeface(font_hp);
        canvas.drawText(String.format("%02d", num), 130 + 801 - 25, 215 + 35+5, paint);
        paint.setTypeface(font_simhei);
        paint.setTextSize(20);
        canvas.drawText(text, 130 + 801 - text.length() * 5, 265+10+5, paint);
    }

    @SuppressLint("DefaultLocale")
    private void drawNumberArea(Canvas canvas, Paint paint, List<String> numbers, boolean special) {
        int index = 0;
        for (List<int[]> row : numberBox) {
            for (int[] pos : row) {
                String obj = numbers.get(index++);
                if (obj.equals("S5")) {
                    paint.setTypeface(font_hp);
                    paint.setTextSize(50);
                    canvas.drawText("5", 65 + pos[0] - 12.5f, pos[1] + 35+5, paint);
                    paint.setTypeface(font_simhei);
                    paint.setTextSize(20);
                    canvas.drawText("WU", 65 + pos[0] - 10, pos[1] + 60+5, paint);
                } else {
                    int n =Integer.parseInt(obj);
                    paint.setTypeface(font_cy);
                    paint.setTextSize(50);
                    canvas.drawText(String.format("%02d", n), 65 + pos[0] - 25, pos[1] + 35+5, paint);
                    paint.setTypeface(font_simhei);
                    paint.setTextSize(20);
                    canvas.drawText(pinyin[n], 65 + pos[0] - pinyin[n].length() * 5, pos[1] + 60+5, paint);
                }
            }
        }
    }

    private void drawMoneyArea(Canvas canvas, Paint paint, List<Integer> moneyIndex) {
        for (int i = 0; i < moneyBox.size(); i++) {
            int[] pos = moneyBox.get(i);
            String m = money[moneyIndex.get(i)];
            paint.setTypeface(font_msyh);
            paint.setTextSize(30);
            canvas.drawText("￥", pos[0], pos[1] + 35, paint);
            if(Objects.equals(m,"200000")){
                paint.setTextSize(25);
            }else{
                paint.setTextSize(30);
            }

            canvas.drawText(m, 65 + pos[0] -m.length()*5, pos[1] + 35, paint);
            paint.setTextSize(20);
            paint.setTypeface(font_simhei);
            canvas.drawText(moneyPinyin[moneyIndex.get(i)], 65 + pos[0] -moneyPinyin[moneyIndex.get(i)].length()*5 , pos[1] + 50+10, paint);
        }
    }

    private void drawMarkLetters(Canvas canvas, Paint paint, String moneyVal) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < winBox.size(); i++) positions.add(i);
        Collections.shuffle(positions);
        positions = positions.subList(0, 3);

        String[] letters;
        if (moneyVal.equals("0")) {
            letters = generateRandomLetters();
        } else {
            letters = getMoneyLetters(moneyVal);
        }

        for (int i = 0; i < 3; i++) {
            int[] pos = winBox.get(positions.get(i));
            paint.setTypeface(font_simhei);
            paint.setTextSize(20);
            canvas.drawText(letters[i], pos[0] + 5, pos[1] + 50+12, paint);
        }
    }

    private void drawBarcode(Canvas canvas, Paint paint, String code) {
        try {
//            BitMatrix matrix = new MultiFormatWriter().encode(
//                    code, BarcodeFormat.PDF_417, 553, 76);
//            Bitmap barcode = Bitmap.createBitmap(553, 76, Bitmap.Config.RGB_565);
//
//            for (int x = 0; x < 553; x++) {
//                for (int y = 0; y < 76; y++) {
//                    barcode.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
//                }

//            canvas.drawBitmap(barcode, 508, 713, paint);
            paint.setTextSize(30);
            paint.setTypeface(font_simhei);
            canvas.drawText(code.split(",")[0], 508 , 713 + 60, paint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] generateRandomLetters() {
        String[] used = {"B", "K", "Q", "R", "S", "U", "W", "Y"};
        List<String> list = new ArrayList<>();
        while (list.size() < 3) {
            char c = (char) (random.nextInt(26) + 'A');
            String s = String.valueOf(c);
            if (!list.contains(s) && !contains(used, s)) list.add(s);
        }
        return list.toArray(new String[0]);
    }

    private String[] getMoneyLetters(String val) {
        return switch (val) {
            case "5" -> new String[]{"W", "U", "K"};
            case "10" -> new String[]{"Y", "S", "K"};
            case "25" -> new String[]{"R", "W", "K"};
            case "50" -> new String[]{"W", "S", "K"};
            case "100" -> new String[]{"Y", "B", "K"};
            case "200" -> new String[]{"R", "B", "K"};
            case "1000" -> new String[]{"Y", "Q", "K"};
            case "5000" -> new String[]{"W", "Q", "K"};
            case "200000" -> new String[]{"R", "S", "W"};
            default -> generateRandomLetters();
        };
    }

    private boolean contains(String[] arr, String s) {
        for (String str : arr) if (str.equals(s)) return true;
        return false;
    }
}
