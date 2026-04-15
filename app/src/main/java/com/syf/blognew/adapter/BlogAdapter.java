package com.syf.blognew.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.syf.blognew.R;
import com.syf.blognew.pojo.vo.BlogVO;
import com.syf.blognew.pojo.req.CommentAddReq;
import com.syf.blognew.pojo.vo.CommentVO;
import com.syf.blognew.pojo.UserApplication;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.websocket.WebSocketManager;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class BlogAdapter extends AbstractAdapter<BlogVO> {
    private final SimpleDateFormat sdf;
    private final View settingView;
    private View popupView ;
    private final TextView btn_submit;
    private int toUid,index=0;//item的index
    private AlertDialog dialog;
    private final Context context;
    private final EditText inputComment;
    private PopupWindow popupWindow;
    private final RelativeLayout rl_input_container;
    private InputMethodManager mInputManager;
    private String mInputContentText;

    /**
     * 对blogItem的回调接口，把网络请求放到fragment里，不要放在adapter里
     */
    public interface OnBlogItemListener{
        void onAddSupport(int position);
        void onDeleteSupport(int position);
        void onDeleteBlog(int position,AlertDialog dialog);
        void onAddComment(int position,String content,int fromId,Integer toId);
    }
    /**
     * 变量接口
     */
    private OnBlogItemListener listener;
    /**
     * 提供set方法
     * @param listener
     */
    public void setOnItemClickListener(OnBlogItemListener listener){
        this.listener=listener;
    }

    public BlogAdapter(List<BlogVO> mData, int mLayoutRes, Context ctx) {
        super(mData,mLayoutRes);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        context=ctx;
        LayoutInflater inflater = LayoutInflater.from(context);
        settingView = inflater.inflate(R.layout.setting_view, null);
        popupView = LayoutInflater.from(context).inflate(R.layout.comment_popupwindow, null);

        inputComment = popupView.findViewById(R.id.et_discuss);
        btn_submit = popupView.findViewById(R.id.btn_confirm);
        rl_input_container = popupView.findViewById(R.id.rl_input_container);
    }

    @Override
    public void bindView(ViewHolder holder, BlogVO obj) {

        holder.setText(R.id.user, obj.getName());
        holder.setText(R.id.create_time, (obj.getCreateTime() == null ? "null" : sdf.format(obj.getCreateTime())));
        holder.setText(R.id.from, "来自" + obj.getModel());

        if(obj.getUrl()!=null&&!obj.getUrl().isEmpty()){
            holder.setImageUrl(R.id.pic,obj.getUrl());
        }else{
            holder.setImageWord(R.id.pic, obj.getName());
        }
        if(WebSocketManager.getInstance().getUser().getUrl()!=null&&!WebSocketManager.getInstance().getUser().getUrl().isEmpty()){
            holder.setButtonLeftUrl(R.id.btn_comment_user, WebSocketManager.getInstance().getUser().getUrl(),8);
        }else {
            holder.setButtonLeftImg(R.id.btn_comment_user, WebSocketManager.getInstance().getUser().getName(), 8);
        }
        if(obj.getIsSupport()==0){
            holder.setImageResource(R.id.support,R.drawable.support);
        }else{
            holder.setImageResource(R.id.support,R.drawable.support_light_full);
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
            index = holder.getItemPosition();
            if(obj.getIsSupport()==0){
                listener.onAddSupport(holder.getItemPosition());
            }else{
                listener.onDeleteSupport(holder.getItemPosition());
            }
        });

        //回复某人
        holder.setOnItemClickListener(R.id.comment_list, (parent, view, position, id) -> {
            index = holder.getItemPosition();
            toUid = obj.getCommentList().get(position).getFromUid();
            showPopupComment(holder.getItemPosition());
            btn_submit.setText("回复");
            inputComment.setHint("回复：" + obj.getCommentList().get(position).getFromUser());
        });

        //更多设置
        //TODO 目前只有删除，后续改成弹出窗口
        holder.setOnClickListener(R.id.setting, v -> {
            if (settingView.getParent() != null) {
                ((ViewGroup) settingView.getParent()).removeView(settingView);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setIcon(R.mipmap.ic_launcher)
                    .setTitle("操作")
                    .setView(settingView)
                    .create();
            dialog = builder.show();
            index = holder.getItemPosition();
        });

        if(!Objects.equals(obj.getUserId(), WebSocketManager.getInstance().getUser().getId())){
            holder.setVisibility(R.id.setting,View.GONE);
        }else{
            holder.setVisibility(R.id.setting,View.VISIBLE);
        }

        // 评论
        holder.setOnClickListener(R.id.comment, v -> {
            index = holder.getItemPosition();
            btn_submit.setText("评论");
            inputComment.setHint("评论:" +obj.getName());
            showPopupComment(index);
        });
        //说点什么吧
        holder.setOnClickListener(R.id.btn_comment_user,v->{
            index = holder.getItemPosition();
            btn_submit.setText("评论");
            inputComment.setHint("评论:" +obj.getName());
            showPopupComment(index);
        });

        holder.setText(R.id.text, obj.getContext());

        holder.setImageListToGridLayout(R.id.img_grid,obj.getImageList(),context);


        Button delete = settingView.findViewById(R.id.delete);

        delete.setOnClickListener(v -> {
           listener.onDeleteBlog(index,dialog);
        });
    }

    @SuppressLint("WrongConstant")
    private void showPopupComment(int position) {
        if (popupView == null) {
            popupView = LayoutInflater.from(context).inflate(R.layout.comment_popupwindow, null);
        }
        inputComment.setText("");
        inputComment.requestFocus();

        if (popupWindow == null) {
            popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT, false);
        }

        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);
        popupWindow.update();

        inputComment.postDelayed(()->{
            mInputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            mInputManager.showSoftInput(inputComment, InputMethodManager.SHOW_IMPLICIT);
        },150);

        popupWindow.setOnDismissListener(() -> {
            mInputManager.hideSoftInputFromWindow(inputComment.getWindowToken(), 0);
        });

        rl_input_container.setOnClickListener(v -> {
            popupWindow.dismiss();
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
            popupWindow.dismiss();
        });
    }
}


