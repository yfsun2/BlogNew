package com.syf.blognew.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.syf.blognew.R;
import com.syf.blognew.fragment.FriendFragment;
import com.syf.blognew.fragment.HomeFragment;
import com.syf.blognew.fragment.MyFragment;
import com.syf.blognew.util.UnReadManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String[] TAB_TITLES = {"首页", "好友", "我的"};
    private final int[] unSelectIcons = {R.mipmap.ic_index_normal, R.mipmap.ic_friend_normal, R.mipmap.ic_mine_normal};
    private final int[] selectIcons = {R.mipmap.ic_index_selected, R.mipmap.ic_friend_selected, R.mipmap.ic_mine_selected};
    private final List<Fragment> mFragmentList = new ArrayList<>();

    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;

    TextView badgeView1,badgeView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestNotificationPermission();
        requestIgnoreBatteryOptimization();

        initView();
        initFragments();
        setupViewPager();
    }

    // 在Activity的onCreate调用
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    private void requestIgnoreBatteryOptimization() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
            @SuppressLint("BatteryLife") Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);
        UnReadManager.getInstance().onUnreadChangeListener = this::updateFriendBadge;
        UnReadManager.getInstance().onNoticeChangeListener = this::updateNoticeBadge;
    }

    /**
     * 添加Fragment
     */
    private void initFragments() {
        mFragmentList.add(HomeFragment.newInstance(TAB_TITLES[0]));
        mFragmentList.add(FriendFragment.newInstance(TAB_TITLES[1]));
        mFragmentList.add(MyFragment.newInstance(TAB_TITLES[2]));
    }

    /**
     * 配置ViewPager+适配器
     */
    private void setupViewPager() {
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setUserInputEnabled(false);
        mViewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                return mFragmentList.size();
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return mFragmentList.get(position);
            }
        });
        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            switch (position) {
                case 0 -> {
                    tab.setIcon(R.mipmap.ic_index_selected);
                    tab.setText("首页");
                }
                case 1 -> {
                    tab.setIcon(R.mipmap.ic_friend_normal);
                    tab.setText("好友");
                    ViewGroup tabView = tab.view;
                    badgeView1 = (TextView) LayoutInflater.from(this).inflate(R.layout.badge_view, null);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            60, 60 // 宽高
                    );
                    params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
                    params.rightMargin = 60;  // 往左
                    params.topMargin = -100;    // 往上
                    badgeView1.setLayoutParams(params);
                    tabView.addView(badgeView1);
                }
                case 2-> {
                    tab.setIcon(R.mipmap.ic_mine_normal);
                    tab.setText("我的");
                    ViewGroup tabView = tab.view;
                    badgeView2 = (TextView) LayoutInflater.from(this).inflate(R.layout.badge_view, null);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            60, 60 // 宽高
                    );
                    params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
                    params.rightMargin = 60;  // 往左
                    params.topMargin = -100;    // 往上
                    badgeView2.setLayoutParams(params);
                    tabView.addView(badgeView2);
                }
            }
        }).attach();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                tab.setIcon(selectIcons[position]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                tab.setIcon(unSelectIcons[position]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }


    /**
     * 返回键弹窗退出
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("是否退出应用？")
                    .setPositiveButton("取消", null)
                    .setNegativeButton("确定", (dialog, which) -> finish())
                    .show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateFriendBadge() {
        int count = UnReadManager.getInstance().friendUnreadCount;
        if (badgeView1 == null) return;
        runOnUiThread(()->{
            if (count <= 0) {
                // 没有未读 → 隐藏
                badgeView1.setVisibility(View.GONE);
            } else {
                // 有未读 → 显示数字
                badgeView1.setVisibility(View.VISIBLE);
                badgeView1.setText(count>99?"99+":String.valueOf(count));
            }
        });
    }

    private void updateNoticeBadge() {
        int count = UnReadManager.getInstance().noticeUnreadCount;
        if (badgeView2 == null) return;
        runOnUiThread(()->{
            if (count <= 0) {
                // 没有未读 → 隐藏
                badgeView2.setVisibility(View.GONE);
            } else {
                // 有未读 → 显示数字
                badgeView2.setVisibility(View.VISIBLE);
                badgeView2.setText(count>99?"99+":String.valueOf(count));
            }
        });
    }

}