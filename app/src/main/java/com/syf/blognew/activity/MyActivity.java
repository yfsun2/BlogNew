package com.syf.blognew.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONArray;
import com.syf.blognew.R;
import com.syf.blognew.adapter.AbstractAdapter;
import com.syf.blognew.pojo.entity.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyActivity extends AppCompatActivity {

    private List<User> list=new ArrayList<>();

    private ListView listView;

    private OkHttpClient client;

    public static Context mContext;

    private Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1){
                list = JSONArray.parseArray(String.valueOf( msg.obj),User.class);
//                MyAdapter adapter = new MyAdapter(MyActivity.this, R.layout.user_item, list);
                AbstractAdapter<User> adapter=new AbstractAdapter<User>((ArrayList<User>) list, R.layout.user_item) {
                    @Override
                    public void bindView(ViewHolder holder, User obj) {
                        holder.setText(R.id.user_id,String.valueOf(obj.getId()));
                        holder.setText(R.id.user_name,obj.getName());
                        holder.setText(R.id.user_age,String.valueOf(obj.getEmail()));
                        holder.setText(R.id.user_email,obj.getEmail());
                    }
                };
                listView.setAdapter(adapter);
            }else if(msg.what==2){
                Toast.makeText(getApplicationContext(),String.valueOf(msg.obj),Toast.LENGTH_SHORT).show();
//                Intent intent=new Intent();
//                intent.setClass(MyActivity.this, MainActivity.class);
//                startActivity(intent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        mContext = getApplicationContext();
        listView = findViewById(R.id.list_view);
        new Thread(() -> {
            try {
                //创建OkHttpClient对象
                client = new OkHttpClient();
                //创建Request
                Request request = new Request.Builder()
                        .url("http://10.5.175.185:8081/queryAll")//访问连接
                        .get()
                        .build();
                //创建Call对象
                Call call = client.newCall(request);
                //通过execute()方法获得请求响应的Response对象
                Response response = call.execute();
                if (response.isSuccessful()) {
                    //处理网络请求的响应，处理UI需要在UI线程中处理
                    String s=response.body().string();
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = s;
                    handler.sendMessage(msg);
                }else {
                    Message msg = new Message();
                    msg.what = 2;
                    msg.obj = "服务器找不到";
                    handler.sendMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Message msg = new Message();
                msg.what = 2;
                msg.obj = "服务器找不到";
                handler.sendMessage(msg);
            }
        }).start();
    }
}