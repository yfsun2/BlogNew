package com.syf.blognew.adapter;

import android.view.View;

import com.syf.blognew.R;
import com.syf.blognew.pojo.vo.CommentVO;

import java.util.List;

public class CommentAdapter extends AbstractAdapter<CommentVO> {

    public CommentAdapter() {
    }

    public CommentAdapter(List<CommentVO> mData, int mLayoutRes) {
        super(mData, mLayoutRes);
    }

    @Override
    public void bindView(ViewHolder holder, CommentVO obj) {
        holder.setText(R.id.fromId, obj.getFromUser());
        if (obj.getToUser() == null) {
            holder.setVisibility(R.id.replay, View.GONE);
            holder.setVisibility(R.id.toId, View.GONE);
        } else {
            holder.setText(R.id.toId, obj.getToUser());
        }
        holder.setText(R.id.content, obj.getContent());
    }
}
