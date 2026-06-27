package com.syf.blognew.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.syf.blognew.R;
import com.syf.blognew.pojo.entity.Gift;
import com.syf.blognew.websocket.WebSocketManager;

import java.util.List;
import java.util.Objects;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftViewHolder> {

    private Context context;
    private List<Gift> giftList;
    private int userPoints; // 用户当前积分

    public interface onExchangeClickListener{
        void onExchangeClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public onExchangeClickListener listener;

    public void setOnExchangeClickListener(onExchangeClickListener listener){
        this.listener=listener;
    }

    public GiftAdapter(Context context, List<Gift> giftList, int userPoints) {
        this.context = context;
        this.giftList = giftList;
        this.userPoints = userPoints;
    }

    @NonNull
    @Override
    public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gift, parent, false);
        return new GiftViewHolder(view);
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        Gift gift = giftList.get(position);

        holder.tvGiftName.setText(gift.getName());
        holder.tvNeedPoints.setText("需要积分：" + gift.getNeedScore());
        holder.tvCount.setText("剩余数量："+gift.getCount());

        // 加载网络图片（使用Glide）
        Glide.with(context).load(gift.getUrl()).into(holder.ivGiftImage);

        // 判断积分是否足够
        if(gift.getCount()>0) {
            holder.tvCount.getPaint().setFlags(0);
            holder.tvCount.getPaint().setAntiAlias(true);
            if(gift.getCount()<=3){
                holder.tvCount.setTextColor(Color.RED);
            }else if(gift.getCount()<=10){
                holder.tvCount.setTextColor(Color.rgb(255,136,42));
            }else{
                holder.tvCount.setTextColor(Color.BLACK);
            }
            if (userPoints >= gift.getNeedScore()) {
                holder.tvExchangeStatus.setText("立即兑换");
                holder.tvExchangeStatus.setEnabled(true);
                holder.tvExchangeStatus.setBackgroundResource(R.drawable.btn_exchange_bg);
            } else {
                holder.tvExchangeStatus.setText("积分不足");
                holder.tvExchangeStatus.setEnabled(false);
                holder.tvExchangeStatus.setBackgroundResource(R.drawable.btn_disabled_bg);
            }
        }else{
            holder.tvCount.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvCount.getPaint().setAntiAlias(true);
            holder.tvCount.setTextColor(Color.parseColor("#555555"));
            holder.tvExchangeStatus.setText("库存不足");
            holder.tvExchangeStatus.setEnabled(false);
            holder.tvExchangeStatus.setBackgroundResource(R.drawable.btn_disabled_bg);
        }

        if(Objects.equals(WebSocketManager.getInstance().getUser().getPower(), "ADMIN")){
            holder.tvEditGift.setVisibility(RecyclerView.VISIBLE);
            holder.tvDeleteGift.setVisibility(RecyclerView.VISIBLE);
            holder.tvExchangeStatus.setVisibility(RecyclerView.GONE);

        }else{
            holder.tvEditGift.setVisibility(RecyclerView.GONE);
            holder.tvDeleteGift.setVisibility(RecyclerView.GONE);
            holder.tvExchangeStatus.setVisibility(RecyclerView.VISIBLE);
        }

        holder.tvExchangeStatus.setOnClickListener(v->{
            listener.onExchangeClick(position);
        });
        holder.tvEditGift.setOnClickListener(v->{
            listener.onEditClick(position);
        });
        holder.tvDeleteGift.setOnClickListener(v->{
            listener.onDeleteClick(position);
        });

    }

    public void updateUserPoints(int newPoints) {
        this.userPoints = newPoints;
        notifyDataSetChanged(); // 自动刷新
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    public static class GiftViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGiftImage;
        TextView tvGiftName, tvNeedPoints, tvExchangeStatus,tvCount,tvEditGift,tvDeleteGift;
        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGiftImage = itemView.findViewById(R.id.iv_gift_image);
            tvGiftName = itemView.findViewById(R.id.tv_gift_name);
            tvNeedPoints = itemView.findViewById(R.id.tv_need_points);
            tvExchangeStatus = itemView.findViewById(R.id.tv_exchange_status);
            tvCount=itemView.findViewById(R.id.tv_count);
            tvEditGift=itemView.findViewById(R.id.tv_edit_gift);
            tvDeleteGift=itemView.findViewById(R.id.tv_delete_gift);
        }
    }
}