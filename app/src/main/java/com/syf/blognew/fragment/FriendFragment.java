package com.syf.blognew.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSONObject;
import com.syf.blognew.R;
import com.syf.blognew.activity.ChatActivity;
import com.syf.blognew.adapter.FriendAdapter;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.pojo.UserApplication;
import com.syf.blognew.pojo.vo.FriendVO;
import com.syf.blognew.util.UnReadManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FriendFragment extends Fragment {
    private List<FriendVO> mFriendList;
    private FriendAdapter friendAdapter;
    private Context mContext;

    private final BroadcastReceiver mMsgReceiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra("message");
            JSONObject obj = JSONObject.parseObject(json);
            assert obj != null;
            String type=obj.getString("type");
            if(Objects.equals(type, "chat")){
                int fromUserId = obj.getIntValue("fromId");
                String content = obj.getString("msg");
                Date time=obj.getDate("time");
                for(FriendVO vo:mFriendList){
                    if(vo.getUser().getId()==fromUserId){
                        vo.setLastMessage(content);
                        vo.setLastTime(time);
                        int num=vo.getUnReadCount();
                        UnReadManager.getInstance().friendUnreadCount++;
                        vo.setUnReadCount(num+1);
                    }
                }
                if (UnReadManager.getInstance().onUnreadChangeListener != null) {
                    UnReadManager.getInstance().onUnreadChangeListener.run();
                }
                friendAdapter.notifyDataSetChanged(); // 红点+最新消息 立刻刷新
            }
        }
    };


    public FriendFragment() {
    }

    public static FriendFragment newInstance(String label) {
        FriendFragment fragment = new FriendFragment();
        Bundle args = new Bundle();
        args.putString("label", label);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root= inflater.inflate(R.layout.fragment_friend, container, false);
        mContext = requireActivity();
        ListView friendListView = root.findViewById(R.id.list_friend);
        mFriendList = new ArrayList<>();
        friendAdapter = new FriendAdapter(mFriendList, R.layout.item_list_friend);
        friendListView.setAdapter(friendAdapter);
        //进入聊天页面
        friendListView.setOnItemClickListener((parent, view, position, id) -> {
            FriendVO friendVO = mFriendList.get(position);
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra("userId", friendVO.getUser().getId());
            intent.putExtra("userName", friendVO.getUser().getName());
            intent.putExtra("url",friendVO.getUser().getUrl()==null?"":friendVO.getUser().getUrl());
            startActivity(intent);
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 监听全局新消息
        ContextCompat.registerReceiver(requireContext(), mMsgReceiver, new IntentFilter("NEW_MESSAGE"), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadFriendList();
    }
    @Override
    public void onHiddenChanged(boolean hidden){
        super.onHiddenChanged(hidden);
        if(!hidden) loadFriendList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireContext().unregisterReceiver(mMsgReceiver);
    }

    private void loadFriendList() {
        Runnable queryFiendList=()-> NetClient.get(ApiConstant.FRIEND_LIST, new NetCallBack() {
            @Override
            public void onFailure(int code, String msg) {
                requireActivity().runOnUiThread(()-> ToastHandler.showToast(msg));
            }
            @Override
            public void onSuccess(String json) {
                List<FriendVO> newData= JSONObject.parseArray(json, FriendVO.class);
                UnReadManager.getInstance().friendUnreadCount=0;
                for(FriendVO vo:newData){
                    UnReadManager.getInstance().friendUnreadCount+=vo.getUnReadCount();
                }
                if (UnReadManager.getInstance().onUnreadChangeListener != null) {
                    UnReadManager.getInstance().onUnreadChangeListener.run();
                }
                mFriendList.clear();
                mFriendList.addAll(0,newData);
                requireActivity().runOnUiThread(()-> friendAdapter.notifyDataSetChanged());
            }
        });
        new Thread(queryFiendList).start();
    }
}