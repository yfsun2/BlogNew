package com.syf.blognew.adapter;

import android.view.View;

import com.syf.blognew.R;
import com.syf.blognew.pojo.vo.MessageVO;
import com.syf.blognew.util.Utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class MessageAdapter extends AbstractMultiAdapter<MessageVO>{

    List<MessageVO> mData;

    // 1. 定义接口
    public interface OnMessageClickListener {
        void onRetryClick(int position, MessageVO failedMessage);
        void onTransferClick(int position,MessageVO message);
        void onLongClick(View view,int position,MessageVO message);
    }

    private OnMessageClickListener listener;

    // 2. 提供设置方法
    public void setOnRetryClickListener(OnMessageClickListener listener) {
        this.listener = listener;
    }
    public MessageAdapter(List<MessageVO> mData) {
        super(mData);
        this.mData=mData;
    }

    @Override
    public void bindView(ViewHolder holder, MessageVO obj, int position) {
        boolean showTime = true;
        // 不是第一条，和上一条比「整分钟」
        if (position > 0) {
            MessageVO pre = mData.get(position - 1);
            LocalDateTime currMin = trimToMinute(obj.getCreateTime());
            LocalDateTime preMin = trimToMinute(pre.getCreateTime());
            // 同一自然分钟 → 隐藏时间
            if (currMin.isEqual(preMin)) {
                showTime = false;
            }
        }

        if (showTime) {
            holder.setVisibility(R.id.tv_sendtime,View.VISIBLE);
        } else {
            holder.setVisibility(R.id.tv_sendtime,View.GONE);
        }


        if (this.getItemViewType(position)==0){//发送方

            holder.setText(R.id.tv_sendtime, Utils.timeFormatter(obj.getCreateTime()));

            if(obj.getIsWithdraw()==1){
                holder.setVisibility(R.id.tv_withdraw,View.VISIBLE);
                holder.setText(R.id.tv_withdraw,"你撤回了一条消息");
                holder.setVisibility(R.id.ll_message,View.GONE);
                return;
            }else{
                holder.setVisibility(R.id.tv_withdraw,View.GONE);
                holder.setVisibility(R.id.ll_message,View.VISIBLE);
            }


            if(obj.getUrl()!=null&&!obj.getUrl().isEmpty()){
                holder.setImageUrl(R.id.jmui_avatar_iv,obj.getUrl());
            }else{
                holder.setImageWord(R.id.jmui_avatar_iv,obj.getUserName());
            }

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


            holder.setOnClickListener(R.id.jmui_fail_resend_ib,v->{
                if (listener != null) {
                    // 把位置和失败的消息传给 Activity
                    listener.onRetryClick(position, mData.get(position));
                }
            });

            if(obj.getMsgType()==0){//普通消息
                holder.setVisibility(R.id.ll_push,View.GONE);
                holder.setVisibility(R.id.tv_content,View.VISIBLE);
                holder.setText(R.id.tv_content,obj.getContent());
            }else{//转账消息
                holder.setVisibility(R.id.tv_content,View.GONE);
                holder.setVisibility(R.id.ll_push,View.VISIBLE);

                holder.setText(R.id.tv_menuName,obj.getContent()+"积分");

                if(obj.getIsReceive()==0){//转账未接收
                    holder.setText(R.id.tv_pushContent,"你发起了一笔转账");
                    holder.setImageResource(R.id.ll_push,R.mipmap.pay_send);
                }else if(obj.getIsReceive()==1){//转账已接收
                    holder.setText(R.id.tv_pushContent,"已被接收");
                    holder.setImageResource(R.id.ll_push,R.mipmap.pay_send_ok);
                }else{//转账被接收回执
                    holder.setText(R.id.tv_pushContent,"已收款");
                    holder.setImageResource(R.id.ll_push,R.mipmap.pay_send_ok);
                }
            }

            holder.setOnLongClickListener(R.id.tv_content, new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onLongClick(view,position,mData.get(position));
                    return true;
                }
            });

        }else {//接收方

            holder.setText(R.id.tv_sendtime, Utils.timeFormatter(obj.getCreateTime()));

            if(obj.getIsWithdraw()==1){
                holder.setVisibility(R.id.tv_withdraw,View.VISIBLE);
                holder.setText(R.id.tv_withdraw,"对方撤回了一条消息");
                holder.setVisibility(R.id.ll_message,View.GONE);
                return;
            }else{
                holder.setVisibility(R.id.tv_withdraw,View.GONE);
                holder.setVisibility(R.id.ll_message,View.VISIBLE);
            }


            if (obj.getUrl() != null && !obj.getUrl().isEmpty()) {
                holder.setImageUrl(R.id.jmui_avatar_iv, obj.getUrl());
            } else {
                holder.setImageWord(R.id.jmui_avatar_iv, obj.getUserName());
            }
//            holder.setText(R.id.tv_display_name, "远程");

            if(obj.getMsgType()==0) {
                holder.setVisibility(R.id.ll_push,View.GONE);
                holder.setVisibility(R.id.ll_common,View.VISIBLE);

                holder.setText(R.id.tv_content, obj.getContent());

            }else{//转账消息
                holder.setVisibility(R.id.ll_common,View.GONE);
                holder.setVisibility(R.id.ll_push,View.VISIBLE);

                holder.setText(R.id.tv_menuName,obj.getContent()+"积分");

                if(obj.getIsReceive()==0){//转账未接收
                    holder.setText(R.id.tv_pushContent,"请收款");
                    holder.setImageResource(R.id.ll_push,R.mipmap.pay_receive);
                    holder.setOnClickListener(R.id.ll_push,v->{
                       listener.onTransferClick(holder.getItemPosition(),obj);
                    });
                }else if(obj.getIsReceive()==1){//转账已接收
                    holder.setText(R.id.tv_pushContent,"已被接收");
                    holder.setImageResource(R.id.ll_push,R.mipmap.pay_receive_ok);
                }else{//转账被接收回执
                    holder.setText(R.id.tv_pushContent,"已收款");
                    holder.setImageResource(R.id.ll_push,R.mipmap.pay_receive_ok);
                }
            }
        }
    }

    public LocalDateTime trimToMinute(LocalDateTime time) {
        if (time == null) return null;
        // 秒和纳秒清0，只保留到 年-月-日 时:分
        return time.withSecond(0).withNano(0);
    }
}
