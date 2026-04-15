package com.syf.blognew.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.syf.blognew.R;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractAdapter<T> extends BaseAdapter {
    private List<T> mData;
    private int mLayoutRes;
    public AbstractAdapter() {}

    public AbstractAdapter(List<T> mData, int mLayoutRes) {
        this.mData = mData;
        this.mLayoutRes = mLayoutRes;
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
        ViewHolder holder = ViewHolder.bind(parent.getContext(), convertView, parent, mLayoutRes, position);
        bindView(holder, getItem(position));
        return holder.getItemView();
    }

    public abstract void bindView(ViewHolder holder, T obj);

    public void add(T data) {
        if (mData == null) mData = new ArrayList<>();
        mData.add(data);
        notifyDataSetChanged();
    }

    public void add(int position, T data) {
        if (mData == null) mData = new ArrayList<>();
        mData.add(position, data);
        notifyDataSetChanged();
    }

    public void remove(T data) {
        if (mData != null) mData.remove(data);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (mData != null) mData.remove(position);
        notifyDataSetChanged();
    }

    public void clear() {
        if (mData != null) mData.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        private final SparseArray<View> mViews;
        private View item;
        private int position;
        private static final int[] colors = {
                0xff1abc9c, 0xff16a085, 0xfff1c40f, 0xfff39c12, 0xff2ecc71,
                0xff27ae60, 0xffe67e22, 0xffd35400, 0xff3498db, 0xff2980b9,
                0xffe74c3c, 0xffc0392b, 0xff9b59b6, 0xff8e44ad, 0xffbdc3c7,
                0xff34495e, 0xff2c3e50, 0xff95a5a6, 0xff7f8c8d, 0xffec87bf,
                0xffd870ad, 0xfff69785, 0xff9ba37e, 0xffb49255, 0xffb49255, 0xffa94136
        };

        private ViewHolder(Context context, ViewGroup parent, int layoutRes) {
            mViews = new SparseArray<>();
            View convertView = LayoutInflater.from(context).inflate(layoutRes, parent, false);
            convertView.setTag(this);
            item = convertView;
        }

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

        public View getItemView() {
            return item;
        }

        public int getItemPosition() {
            return position;
        }

        public ViewHolder setText(int id, CharSequence text) {
            View view = getView(id);
            if (view instanceof TextView) {
                ((TextView) view).setText(text);
            }
            return this;
        }

        public ViewHolder setImageResource(int id, int drawableRes) {
            View view = getView(id);
            if(view instanceof ImageButton){
                view.setBackgroundResource(drawableRes);
            }
            else if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(drawableRes);
            } else {
                view.setBackgroundResource(drawableRes);
            }
            return this;
        }

        // ====================== 修复 1：每次设置URL前清空 ======================
        public ViewHolder setImageUrl(int id, String url) {
            ImageView iv = getView(id);
            iv.setImageBitmap(null);
            iv.setImageDrawable(null);
            // Glide 内部自动处理：复用错乱、缓存、异步、滑动优化、取消旧请求
            Glide.with(iv.getContext())
                    .load(url)
                    .centerCrop()
                    .into(iv);

            return this;
        }

        public ViewHolder setButtonLeftImg(int id, String first, int paddingDp) {
            Button btn = getView(id);
            btn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            Bitmap bitmap=getBitmapByFirst(String.valueOf(first.charAt(0)));
            bitmap=toRoundBitmap(bitmap);
            if (bitmap != null) {
                BitmapDrawable drawable = new BitmapDrawable(btn.getResources(), bitmap);
                // 这里可以统一设置大小，也可以用 intrinsicBounds
                int iconSize = dp2px(btn.getContext(), 30);
                drawable.setBounds(0, 0, iconSize, iconSize);
                btn.setCompoundDrawables(drawable, null, null, null);
                btn.setCompoundDrawablePadding(dp2px(btn.getContext(), paddingDp));
            }
            return this;
        }

        private Bitmap toRoundBitmap(Bitmap bitmap) {
            if (bitmap == null) return null;

            int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
            Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, size, size);

            paint.setAntiAlias(true);
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, null, rect, paint);

            return output;
        }

        private int dp2px(Context context, float dp) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dp * scale + 0.5f);
        }

        // ------------ 你原来的（文字头像）------------

        // ------------ 我给你新增的（URL 网络图片）------------
        public ViewHolder setButtonLeftUrl(int id, String imageUrl, int paddingDp) {
            Button btn = getView(id);
            btn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

            if (imageUrl == null || imageUrl.isEmpty()) {
                return this;
            }

            int iconSize = dp2px(btn.getContext(), 30);

            Glide.with(btn.getContext())
                    .asBitmap()
                    .load(imageUrl)
                    .centerCrop() // 保证圆形不拉伸
                    .override(iconSize, iconSize)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            // 直接用你现成的圆角方法！
                            Bitmap roundBitmap = toRoundBitmap(resource);
                            BitmapDrawable drawable = new BitmapDrawable(btn.getResources(), roundBitmap);
                            drawable.setBounds(0, 0, iconSize, iconSize);

                            btn.post(() -> {
                                btn.setCompoundDrawables(drawable, null, null, null);
                                btn.setCompoundDrawablePadding(dp2px(btn.getContext(), paddingDp));
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });

            return this;
        }


        // ====================== 修复 2：文字头像 ======================
        public ViewHolder setImageWord(int id, String first) {
            if(first==null||first.isEmpty()) return this;
            ImageView iv = getView(id);
            // 👇 必加：清空
            iv.setImageBitmap(null);
            iv.setImageDrawable(null);

            iv.setImageBitmap(getBitmapByFirst(String.valueOf(first.charAt(0))));
            return this;
        }

        public ViewHolder setImageListToGridLayout(int id, List<String> imageList, Context context) {
            GridLayout gridLayout = getView(id);
            gridLayout.removeAllViews();
            if (imageList.isEmpty()) return this;

            gridLayout.setColumnCount(3);

            // ====================== 修复位置 ======================
            // 原来的代码会越刷新越小
            // int totalWidth = gridLayout.getWidth();
            // 改成：永远使用 屏幕宽度 计算，固定不变！
            int totalWidth = context.getResources().getDisplayMetrics().widthPixels;

            // 减去列表左右的padding（如果你列表有10dp左右边距，就减20）
            // 没有就写 0
            int offset = 60;
            int imageWidth = (totalWidth - offset) / 3;
            int imageHeight = imageWidth;

            int space = 20; // 间距保持你原来的5

            for (int i = 0; i < imageList.size(); i++) {
                String imageUrl = imageList.get(i);
                ImageView imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                // ====================== 修复：减去间距，防止撑出界面 ======================
                params.width = imageWidth - space * 2;
                params.height = imageHeight - space * 2;
                params.setMargins(space, space, space, space);

                imageView.setLayoutParams(params);

                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.moren)
                        .into(imageView);

                int finalI = i;
                imageView.setOnClickListener(v -> showImageDialog(context, imageList, finalI));
                gridLayout.addView(imageView);
            }
            return this;
        }

        private void showImageDialog(Context context, List<String> imageUrls, int position) {
            Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.dialog_image_pager);

            ViewPager2 viewPager = dialog.findViewById(R.id.viewPager);
            ImageView ivClose = dialog.findViewById(R.id.iv_close);

            ImagePagerAdapter adapter = new ImagePagerAdapter(context, imageUrls, dialog::dismiss);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(position, false);

            ivClose.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }

        public Bitmap getBitmapByFirst(String first){
            Bitmap bitmap = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            int color = colors[first.hashCode() % colors.length];
            canvas.drawColor(color);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextSize(50);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);

            Paint.FontMetricsInt fm = paint.getFontMetricsInt();
            int baseline = (100 - fm.top - fm.bottom) / 2;
            canvas.drawText(first.toUpperCase(), 50, baseline, paint);
            return bitmap;
        }

        public ViewHolder setImageByte(int id, byte[] pic,String first) {
            ImageView iv = getView(id);
            iv.setImageBitmap(null); // 清空
            if(pic!=null){
                iv.setImageBitmap(BitmapFactory.decodeByteArray(pic,0,pic.length));
            }else {
                iv.setImageBitmap(getBitmapByFirst(first));
            }
            return this;
        }

        public ViewHolder setAdapter(int id, AbstractAdapter<?> adapter){
            View view=getView(id);
            if (view instanceof ListView){
                ((ListView)view).setAdapter(adapter);
                setListViewHeightBasedOnChildren((ListView)view);
            }
            return this;
        }

        public ViewHolder setOnItemClickListener(int id, AdapterView.OnItemClickListener listener){
            View view=getView(id);
            if (view instanceof ListView){
                ((ListView) view).setOnItemClickListener(listener);
            }
            return this;
        }

        public static void setListViewHeightBasedOnChildren(ListView listView) {
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null) return;

            int totalHeight = 0;
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
        }

        public ViewHolder setOnClickListener(int id, View.OnClickListener listener) {
            getView(id).setOnClickListener(listener);
            return this;
        }

        public ViewHolder setVisibility(int id, int visible) {
            getView(id).setVisibility(visible);
            return this;
        }

        public ViewHolder setTag(int id, Object obj) {
            getView(id).setTag(obj);
            return this;
        }

        public Object getTag(int id) {
            return getView(id).getTag();
        }
    }
}