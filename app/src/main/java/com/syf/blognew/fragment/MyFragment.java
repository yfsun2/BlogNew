package com.syf.blognew.fragment;

import static com.syf.blognew.service.BackgroundNotificationService.ACTION_NEW_MESSAGE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.syf.blognew.R;
import com.syf.blognew.activity.GiftActivity;
import com.syf.blognew.activity.LoginActivity;
import com.syf.blognew.activity.ModifyPayPwdActivity;
import com.syf.blognew.activity.MyCaptureActivity;
import com.syf.blognew.activity.NfcActivity;
import com.syf.blognew.activity.NoticeActivity;
import com.syf.blognew.activity.PayCodeActivity;
import com.syf.blognew.activity.TransferActivity;
import com.syf.blognew.activity.UserSettingActivity;
import com.syf.blognew.api.ApiConstant;
import com.syf.blognew.api.NetCallBack;
import com.syf.blognew.api.NetClient;
import com.syf.blognew.dialog.RedPacketDialog;
import com.syf.blognew.handler.ToastHandler;
import com.syf.blognew.pojo.entity.User;
import com.syf.blognew.util.ImageUtil;
import com.syf.blognew.util.SpUtil;
import com.syf.blognew.util.UnReadManager;
import com.syf.blognew.view.RoundImageView;
import com.syf.blognew.websocket.WebSocketManager;

import java.util.Objects;


public class MyFragment extends Fragment {

    private TextView tvUnread;
    private Context ctx;
    private final BroadcastReceiver messageReceiver= new BroadcastReceiver() {
    @SuppressLint("SetTextI18n")
    @Override
    public void onReceive(Context context, Intent intent) {
        String json = intent.getStringExtra("message");
        JSONObject obj = JSONObject.parseObject(json);
        assert obj != null;
        String type=obj.getString("type");
        if(Objects.equals(type, "notice")){
            UnReadManager.getInstance().noticeUnreadCount++;
            if (UnReadManager.getInstance().onNoticeChangeListener != null) {
                UnReadManager.getInstance().onNoticeChangeListener.run();
            }
            requireActivity().runOnUiThread(()->{
                if(UnReadManager.getInstance().noticeUnreadCount<=0){
                    tvUnread.setVisibility(View.GONE);
                }else if(UnReadManager.getInstance().noticeUnreadCount<100){
                    tvUnread.setVisibility(View.VISIBLE);
                    tvUnread.setText(String.valueOf(UnReadManager.getInstance().noticeUnreadCount));
                }else{
                    tvUnread.setVisibility(View.VISIBLE);
                    tvUnread.setText("99+");
                }
            });
        }
    }
};

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ctx=requireActivity();
        ContextCompat.registerReceiver(ctx, messageReceiver, new IntentFilter(ACTION_NEW_MESSAGE), ContextCompat.RECEIVER_NOT_EXPORTED);
        tvUnread=view.findViewById(R.id.tv_unread_badge);

        RoundImageView portrait = view.findViewById(R.id.portrait);
        User currentUser= WebSocketManager.getInstance().getUser();
        if(currentUser==null) return;
        String url=currentUser.getUrl();
        if(url!=null&&!url.isEmpty()) Glide.with(this).load(url).into(portrait);
        else if(currentUser.getName() != null && !currentUser.getName().isEmpty()){
            portrait.setImageBitmap(ImageUtil.getBitmapByFirst(String.valueOf(currentUser.getName().charAt(0))));
        }

        TextView tvUserName=view.findViewById(R.id.tv_username);
        tvUserName.setText(currentUser.getName());

        TextView tvUserId=view.findViewById(R.id.tv_userid);
        tvUserId.setText(currentUser.getId().toString());

        view.findViewById(R.id.iv_scan).setOnClickListener(v-> startScan());

        view.findViewById(R.id.ll_points_change).setOnClickListener(v->{
            Intent intent=new Intent(requireActivity(), GiftActivity.class);
            startActivity(intent);
        });


