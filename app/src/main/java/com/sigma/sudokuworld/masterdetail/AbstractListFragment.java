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
        mMasterDetailViewModel = ViewModelProviders.of(this).get(MasterDetailViewModel.class);

        //Clearing search results on rotate TODO: search persists rotate. VIEW models make this trick
        if (savedInstanceState != null) {
            filterList("");
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        SearchView searchView = (SearchView) menu.findItem(R.id.searchItem).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return false;
            }
        });
    }

    public abstract void filterList(String query);
}
