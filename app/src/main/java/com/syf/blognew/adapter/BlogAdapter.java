package com.syf.blognew.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.VerifiedInputEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.syf.blognew.R;
import com.syf.blognew.pojo.vo.BlogVO;
import com.syf.blognew.util.Utils;
import com.syf.blognew.websocket.WebSocketManager;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BlogAdapter extends AbstractAdapter<BlogVO> {
    private View popupInputView ,popupSettingView;
    private final TextView btn_submit;
    private int toUid;//item的index
    private final Context context;
    private final EditText inputComment;
    private PopupWindow popupInput,popupSetting;
    private final RelativeLayout rl_input_container;
    private InputMethodManager mInputManager;
    private String mInputContentText;

    public interface OnBlogItemListener{
        void onAddSupport(int position);
        void onDeleteSupport(int position);
        void onDeleteBlog(int position);
        void onEditBlog(int position);
        void onSetPrivateBlog(int position);
        void onsetPublicBlog(int position);
        void onAddComment(int position,String content,int fromId,Integer toId);
    }
    private OnBlogItemListener listener;
    public void setOnItemClickListener(OnBlogItemListener listener){
        this.listener=listener;
    }

    public BlogAdapter(List<BlogVO> mData, int mLayoutRes, Context ctx) {
        super(mData,mLayoutRes);
        context=ctx;
        LayoutInflater inflater = LayoutInflater.from(context);
        popupInputView = inflater.inflate(R.layout.popup_window_comment, null);

        inputComment = popupInputView.findViewById(R.id.et_discuss);
        btn_submit = popupInputView.findViewById(R.id.btn_confirm);
        rl_input_container = popupInputView.findViewById(R.id.rl_input_container);

        popupSettingView=inflater.inflate(R.layout.popup_window_setting, null);
    }

    @Override
    public void bindView(ViewHolder holder,int position, BlogVO obj) {

        holder.setText(R.id.user, obj.getName());
        holder.setText(R.id.create_time, (obj.getCreateTime() == null ? "null" : Utils.timeFormatter(obj.getCreateTime())));
        holder.setText(R.id.from, "来自" + obj.getModel());

        if(obj.getIsPrivate()==1){
            holder.setVisibility(R.id.iv_private, View.VISIBLE);
        }else{
            holder.setVisibility(R.id.iv_private, View.GONE);
        }

        if(obj.getUrl()!=null&&!obj.getUrl().isEmpty()){
            holder.setImageUrl(R.id.pic,obj.getUrl());
        }else{
            holder.setImageWord(R.id.pic, obj.getName());
        }
        if(WebSocketManager.getInstance().getUser().getUrl()!=null&&!WebSocketManager.getInstance().getUser().getUrl().isEmpty()){
            holder.setImageUrl(R.id.iv_comment_user,WebSocketManager.getInstance().getUser().getUrl());
        }else {
            holder.setImageWord(R.id.iv_comment_user,WebSocketManager.getInstance().getUser().getName());
        }
        if(obj.getIsSupport()==0){
            holder.setImageResource(R.id.support,R.mipmap.support);
        }else{
            holder.setImageResource(R.id.support,R.mipmap.support_light_full);
        }
        //有点赞的人才显示点赞列表
        if(obj.getSupportList()!=null&&!obj.getSupportList().isEmpty()){
            holder.setVisibility(R.id.support_layout,View.VISIBLE);
            holder.setText(R.id.support_list,obj.getSupportList().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("、")));
            if(obj.getSupportList().size()>1){
                holder.setText(R.id.support_count,obj.getSupportList().size()+"人赞了");
            }else{
                holder.setText(R.id.support_count,"赞了");
            }
        }else{
            holder.setVisibility(R.id.support_layout,View.GONE);
        }

        CommentAdapter commentAdapter = new CommentAdapter(obj.getCommentList(), R.layout.item_list_index_reply);
        holder.setAdapter(R.id.comment_list, commentAdapter);

        //点赞
        holder.setOnClickListener(R.id.support,v->{
            if(obj.getIsSupport()==0){
                listener.onAddSupport(position);
            }else{
                listener.onDeleteSupport(position);
            }
        });

        //回复某人
        holder.setOnItemClickListener(R.id.comment_list, (parent, view, pos, id) -> {
            toUid = obj.getCommentList().get(pos).getFromUid();
            showPopupComment(pos);
            btn_submit.setText("回复");
            inputComment.setHint("回复：" + obj.getCommentList().get(pos).getFromUser());
        });

        //更多设置
        holder.setOnClickListener(R.id.setting, v -> {
            showPopupSetting(obj,position);
        });

        if(!Objects.equals(obj.getUserId(), WebSocketManager.getInstance().getUser().getId())){
            holder.setVisibility(R.id.setting,View.GONE);
        }else{
            holder.setVisibility(R.id.setting,View.VISIBLE);
        }

        // 评论
        holder.setOnClickListener(R.id.comment, v -> {
            btn_submit.setText("评论");
            inputComment.setHint("评论:" +obj.getName());
            showPopupComment(position);
        });
        //说点什么吧
        holder.setOnClickListener(R.id.ll_comment_user,v->{
            btn_submit.setText("评论");
            inputComment.setHint("评论:" +obj.getName());
            showPopupComment(position);
        });

        holder.setText(R.id.text, obj.getContext());

        holder.setImageListToGridLayout(R.id.img_grid,obj.getImageList(),context);

    }

    private void showPopupComment(int position) {
        if (popupInputView == null) {
            popupInputView = LayoutInflater.from(context).inflate(R.layout.popup_window_comment, null);
        }
        inputComment.setText("");
        inputComment.requestFocus();

        if (popupInput == null) {
            popupInput = new PopupWindow(popupInputView, RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        }

        popupInput.setTouchable(true);
//        popupWindow.setFocusable(true);
        popupInput.setOutsideTouchable(true);
        popupInput.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupInput.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupInput.showAtLocation(popupInputView, Gravity.BOTTOM, 0, 0);
        popupInput.update();

        inputComment.postDelayed(()->{
            mInputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            mInputManager.showSoftInput(inputComment, InputMethodManager.SHOW_IMPLICIT);
        },150);

        popupInput.setOnDismissListener(() -> {
            mInputManager.hideSoftInputFromWindow(inputComment.getWindowToken(), 0);
        });

        rl_input_container.setOnClickListener(v -> {
            popupInput.dismiss();
        });

        // 提交评论
        btn_submit.setOnClickListener(v -> {
            mInputContentText = inputComment.getText().toString().trim();
            if (btn_submit.getText().toString().equals("评论")) {
                listener.onAddComment(position,mInputContentText,WebSocketManager.getInstance().getUser().getId(), null);
            } else {
                listener.onAddComment(position,mInputContentText,WebSocketManager.getInstance().getUser().getId(), toUid);
            }
            mInputManager.hideSoftInputFromWindow(inputComment.getWindowToken(), 0);
            popupInput.dismiss();
        });
    }

    private void showPopupSetting(BlogVO obj,int position){
        if (popupSettingView == null) {
            popupSettingView = LayoutInflater.from(context).inflate(R.layout.popup_window_setting, null);
        }

        if (popupSetting == null) {
            popupSetting = new PopupWindow(popupSettingView, RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        }
        popupSetting.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupSetting.setOutsideTouchable(true);
        setWindowAlpha(0.5f); // 半透明（推荐0.5~0.7）

        popupSetting.setTouchable(true);
        popupSetting.setOutsideTouchable(true);
        popupSetting.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupSetting.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupSetting.showAtLocation(popupInputView, Gravity.BOTTOM, 0, 0);
        popupSetting.update();

        rl_input_container.setOnClickListener(v -> {
            popupSetting.dismiss();
        });

        if(obj.getIsPrivate()==0){
            popupSettingView.findViewById(R.id.ll_private).setVisibility(View.VISIBLE);
            popupSettingView.findViewById(R.id.ll_public).setVisibility(View.GONE);
        }else{
            popupSettingView.findViewById(R.id.ll_private).setVisibility(View.GONE);
            popupSettingView.findViewById(R.id.ll_public).setVisibility(View.VISIBLE);
        }

        //编辑
        popupSettingView.findViewById(R.id.ll_edit).setOnClickListener(v->{
            listener.onEditBlog(position);
            popupSetting.dismiss();
        });
        //私密
        popupSettingView.findViewById(R.id.ll_private).setOnClickListener(v->{
            listener.onSetPrivateBlog(position);
            popupSetting.dismiss();
        });
        //公开
        popupSettingView.findViewById(R.id.ll_public).setOnClickListener(v->{
            listener.onsetPublicBlog(position);
            popupSetting.dismiss();
        });
        //删除
        popupSettingView.findViewById(R.id.ll_delete).setOnClickListener(v->{
            listener.onDeleteBlog(position);
            popupSetting.dismiss();
        });
        popupSetting.setOnDismissListener(()-> setWindowAlpha(1.0f));
    }

    private void setWindowAlpha(float alpha) {
        WindowManager.LayoutParams lp = ((AppCompatActivity)context).getWindow().getAttributes();
        // 添加背景变暗标志（解决部分机型不生效问题）
        ((AppCompatActivity)context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        lp.alpha = alpha; // 0.0f全透明，1.0f不透明
        ((AppCompatActivity)context).getWindow().setAttributes(lp);
    }
}


