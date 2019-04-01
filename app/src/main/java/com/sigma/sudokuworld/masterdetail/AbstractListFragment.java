package com.sigma.sudokuworld.masterdetail;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.*;
import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.viewmodels.MasterDetailViewModel;

public abstract class AbstractListFragment extends Fragment {

    protected MasterDetailViewModel mMasterDetailViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Shared view model
        if (this.getActivity() != null) {
            mMasterDetailViewModel = ViewModelProviders.of(this.getActivity()).get(MasterDetailViewModel.class);
        } else {
            mMasterDetailViewModel = ViewModelProviders.of(this).get(MasterDetailViewModel.class);
        }
    }
}
