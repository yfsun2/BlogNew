package com.syf.blognew.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.syf.blognew.R;
import com.syf.blognew.pojo.vo.BlogVO;
import com.syf.blognew.pojo.vo.FriendVO;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FriendAdapter extends AbstractAdapter<FriendVO>{

    private final SimpleDateFormat sdf= new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);

    public FriendAdapter(List<FriendVO> mData, int mLayoutRes){
        super(mData,mLayoutRes);
    }
    @Override
    public void bindView(ViewHolder holder, FriendVO obj) {
        //昵称
        holder.setText(R.id.username,obj.getUser().getName());
        //头像
        if(obj.getUser().getUrl()!=null&&!obj.getUser().getUrl().isEmpty()){
            holder.setImageUrl(R.id.touxiang,obj.getUser().getUrl());
        }else{
            holder.setImageWord(R.id.touxiang,obj.getUser().getName());
        }
        //未读消息
        if(obj.getUnReadCount()>0){
            holder.setVisibility(R.id.tv_unread_badge, View.VISIBLE);
            holder.setText(R.id.tv_unread_badge,obj.getUnReadCount()>99?"99+":String.valueOf(obj.getUnReadCount()));
        }else{
            holder.setVisibility(R.id.tv_unread_badge, View.GONE);
        }
        //最后一条聊天信息
        holder.setText(R.id.last_words, (obj.getLastMessage() == null ? "" : obj.getLastMessage()));
        //最后聊天时间
        holder.setText(R.id.last_time, (obj.getLastTime() == null ? "" : sdf.format(obj.getLastTime())));
    }
}
