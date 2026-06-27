package com.syf.blognew.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.syf.blognew.R;
import com.syf.blognew.pojo.vo.NoticeVO;
import com.syf.blognew.util.Utils;
import com.syf.blognew.websocket.WebSocketManager;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NoticeAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<NoticeVO> mDataList;
    private OnNoticeOperateListener mListener;
    public interface OnNoticeOperateListener{
        // 好友申请操作
        void onFriendOperate(int position, boolean isAgree);
        void onGiftSend(int position);
        void onGiftFinish(int position);
        void onGiftRefuse(int position);
        // 条目点击标记已读
        void onItemClick(int position);
    }

    public void setOnNoticeOperateListener(OnNoticeOperateListener listener){
        this.mListener = listener;
    }

    public NoticeAdapter(Context context, List<NoticeVO> list){
        this.mContext = context;
        this.mDataList = list;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = View.inflate(mContext, R.layout.item_notice, null);
            holder = new ViewHolder();
            holder.vUnreadDot = convertView.findViewById(R.id.v_unread_dot);
            holder.ivIcon = convertView.findViewById(R.id.iv_type_icon);
            holder.ivBg=convertView.findViewById(R.id.iv_bg);
            holder.tvTitle = convertView.findViewById(R.id.tv_title);
            holder.tvContent = convertView.findViewById(R.id.tv_content);
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            holder.tvStatus = convertView.findViewById(R.id.tv_status);
            holder.llFriendBtn = convertView.findViewById(R.id.ll_friend_btn);
            holder.llGiftBtn = convertView.findViewById(R.id.ll_gift_btn);
            holder.btnAgree = convertView.findViewById(R.id.btn_agree);
            holder.btnRefuse = convertView.findViewById(R.id.btn_refuse);
            holder.btnHaveSend= convertView.findViewById(R.id.btn_have_send);
            holder.btnHaveFinish= convertView.findViewById(R.id.btn_have_finish);
            holder.btnHaveRefuse= convertView.findViewById(R.id.btn_have_refuse);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        NoticeVO bean = mDataList.get(position);

        // 未读红点
        holder.vUnreadDot.setVisibility(bean.getIsRead()==1 ? View.INVISIBLE : View.VISIBLE);
        if(WebSocketManager.getInstance().getUser().getPower().equals("ADMIN")){
            holder.tvTitle.setText(bean.getTitle()+"【"+bean.getUserName()+"】");
        }else{
            holder.tvTitle.setText(bean.getTitle());
        }

        holder.tvContent.setText(bean.getContent());
        holder.tvTime.setText(Utils.timeFormatter(bean.getTime()));

        // 全部控件默认隐藏
        holder.tvStatus.setVisibility(View.GONE);
        holder.llFriendBtn.setVisibility(View.GONE);
        holder.llGiftBtn.setVisibility(View.GONE);
        holder.ivBg.setImageResource(0);

        // 根据类型展示对应内容
        switch (bean.getType()){
            case NoticeVO.TYPE_EXCHANGE_GIFT:
                holder.ivIcon.setImageResource(R.mipmap.ic_gift);
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.llGiftBtn.setVisibility(View.VISIBLE);
                holder.btnHaveSend.setOnClickListener(v->{
                    mListener.onGiftSend(position);
                });
                holder.btnHaveFinish.setOnClickListener(v->{
                    mListener.onGiftFinish(position);
                });
                holder.btnHaveRefuse.setOnClickListener(v->{
                    mListener.onGiftRefuse(position);
                });
                // 发货状态
                switch (bean.getGoodsStatus()){
                    case 0:
                        holder.tvStatus.setText("状态：待发货");
                        holder.tvStatus.setTextColor(Color.parseColor("#FF9900"));
                        if(WebSocketManager.getInstance().getUser().getPower().equals("ADMIN")){
                            holder.btnHaveSend.setVisibility(View.VISIBLE);
                            holder.btnHaveFinish.setVisibility(View.GONE);
                            holder.btnHaveRefuse.setVisibility(View.VISIBLE);
                        }else{
                            holder.btnHaveSend.setVisibility(View.GONE);
                            holder.btnHaveFinish.setVisibility(View.GONE);
                            holder.btnHaveRefuse.setVisibility(View.GONE);
                        }
                        break;
                    case 1:
                        holder.tvStatus.setText("状态：已发货");
                        holder.tvStatus.setTextColor(Color.parseColor("#0099FF"));
                        if(WebSocketManager.getInstance().getUser().getPower().equals("ADMIN")){
                            holder.btnHaveSend.setVisibility(View.GONE);
                            holder.btnHaveFinish.setVisibility(View.GONE);
                            holder.btnHaveRefuse.setVisibility(View.GONE);
                        }else{
                            holder.btnHaveSend.setVisibility(View.GONE);
                            holder.btnHaveFinish.setVisibility(View.VISIBLE);
                            holder.btnHaveRefuse.setVisibility(View.GONE);
                        }
                        break;
                    case 2:
                        holder.tvStatus.setText("状态：已签收");
                        holder.tvStatus.setTextColor(Color.parseColor("#00BB66"));
                        holder.ivBg.setImageResource(R.mipmap.ic_exchange_success);
                        holder.btnHaveSend.setVisibility(View.GONE);
                        holder.btnHaveFinish.setVisibility(View.GONE);
                        holder.btnHaveRefuse.setVisibility(View.GONE);
                        break;
                    case 3:
                        holder.tvStatus.setText("状态：已拒绝");
                        holder.tvStatus.setTextColor(Color.RED);
                        holder.ivBg.setImageResource(R.mipmap.ic_exchange_fail1);
                        holder.btnHaveSend.setVisibility(View.GONE);
                        holder.btnHaveFinish.setVisibility(View.GONE);
                        holder.btnHaveRefuse.setVisibility(View.GONE);
                        break;
                }
                break;

            case NoticeVO.TYPE_TRANSFER_OUT:
                holder.ivIcon.setImageResource(R.mipmap.ic_pay);
                break;

            case NoticeVO.TYPE_TRANSFER_IN:
                holder.ivIcon.setImageResource(R.mipmap.ic_receiver);
                break;

            case NoticeVO.TYPE_ADD_FRIEND:
                holder.ivIcon.setImageResource(R.mipmap.ic_add_friend);
                holder.tvStatus.setVisibility(View.VISIBLE);
                //未操作才可以看见
                if(bean.getFriendStatus()==0){
                    holder.llFriendBtn.setVisibility(View.VISIBLE);
                }else{
                    holder.llFriendBtn.setVisibility(View.GONE);
                }
                // 同意/拒绝监听
                holder.btnAgree.setOnClickListener(v -> {
                    if(mListener != null){
                        mListener.onFriendOperate(position,true);
                    }
                });
                holder.btnRefuse.setOnClickListener(v -> {
                    if(mListener != null){
                        mListener.onFriendOperate(position,false);
                    }
                });
                // 好友申请状态
                switch (bean.getFriendStatus()){
                    case 0:
                        holder.tvStatus.setText("状态：未操作");
                        holder.tvStatus.setTextColor(Color.parseColor("#FF9900"));
                        break;
                    case 1:
                        holder.tvStatus.setText("状态：已同意");
                        holder.tvStatus.setTextColor(Color.parseColor("#0099FF"));
                        break;
                    case 2:
                        holder.tvStatus.setText("状态：已拒绝");
                        holder.tvStatus.setTextColor(Color.RED);
                        break;
                }
                break;

            case NoticeVO.TYPE_SYSTEM:
                holder.ivIcon.setImageResource(R.mipmap.ic_system);
                break;
        }

        // 条目点击标记已读
        convertView.setOnClickListener(v -> {
            if(mListener != null){
                mListener.onItemClick(position);
            }
        });

        return convertView;
    }

    static class ViewHolder{
        View vUnreadDot;
        ImageView ivIcon,ivBg;
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
        TextView tvStatus;
        LinearLayout llFriendBtn,llGiftBtn;
        Button btnAgree;
        Button btnRefuse;
        Button btnHaveSend;
        Button btnHaveFinish;
        Button btnHaveRefuse;
    }
}
