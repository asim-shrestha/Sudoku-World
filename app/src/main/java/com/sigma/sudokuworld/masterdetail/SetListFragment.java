package com.sigma.sudokuworld.masterdetail;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.adapters.SetRecyclerViewAdapter;
import com.sigma.sudokuworld.persistence.db.entities.Set;
import com.sigma.sudokuworld.viewmodels.MasterDetailViewModel;

import java.util.List;


public class SetListFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private MasterDetailViewModel mMasterDetailViewModel;
    private SetRecyclerViewAdapter mAdapter;
    private String filterQuery = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMasterDetailViewModel = ViewModelProviders.of(this).get(MasterDetailViewModel.class);
        filterList(filterQuery);

        mAdapter = new SetRecyclerViewAdapter(mListener);
        mMasterDetailViewModel.getFilteredSets().observe(this, new Observer<List<Set>>() {
            @Override
            public void onChanged(@Nullable List<Set> sets) {
                mAdapter.setItems(sets);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSetListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void filterList(String query) {
        if (query == null) query = "";
        filterQuery = query;

        if(mMasterDetailViewModel != null) {
            mMasterDetailViewModel.filterSets(query);
        }
    }

    public interface OnFragmentInteractionListener {
        void onClickSetFragmentInteraction(Set set);
        void onLongClickSetFragmentInteraction(View view, Set set);
    }
}
