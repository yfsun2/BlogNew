package com.syf.blognew.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.syf.blognew.R;
import com.syf.blognew.view.ScaleImageView;

import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.Holder> {

    private Context context;
    private List<String> urls;
    private Runnable onImageClick;

    public ImagePagerAdapter(Context context, List<String> urls, Runnable onImageClick) {
        this.context = context;
        this.urls = urls;
        this.onImageClick = onImageClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_scale_image, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Glide.with(context).load(urls.get(position)).into(holder.iv);
        holder.iv.setOnClickListener(v -> onImageClick.run());
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ScaleImageView iv;
        public Holder(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv_scale);
        }
    }
}