        view.findViewById(R.id.ll_pay_setting).setOnClickListener(v->{
            Intent intent=new Intent(requireActivity(), ModifyPayPwdActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.ll_user_setting).setOnClickListener(v->{
            Intent intent=new Intent(requireActivity(), UserSettingActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.ll_nfc).setOnClickListener(v->{
            Intent intent=new Intent(requireActivity(), NfcActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.ll_notice).setOnClickListener(v->{
            Intent intent=new Intent(requireActivity(), NoticeActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.ll_pay_code).setOnClickListener(v->{
            Intent intent=new Intent(requireActivity(), PayCodeActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            SpUtil.logout();
            Intent intent=new Intent(ctx, LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getUnreadNotice();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ctx.unregisterReceiver(messageReceiver);
    }

    private void getUnreadNotice(){
        Runnable getThread=()-> NetClient.get(ApiConstant.NOTICE_UNREAD, new NetCallBack() {
            @Override
            public void onFailure(int code, String msg) {
                requireActivity().runOnUiThread(()-> ToastHandler.showToast(msg));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(String json) {
                if(json.isEmpty()) return;
                UnReadManager.getInstance().noticeUnreadCount= Integer.parseInt(json);
                UnReadManager.getInstance().onNoticeChangeListener.run();
                requireActivity().runOnUiThread(()->{
                    if(UnReadManager.getInstance().noticeUnreadCount<=0){
                        tvUnread.setVisibility(View.GONE);
                    }else if(UnReadManager.getInstance().noticeUnreadCount<100){
                        tvUnread.setVisibility(View.VISIBLE);
                        tvUnread.setText(String.valueOf(UnReadManager.getInstance().noticeUnreadCount));
                    }else{
                        tvUnread.setVisibility(View.VISIBLE);
                        tvUnread.setText("99+");
                    }
                });
            }
        });
        new Thread(getThread).start();
    }
    private void startScan() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setOrientationLocked(true);
        integrator.setCaptureActivity(MyCaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("扫描积分收款码/付款码/红包码");
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result == null || result.getContents() == null) return;

        String content = result.getContents();
        JSONObject obj;
        try{
            obj = JSONObject.parseObject(content);
            String type=obj.getString("type");
            if(Objects.equals(type,"receive")){//收款码带金额
//                String orderNo=obj.getString("orderNo");
                Integer userId=obj.getInteger("userId");
                String userName=obj.getString("userName");
                String url=obj.getString("url");
                int point=obj.getIntValue("point");

                Intent intent=new Intent(requireActivity(), TransferActivity.class);
                intent.putExtra("toUserId",userId);
                intent.putExtra("userName",userName);
                intent.putExtra("url",url);
                intent.putExtra("point",point);
                startActivity(intent);

            }else if(Objects.equals(type,"pay")){
                String code=obj.getString("code");
                Integer userId=obj.getInteger("userId");
                showPointDialog(code,userId);
            }else if(Objects.equals(type,"hongbao")){
                String orderNo=obj.getString("orderNo");
                // 弹出红包弹窗
                RedPacketDialog packetDialog = new RedPacketDialog(ctx);
                packetDialog.setOnOpenListener(() -> getHongbao(orderNo));
                packetDialog.show();

            }
        } catch (Exception e) {
            ToastHandler.showToast("非本软件官方二维码");
            Log.e("MyFragment", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void showPointDialog(String code,Integer customerId) {
        EditText etPoint = new EditText(ctx);
        etPoint.setInputType(InputType.TYPE_CLASS_NUMBER);
        etPoint.setHint("请输入收款金额");

        new AlertDialog.Builder(ctx)
                .setTitle("输入收款金额")
                .setView(etPoint)
                .setPositiveButton("确定", (dialog, which) -> {
                    String point = etPoint.getText().toString().trim();
                    if (TextUtils.isEmpty(point)) {
                        ToastHandler.showToast("金额不能为空");
                        return;
                    }
                    scanPayCode(code,Integer.parseInt(point),customerId);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 扫付款码扣款
    private void scanPayCode(String payCode, int point,Integer customerId) {
        String url = ApiConstant.PAY_BY_PAY_CODE
                + "?qrCode=" + payCode
                + "&merchantId=" + customerId
                + "&point=" + point;
        Runnable thread=()-> NetClient.get(url, new NetCallBack() {
            @Override
            public void onSuccess(String data) {
                if (!isAdded() || getActivity() == null) return;
                requireActivity().runOnUiThread(()-> ToastHandler.showToast("收款成功"));
            }

            @Override
            public void onFailure(int code, String msg) {
                if (!isAdded() || getActivity() == null) return;
                requireActivity().runOnUiThread(() -> ToastHandler.showToast(msg));
            }
        });
        new Thread(thread).start();
    }

    public void getHongbao(String orderNo){
        Runnable thread=()-> NetClient.get(ApiConstant.GET_HONGBAO+orderNo, new NetCallBack() {
            @Override
            public void onFailure(int code, String msg) {
                if(getActivity()==null) return;
                requireActivity().runOnUiThread(()-> ToastHandler.showToast(msg));
            }

            @Override
            public void onSuccess(String json) {
                requireActivity().runOnUiThread(()->{
                    int num=Integer.parseInt(json);
                    if(num==0){
                        ToastHandler.showToast("未中奖");
                    }else{
                        ToastHandler.showToast("恭喜您，中奖"+json+"积分");
                    }
                });
            }
        });
        new Thread(thread).start();
    }

}