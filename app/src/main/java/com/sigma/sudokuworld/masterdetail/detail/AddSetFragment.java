package com.sigma.sudokuworld.masterdetail.detail;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.adapters.LanguageSpinnerAdapter;
import com.sigma.sudokuworld.persistence.db.entities.Language;
import com.sigma.sudokuworld.persistence.db.views.WordPair;
import com.sigma.sudokuworld.adapters.CheckedPairRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class AddSetFragment extends AbstractDrillDownFragment {
    private OnFragmentInteractionListener mListener;

    private TextInputEditText mNameInput;
    private TextInputEditText mDescriptionInput;
    private Spinner mNativeLanguageSpinner;
    private Spinner mForeignLanguageSpinner;

    private LanguageSpinnerAdapter mLanguageSpinnerAdapter;
    private CheckedPairRecyclerViewAdapter mCheckedPairRecyclerViewAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCheckedPairRecyclerViewAdapter = new CheckedPairRecyclerViewAdapter(mListener);

        ArrayList<Language> languages = (ArrayList<Language>) mMasterDetailViewModel.getAllLanguages();
        mLanguageSpinnerAdapter = new LanguageSpinnerAdapter(getContext(),  languages);

        mMasterDetailViewModel.getFilteredWordPairs().observe(this, new Observer<List<WordPair>>() {
            @Override
            public void onChanged(@Nullable List<WordPair> wordPairs) {
                mCheckedPairRecyclerViewAdapter.setItems(wordPairs);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_set, container, false);
        mNameInput = view.findViewById(R.id.nameInput);
        mDescriptionInput = view.findViewById(R.id.descriptionInput);

        mNativeLanguageSpinner = view.findViewById(R.id.nativeLanguageSpinner);
        mNativeLanguageSpinner.setAdapter(mLanguageSpinnerAdapter);

        mNativeLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mListener.onNativeLanguageSelectedFragmentInteraction(mLanguageSpinnerAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //stub
            }
        });

        mForeignLanguageSpinner = view.findViewById(R.id.foreignLanguageSpinner);
        mForeignLanguageSpinner.setAdapter(mLanguageSpinnerAdapter);

        mForeignLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mListener.onForeignLanguageSelectedFragmentInteraction(mLanguageSpinnerAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //stub
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(mCheckedPairRecyclerViewAdapter);

        mAppBarLayout.setTitle("Create Word Set");

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

    public void setFilteredPairs(List<WordPair> wordPairs) {
        mCheckedPairRecyclerViewAdapter.setItems(wordPairs);
    }

    public String getSetName() {
        return mNameInput.getText().toString();
    }

    public String getSetDescription() {
        return mDescriptionInput.getText().toString();
    }

    public interface OnFragmentInteractionListener {
        void onCheckChangedFragmentInteraction(WordPair wordPair, Boolean isChecked);
        void onNativeLanguageSelectedFragmentInteraction(Language language);
        void onForeignLanguageSelectedFragmentInteraction(Language language);
    }
}
