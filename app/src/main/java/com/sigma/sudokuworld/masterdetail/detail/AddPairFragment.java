package com.sigma.sudokuworld.masterdetail.detail;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Spinner;
import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.adapters.LanguageSpinnerAdapter;
import com.sigma.sudokuworld.persistence.db.entities.Language;

import java.util.ArrayList;

public class AddPairFragment extends AbstractDrillDownFragment {
    private TextInputEditText mNativeWordInput;
    private TextInputEditText mForeignWordInput;

    private LanguageSpinnerAdapter mLanguageSpinnerAdapter;
    private Spinner mNativeSpinner;
    private Spinner mForeignSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_pair, container, false);

        mAppBarLayout.setTitle("Add Word Pair");
        mNativeWordInput = view.findViewById(R.id.nativeInput);
        mForeignWordInput = view.findViewById(R.id.foreignInput);
        mNativeSpinner = view.findViewById(R.id.nativeSpinner);
        mForeignSpinner = view.findViewById(R.id.foreignSpinner);

        ArrayList<Language> languages = (ArrayList<Language>) mMasterDetailViewModel.getAllLanguages();
        mLanguageSpinnerAdapter = new LanguageSpinnerAdapter(getContext(), languages);

        mNativeSpinner.setAdapter(mLanguageSpinnerAdapter);
        mForeignSpinner.setAdapter(mLanguageSpinnerAdapter);

        return view;
    }

    public Language getNativeLanguage() {
        return (Language) mNativeSpinner.getSelectedItem();
    }

    public Language getForeignLanguage() {
        return (Language) mForeignSpinner.getSelectedItem();
    }

    public String getNativeWord() {
        return mNativeWordInput.getText().toString();
    }

    public String getForeignWord() {
        return mForeignWordInput.getText().toString();
    }
}

