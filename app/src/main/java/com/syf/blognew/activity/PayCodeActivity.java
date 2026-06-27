package com.syf.blognew.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.syf.blognew.R;
import com.syf.blognew.adapter.PayCodePagerAdapter;

public class PayCodeActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private final String[] tabTitles = {"收款码", "付款码"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_code);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewPager2);

        findViewById(R.id.iv_back).setOnClickListener(v->finish());

        // 设置适配器
        viewPager2.setAdapter(new PayCodePagerAdapter(this));

        // TabLayout + ViewPager2 联动
        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }
}

