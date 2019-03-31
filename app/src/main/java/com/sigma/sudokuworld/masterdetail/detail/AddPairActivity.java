package com.sigma.sudokuworld.masterdetail.detail;

import android.os.Bundle;
import android.view.View;
import com.sigma.sudokuworld.R;
import com.sigma.sudokuworld.persistence.db.entities.Language;
import com.sigma.sudokuworld.persistence.db.entities.Word;

public class AddPairActivity extends AbstractDrillDownActivity {
    private AddPairFragment mAddPairFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAddPairFragment = new AddPairFragment();
        getSupportFragmentManager().beginTransaction()
                .add(mFragmentContainerID, mAddPairFragment)
                .commit();

        mFAB.setImageResource(R.drawable.ic_save_black_24dp);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveWordPair();
            }
        });
    }

    private void saveWordPair() {
        String nWord = mAddPairFragment.getNativeWord();
        String fWord = mAddPairFragment.getForeignWord();
        Language nLang = mAddPairFragment.getNativeLanguage();
        Language fLang = mAddPairFragment.getForeignLanguage();


        if (nWord != null && fWord != null) {
            if (!nWord.isEmpty() && !fWord.isEmpty()) {

                Word nativeWord = new Word(0, nLang.getLanguageID(), nWord);
                Word foreignWord = new Word(0, fLang.getLanguageID(), fWord);

                mMasterDetailViewModel.saveWordPair(nativeWord, foreignWord);
                finish();
            }
        }
    }
}
