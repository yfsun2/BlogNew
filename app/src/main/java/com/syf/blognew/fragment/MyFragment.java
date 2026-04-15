package com.syf.blognew.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.syf.blognew.R;
import com.syf.blognew.activity.LoginActivity;
import com.syf.blognew.pojo.entity.User;
import com.syf.blognew.pojo.UserApplication;
import com.syf.blognew.service.BackgroundNotificationService;
import com.syf.blognew.util.ImageUtil;
import com.syf.blognew.util.SpUtil;
import com.syf.blognew.view.RoundImageView;
import com.syf.blognew.websocket.WebSocketManager;


public class MyFragment extends Fragment {

    public MyFragment() {
    }

    public static MyFragment newInstance(String label) {
        MyFragment fragment = new MyFragment();
        Bundle args = new Bundle();
        args.putString("label",label);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        RoundImageView portrait = requireActivity().findViewById(R.id.portrait);
        User own= WebSocketManager.getInstance().getUser();
        byte[] pic=own.getPic();
        String url=own.getUrl();
        if(url!=null&&!url.isEmpty()) Glide.with(this).load(url).into(portrait);
        else if(pic!=null){
            portrait.setImageBitmap(BitmapFactory.decodeByteArray(pic,0, pic.length));
        }else if(own.getName() != null && !own.getName().isEmpty()){
            portrait.setImageBitmap(ImageUtil.getBitmapByFirst(String.valueOf(own.getName().charAt(0))));
        }

        Button logout = requireActivity().findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            SpUtil.clearToken();
            SpUtil.logout();
            // 发送退出广播，让服务自我销毁
            UserApplication.getAppContext().sendBroadcast(new Intent(BackgroundNotificationService.ACTION_LOGOUT));

            Intent intent=new Intent();
            intent.setClass(requireActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my, container, false);
    }
}