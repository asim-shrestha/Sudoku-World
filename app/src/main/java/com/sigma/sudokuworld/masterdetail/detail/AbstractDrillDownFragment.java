package com.sigma.sudokuworld.masterdetail.detail;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.viewmodels.MasterDetailViewModel;

public abstract class AbstractDrillDownFragment extends Fragment {
    CollapsingToolbarLayout mAppBarLayout;
    MasterDetailViewModel mMasterDetailViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity activity = this.getActivity();
        if (activity != null) {
            mAppBarLayout = activity.findViewById(R.id.toolbar_layout);
            mAppBarLayout.setExpandedTitleColor(getResources().getColor(R.color.colorBlack));

            //Getting the view model of parent activity
            mMasterDetailViewModel = ViewModelProviders.of(this.getActivity()).get(MasterDetailViewModel.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                AnimatedVectorDrawable avd = (AnimatedVectorDrawable) ContextCompat.getDrawable(getContext(), R.drawable.avd_menu);
                avd.start();
                mAppBarLayout.setBackground(avd);
            }
        }
    }
}
