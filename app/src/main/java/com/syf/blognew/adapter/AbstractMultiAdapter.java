package com.syf.blognew.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author yfsun10
 * @version 1.0
 * @date 2021/5/20 15:45
 */

public abstract class AbstractMultiAdapter<T> extends BaseAdapter {

    private List<T> mData;

    protected MultiItemTypeSupportListener multiItemSupportListener;

    public AbstractMultiAdapter() {
    }

    public AbstractMultiAdapter(List<T> mData) {
        this.mData = mData;
    }

    public void setMultiItemTypeSupportListener(MultiItemTypeSupportListener multiItemSupportListener){
        this.multiItemSupportListener = multiItemSupportListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (multiItemSupportListener != null) {
            return multiItemSupportListener.getItemViewType(position);
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        if (multiItemSupportListener != null) {
            return multiItemSupportListener.getViewTypeCount();
        }
        return super.getViewTypeCount();
    }



    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.bind(parent.getContext(), convertView, parent, multiItemSupportListener.getLayoutId(position), position);
        bindView(holder, getItem(position),position);
        return holder.getItemView();
    }

    public abstract void bindView(ViewHolder holder, T obj,int position);

    //添加一个元素
    public void add(T data) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        mData.add(data);
        notifyDataSetChanged();
    }

    //往特定位置，添加一个元素
    public void add(int position, T data) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        mData.add(position, data);
        notifyDataSetChanged();
    }

    //移除指定元素
    public void remove(T data) {
        if (mData != null) {
            mData.remove(data);
        }
        notifyDataSetChanged();
    }

    //移除指定位置的元素
    public void remove(int position) {
        if (mData != null) {
            mData.remove(position);
        }
        notifyDataSetChanged();
    }

    //清楚元素
    public void clear() {
        if (mData != null) {
            mData.clear();
        }
        notifyDataSetChanged();
    }

    public interface MultiItemTypeSupportListener {
        int getItemViewType(int position);
        int getViewTypeCount();
        int getLayoutId(int position);

    }


    public static class ViewHolder {

        private SparseArray<View> mViews;   //存储ListView 的 item中的View
        private View item;                  //存放convertView
        private int position;               //游标
        private Context context;            //Context上下文

        private static final int[] colors = {
                0xff1abc9c, 0xff16a085, 0xfff1c40f, 0xfff39c12, 0xff2ecc71,
                0xff27ae60, 0xffe67e22, 0xffd35400, 0xff3498db, 0xff2980b9,
                0xffe74c3c, 0xffc0392b, 0xff9b59b6, 0xff8e44ad, 0xffbdc3c7,
                0xff34495e, 0xff2c3e50, 0xff95a5a6, 0xff7f8c8d, 0xffec87bf,
                0xffd870ad, 0xfff69785, 0xff9ba37e, 0xffb49255, 0xffb49255, 0xffa94136
        };

        //构造方法，完成相关初始化
        private ViewHolder(Context context, ViewGroup parent, int layoutRes) {
            mViews = new SparseArray<>();
            this.context = context;
            View convertView = LayoutInflater.from(context).inflate(layoutRes, parent, false);
            convertView.setTag(this);
            item = convertView;
        }

        //绑定ViewHolder与item
        public static ViewHolder bind(Context context, View convertView, ViewGroup parent, int layoutRes, int position) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder(context, parent, layoutRes);
            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.item = convertView;
            }
            holder.position = position;
            return holder;
        }

        @SuppressWarnings("unchecked")
        public <T extends View> T getView(int id) {
            T t = (T) mViews.get(id);
            if (t == null) {
                t = (T) item.findViewById(id);
                mViews.put(id, t);
            }
            return t;
        }


        /**
         * 获取当前条目
         */
        public View getItemView() {
            return item;
        }

        /**
         * 获取条目位置
         */
        public int getItemPosition() {
            return position;
        }

        /**
         * 设置文字
         */
        public ViewHolder setText(int id, CharSequence text) {
            View view = getView(id);
            if (view instanceof TextView) {
                ((TextView) view).setText(text);
            }
            return this;
        }
        public ViewHolder setText1(int id, Supplier<String> supplier) {
            View view = getView(id);
            if (view instanceof TextView) {
                ((TextView) view).setText(supplier.get());
            }
            return this;
        }
        /**
         * 设置图片
         */
        public ViewHolder setImageResource(int id, int drawableRes) {
            View view = getView(id);
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(drawableRes);
            } else {
                view.setBackgroundResource(drawableRes);
            }
            return this;
        }

        public ViewHolder setImageUri(int id,String url){
            View view=getView(id);
            if (view instanceof ImageView) {

                ((ImageView) view).setImageURI(Uri.parse(url));
            } else {
                try {
                    view.setBackground(Drawable.createFromStream(context.getContentResolver().openInputStream(Uri.parse(url)), null));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return this;
        }

        public ViewHolder setImageByte(int id,byte[] pic){
            View view=getView(id);
            if(view instanceof ImageView){
                if(pic!=null){
                    ((ImageView)view).setImageBitmap(BitmapFactory.decodeByteArray(pic,0, pic.length));
                }else {
                    ((ImageView)view).setImageBitmap(null);
                }
            }else {
                if(pic!=null){
                    view.setBackground(new BitmapDrawable(null,BitmapFactory.decodeByteArray(pic,0, pic.length)));
                }else {
                    view.setBackground(null);
                }
            }
            return this;
        }

        public ViewHolder setImageUrl(int id, String url) {
            View view = getView(id);
            if (view instanceof ImageView) {
                final Handler myhandler = new Handler(Looper.getMainLooper()){
                    @Override
                    public void handleMessage(Message msg) {
                        Bitmap bitmap = (Bitmap)msg.obj;
                        ((ImageView) view).setImageBitmap(bitmap);
                        super.handleMessage(msg);
                    }
                };

                new Thread(()->{
                    try {
                        Bitmap bitmap=BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
                        Message msg = new Message();
                        msg.obj = bitmap;
                        myhandler.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

            } else {
                final Handler myhandler = new Handler(Looper.getMainLooper()){
                    @Override
                    public void handleMessage(Message msg) {
                        Drawable drawable = (Drawable)msg.obj;
                        view.setBackground(drawable);
                        super.handleMessage(msg);
                    }
                };
                new Thread(()->{
                    try {
//                        Drawable drawable=new BitmapDrawable(null,BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream()));
                        Drawable drawable=Drawable.createFromStream(new URL(url).openConnection().getInputStream(),"image.jpg");
                        Message msg=new Message();
                        msg.obj=drawable;
                        myhandler.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            return this;
        }

        public ViewHolder setImageWord(int id, String xing) {
            if(xing==null||xing.isEmpty()) return this;
            ImageView iv = getView(id);

            // 👇 必加：清空
            iv.setImageBitmap(null);
            iv.setImageDrawable(null);

            iv.setImageBitmap(getBitmapByXing(String.valueOf(xing.charAt(0))));
            return this;
        }

        public Bitmap getBitmapByXing(String xing){
            Bitmap bitmap = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            int color = colors[xing.hashCode() % colors.length];
            canvas.drawColor(color);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextSize(50);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);

            Paint.FontMetricsInt fm = paint.getFontMetricsInt();
            int baseline = (100 - fm.top - fm.bottom) / 2;
            canvas.drawText(xing.toUpperCase(), 50, baseline, paint);
            return bitmap;
        }

        public ViewHolder setOnClickListener(int id, View.OnClickListener listener) {
            getView(id).setOnClickListener(listener);
            return this;
        }

        public ViewHolder setOnLongClickListener(int id,View.OnLongClickListener listener){
            getView(id).setOnLongClickListener(listener);
            return this;
        }

        public ViewHolder setOnTouchListener(int id, View.OnTouchListener listener){
            getView(id).setOnTouchListener(listener);
            return this;
        }

        /**
         * 设置可见
         */
        public ViewHolder setVisibility(int id, int visible) {
            getView(id).setVisibility(visible);
            return this;
        }

        /**
         * 设置标签
         */
        public ViewHolder setTag(int id, Object obj) {
            getView(id).setTag(obj);
            return this;
        }

        //其他方法可自行扩展

    }
}
