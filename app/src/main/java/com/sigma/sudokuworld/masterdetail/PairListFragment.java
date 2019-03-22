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
import com.sigma.sudokuworld.persistence.db.views.WordPair;
import com.sigma.sudokuworld.adapters.PairRecyclerViewAdapter;
import com.sigma.sudokuworld.viewmodels.MasterDetailViewModel;

import java.util.List;

public class PairListFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private PairRecyclerViewAdapter mAdapter;
    private MasterDetailViewModel mMasterDetailViewModel;
    private String filterQuery = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMasterDetailViewModel = ViewModelProviders.of(this).get(MasterDetailViewModel.class);
        filterList(filterQuery);

        mAdapter = new PairRecyclerViewAdapter(mListener);
        mMasterDetailViewModel.getFilteredWordPairs().observe(this, new Observer<List<WordPair>>() {
            @Override
            public void onChanged(@Nullable List<WordPair> wordPairs) {
                mAdapter.setItems(wordPairs);
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
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
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
            mMasterDetailViewModel.filterWordPairs(query);
        }
    }

    public interface OnFragmentInteractionListener {
        void onClickPairFragmentInteraction(WordPair wordPair);
        void onLongPairClickFragmentInteraction(WordPair wordPair);
    }
}

