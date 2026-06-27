package com.syf.blognew.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.syf.blognew.fragment.PayCodeFragment;
import com.syf.blognew.fragment.ReceiveCodeFragment;

public class PayCodePagerAdapter extends FragmentStateAdapter {
    private static final int PAGE_COUNT = 2;

    public PayCodePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new ReceiveCodeFragment();
        } else {
            return new PayCodeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }
}