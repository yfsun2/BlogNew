package com.syf.blognew.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.syf.blognew.R;
import com.syf.blognew.pojo.vo.UserVO;

import java.util.List;

public class FriendSearchAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<UserVO> mDataList;
    private OnAddFriendListener listener;

    public interface OnAddFriendListener{
        void onAdd(int position, UserVO bean);
    }

    public void setOnAddFriendListener(OnAddFriendListener listener){
        this.listener = listener;
    }

    public FriendSearchAdapter(Context context, List<UserVO> dataList) {
        this.mContext = context;
        this.mDataList = dataList;
    }

    @Override
    public int getCount() {
        return mDataList == null ? 0 : mDataList.size();
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
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_friend_search, null);
            holder = new ViewHolder();
            holder.ivAvatar = convertView.findViewById(R.id.iv_avatar);
            holder.tvName = convertView.findViewById(R.id.tv_name);
            holder.tvAccount = convertView.findViewById(R.id.tv_account);
            holder.btnAdd = convertView.findViewById(R.id.btn_add);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        UserVO bean = mDataList.get(position);
        if(bean.getUrl()!=null&&!bean.getUrl().isEmpty()){
            Glide.with(mContext).load(bean.getUrl()).into(holder.ivAvatar);
        }
        holder.tvName.setText(bean.getName());
        holder.tvAccount.setText("ID:" + bean.getId());

        holder.btnAdd.setOnClickListener(v -> {
            if(listener != null){
                listener.onAdd(position,bean);
            }
        });
        return convertView;
    }

    static class ViewHolder{
        ImageView ivAvatar;
        TextView tvName;
        TextView tvAccount;
        Button btnAdd;
    }
}