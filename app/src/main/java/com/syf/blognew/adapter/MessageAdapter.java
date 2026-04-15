package com.syf.blognew.adapter;

import android.view.View;

import com.syf.blognew.R;
import com.syf.blognew.pojo.vo.ChatMessage;
import com.syf.blognew.pojo.UserApplication;
import com.syf.blognew.handler.ToastHandler;

import org.java_websocket.client.WebSocketClient;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends AbstractMultiAdapter<ChatMessage>{

    private final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    List<ChatMessage> mData;

    // 1. 定义接口
    public interface OnRetryClickListener {
        void onRetryClick(int position, ChatMessage failedMessage);
    }

    private OnRetryClickListener mRetryListener;

    // 2. 提供设置方法
    public void setOnRetryClickListener(OnRetryClickListener listener) {
        this.mRetryListener = listener;
    }
    public MessageAdapter(List<ChatMessage> mData) {
        super(mData);
        this.mData=mData;
    }

    @Override
    public void bindView(ViewHolder holder, ChatMessage obj, int position) {
        if (this.getItemViewType(position)==0){
            holder.setText(R.id.tv_content,obj.getContent());
            holder.setText(R.id.tv_sendtime,sdf.format(obj.getTime()));
            if(obj.getIsRead()==0){//未读
                holder.setVisibility(R.id.tv_isRead,View.VISIBLE);
                holder.setVisibility(R.id.jmui_fail_resend_ib,View.GONE);
                holder.setVisibility(R.id.jmui_sending_iv,View.GONE);
                holder.setText(R.id.tv_isRead,"未读");
            }else if(obj.getIsRead()==1){//已读
                holder.setVisibility(R.id.tv_isRead,View.VISIBLE);
                holder.setVisibility(R.id.jmui_fail_resend_ib,View.GONE);
                holder.setVisibility(R.id.jmui_sending_iv,View.GONE);
                holder.setText(R.id.tv_isRead,"已读");
            }else if(obj.getIsRead()==2){//发送中
                holder.setVisibility(R.id.tv_isRead,View.GONE);
                holder.setVisibility(R.id.jmui_fail_resend_ib,View.GONE);
                holder.setVisibility(R.id.jmui_sending_iv,View.VISIBLE);
            }else{//发送失败
                holder.setVisibility(R.id.tv_isRead,View.GONE);
                holder.setVisibility(R.id.jmui_fail_resend_ib,View.VISIBLE);
                holder.setVisibility(R.id.jmui_sending_iv,View.GONE);
            }

            if(obj.getUrl()!=null&&!obj.getUrl().isEmpty()){
                holder.setImageUrl(R.id.jmui_avatar_iv,obj.getUrl());
            }else{
                holder.setImageWord(R.id.jmui_avatar_iv,obj.getUserName());
            }

            holder.setOnClickListener(R.id.jmui_fail_resend_ib,v->{

                if (mRetryListener != null) {
                    // 把位置和失败的消息传给 Activity
                    mRetryListener.onRetryClick(position, mData.get(position));
                }

            });
        }else {
            holder.setText(R.id.tv_content,obj.getContent());
            holder.setText(R.id.tv_sendtime,sdf.format(obj.getTime()));
            holder.setText(R.id.tv_display_name,"远程");
            if(obj.getUrl()!=null&&!obj.getUrl().isEmpty()){
                holder.setImageUrl(R.id.jmui_avatar_iv,obj.getUrl());
            }else{
                holder.setImageWord(R.id.jmui_avatar_iv,obj.getUserName());
            }
        }
    }
}
