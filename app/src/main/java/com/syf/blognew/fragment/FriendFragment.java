package com.syf.blognew.fragment;

import static com.syf.blognew.service.BackgroundNotificationService.ACTION_NEW_MESSAGE;

import android.annotation.SuppressLint;
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

import com.alibaba.fastjson.JSONObject;
import com.syf.blognew.R;
import com.syf.blognew.activity.ChatActivity;
import com.syf.blognew.adapter.FriendAdapter;
import com.syf.blognew.dialog.FriendSearchDialog;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.pojo.vo.FriendVO;
import com.syf.blognew.util.UnReadManager;
import com.syf.blognew.util.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendFragment extends Fragment {
    private List<FriendVO> friendList;
    private FriendAdapter friendAdapter;
    private Context ctx;

    private final BroadcastReceiver messageReceiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra("message");
            JSONObject obj = JSONObject.parseObject(json);
            assert obj != null;
            String type=obj.getString("type");
            if(Objects.equals(type, "chat")){
                int fromUserId = obj.getIntValue("fromId");
                String content = obj.getString("msg");
                LocalDateTime time =   LocalDateTime.parse(obj.getString("time"), Utils.FORMATTER);
                for(FriendVO vo:friendList){
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
                friendAdapter.notifyDataSetChanged();
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ctx = requireActivity();
        ListView friendListView = view.findViewById(R.id.list_friend);
        friendList = new ArrayList<>();
        friendAdapter = new FriendAdapter(friendList, R.layout.item_list_friend);
        friendListView.setAdapter(friendAdapter);
        @SuppressLint("InflateParams") View emptyView = LayoutInflater.from(ctx).inflate(R.layout.empty_view, null);
        ((ViewGroup)friendListView.getParent()).addView(emptyView,friendListView.getLayoutParams());
        friendListView.setEmptyView(emptyView);
        //进入聊天页面
        friendListView.setOnItemClickListener((parent, view1, position, id) -> {
            FriendVO friendVO = friendList.get(position);
            Intent intent = new Intent(ctx, ChatActivity.class);
            intent.putExtra("userId", friendVO.getUser().getId());
            intent.putExtra("userName", friendVO.getUser().getName());
            intent.putExtra("url",friendVO.getUser().getUrl()==null?"":friendVO.getUser().getUrl());
            startActivity(intent);
        });

        ContextCompat.registerReceiver(requireContext(), messageReceiver, new IntentFilter(ACTION_NEW_MESSAGE), ContextCompat.RECEIVER_NOT_EXPORTED);

        view.findViewById(R.id.btn_add_friend).setOnClickListener(v->{
            FriendSearchDialog searchDialog = new FriendSearchDialog(requireContext());
            searchDialog.show();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        loadFriendList();
    }
    @Override
    public void onResume() {
        super.onResume();
        loadFriendList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireContext().unregisterReceiver(messageReceiver);
    }

    private void loadFriendList() {
        Runnable th=()-> NetClient.get(ApiConstant.FRIEND_LIST, new NetCallBack() {
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
                friendList.clear();
                friendList.addAll(0,newData);
                requireActivity().runOnUiThread(()-> friendAdapter.notifyDataSetChanged());
            }
        });
        new Thread(th).start();
    }
}