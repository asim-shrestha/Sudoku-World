package com.sigma.sudokuworld.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.sigma.sudokuworld.masterdetail.OnlineSetListFragment;
import com.sigma.sudokuworld.masterdetail.PairListFragment;
import com.sigma.sudokuworld.masterdetail.SetListFragment;

public class MasterDetailTabPagerAdapter extends FragmentPagerAdapter {
    private String[] tabTitles = new String[]{"Online Sets", "My Sets", "My Word Pairs"};

    public MasterDetailTabPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return new OnlineSetListFragment();
            case 1: return new SetListFragment();
            case 2:
            default:
                return new PairListFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
