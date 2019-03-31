package com.sigma.sudokuworld.masterdetail.detail;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.adapters.PairRecyclerViewAdapter;
import com.sigma.sudokuworld.persistence.db.entities.Set;
import com.sigma.sudokuworld.persistence.db.views.WordPair;
import com.sigma.sudokuworld.persistence.sharedpreferences.KeyConstants;

import java.util.List;


public class SetDetailFragment extends AbstractDrillDownFragment {
    private Set mSet;
    private List<WordPair> mWordPairs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSet = mMasterDetailViewModel.getSet(getArguments().getLong(KeyConstants.SET_ID_KEY));
        mWordPairs = mMasterDetailViewModel.getWordsInSet(mSet);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_detail, container, false);
        TextView nameTextView = view.findViewById(R.id.setName);
        TextView descriptionTextView = view.findViewById(R.id.setDescription);
        TextView nativeLanguageName = view.findViewById(R.id.nativeLangName);
        TextView foreignLanguageName = view.findViewById(R.id.foreignLangName);

        if (mSet != null) {
            nameTextView.setText(mSet.getName());
            descriptionTextView.setText(mSet.getDescription());
            nativeLanguageName.setText(mWordPairs.get(0).getNativeLanguageName());
            foreignLanguageName.setText(mWordPairs.get(0).getForeignLanguageName());

            mAppBarLayout.setTitle(mSet.getName());
        }

        PairRecyclerViewAdapter adapter = new PairRecyclerViewAdapter(null);
        adapter.setItems(mWordPairs);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }
}