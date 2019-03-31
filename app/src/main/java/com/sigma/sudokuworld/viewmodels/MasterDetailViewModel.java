package com.sigma.sudokuworld.viewmodels;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.sigma.sudokuworld.persistence.LanguageRepository;
import com.sigma.sudokuworld.persistence.WordPairRepository;
import com.sigma.sudokuworld.persistence.WordSetRepository;
import com.sigma.sudokuworld.persistence.db.entities.Language;
import com.sigma.sudokuworld.persistence.db.entities.Set;
import com.sigma.sudokuworld.persistence.db.entities.Word;
import com.sigma.sudokuworld.persistence.db.views.WordPair;
import com.sigma.sudokuworld.persistence.firebase.FireBaseSet;
import com.sigma.sudokuworld.persistence.sharedpreferences.PersistenceService;

import java.util.ArrayList;
import java.util.List;

public class MasterDetailViewModel extends BaseSettingsViewModel {
    private WordSetRepository mWordSetRepository;
    private WordPairRepository mWordPairRepository;
    private LanguageRepository mLanguageRepository;

    private LiveData<List<Set>> mAllSets;
    private LiveData<List<FireBaseSet>> mOnlineSets;
    private LiveData<List<WordPair>> mAllWordPairs;

    private MutableLiveData<List<WordPair>> mFilteredWordPairs;
    private Observer<List<WordPair>> mWordPairObserver;

    private MutableLiveData<List<Set>> mFilteredSets;
    private Observer<List<Set>> mSetObserver;

    private MutableLiveData<List<FireBaseSet>> mFilteredFirebaseSets;
    private Observer<List<FireBaseSet>> mFirebaseSetObserver;

    private String mFilterQuery;
    private int mNumberOfLocalSets;


    public MasterDetailViewModel(@NonNull Application application) {
        super(application);
        mWordSetRepository = new WordSetRepository(mApplication);
        mWordPairRepository = new WordPairRepository(mApplication);
        mLanguageRepository = new LanguageRepository(mApplication);

        mAllSets = mWordSetRepository.getAllSets();
        mAllWordPairs = mWordPairRepository.getAllWordPairs();
        mOnlineSets = mWordSetRepository.getOnlineSets();

        mFilterQuery = "";

        mWordPairObserver = new Observer<List<WordPair>>() {
            @Override
            public void onChanged(@Nullable List<WordPair> wordPairs) {
                filterWordPairs(mFilterQuery);
            }
        };
        mAllWordPairs.observeForever(mWordPairObserver);
        mFilteredWordPairs = new MutableLiveData<>();

        mSetObserver = new Observer<List<Set>>() {
            @Override
            public void onChanged(@Nullable List<Set> sets) {
                filterSets(mFilterQuery);

                if (sets != null) mNumberOfLocalSets = sets.size();
                else mNumberOfLocalSets = 0;
            }
        };
        mAllSets.observeForever(mSetObserver);
        mFilteredSets = new MutableLiveData<>();

        mFirebaseSetObserver = new Observer<List<FireBaseSet>>() {
            @Override
            public void onChanged(@Nullable List<FireBaseSet> fireBaseSets) {
                filterOnlineSets(mFilterQuery);
            }
        };
        mOnlineSets.observeForever(mFirebaseSetObserver);
        mFilteredFirebaseSets = new MutableLiveData<>();
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        mAllWordPairs.removeObserver(mWordPairObserver);
        mAllSets.removeObserver(mSetObserver);
        mOnlineSets.removeObserver(mFirebaseSetObserver);
    }

    /*
        Live Data
     */

    public LiveData<List<FireBaseSet>> getFilteredOnlineSets() {
        return mFilteredFirebaseSets;
    }

    public LiveData<List<Set>> getFilteredSets() {
        return mFilteredSets;
    }

    public LiveData<List<WordPair>> getAllWordPairs() {
        return mAllWordPairs;
    }

    public LiveData<List<WordPair>> getFilteredWordPairs() {
        return mFilteredWordPairs;
    }

    public List<Language> getAllLanguages() {
        return mLanguageRepository.getAllLanguages();
    }

    /*
        Search Filters
     */

    public void filterAllLists(String filterQuery) {
        mFilterQuery = filterQuery.toLowerCase();

        filterWordPairs(filterQuery);
        filterSets(filterQuery);
        filterOnlineSets(filterQuery);
    }

    private void filterWordPairs(String query) {
        List<WordPair> wordPairs = mAllWordPairs.getValue();
        List<WordPair> filteredWordPairs = new ArrayList<>();

        if (wordPairs != null) {
            for (WordPair wp : wordPairs) {
                if (wp.getNativeWord().getWord().toLowerCase().contains(mFilterQuery) || wp.getForeignWord().getWord().toLowerCase().contains(mFilterQuery)) {
                    filteredWordPairs.add(wp);
                }
            }
        }

        mFilteredWordPairs.setValue(filteredWordPairs);
    }

    public void filterWordPairsByLanguage(Language nativeLanguage, Language foreignLanguage) {
        List<WordPair> wordPairs = mAllWordPairs.getValue();
        List<WordPair> filteredWordPairs = new ArrayList<>();

        if (wordPairs != null) {
            for (WordPair wp : wordPairs) {
                if (wp.getNativeLanguageName().equals(nativeLanguage.getName()) && wp.getForeignLanguageName().equals(foreignLanguage.getName())) {
                    filteredWordPairs.add(wp);
                }
            }
        }

        mFilteredWordPairs.setValue(filteredWordPairs);
    }

    private void filterSets(String query) {
        List<Set> sets = mAllSets.getValue();
        List<Set> filteredSets = new ArrayList<>();

        if (sets != null) {
            for (Set set : sets) {
                if (set.getName().toLowerCase().contains(mFilterQuery)) {
                    filteredSets.add(set);
                }
            }
        }

        mFilteredSets.setValue(filteredSets);
    }

    private void filterOnlineSets(String query) {
        List<FireBaseSet> firebaseSets = mOnlineSets.getValue();
        List<FireBaseSet> filteredFirebaseSets = new ArrayList<>();

        if (firebaseSets != null) {
            for (FireBaseSet fireBaseSet : firebaseSets) {
                if (fireBaseSet.getName().toLowerCase().contains(mFilterQuery)) {
                    filteredFirebaseSets.add(fireBaseSet);
                }
            }
        }

        mFilteredFirebaseSets.setValue(filteredFirebaseSets);
    }


    /*
        Set actions
     */

    public void setSelectedSet(long setID) {
        PersistenceService.saveSetSetting(mApplication, setID);
    }

    public Set getSet(long setID) {
        return mWordSetRepository.getSet(setID);
    }

    public void saveSet(String name, String description, List<WordPair> wordPairs) {
        mWordSetRepository.saveSet(name, description, wordPairs);
    }

    public void deleteSet(Set set) {
        mWordSetRepository.deleteSet(set);
    }

    public List<WordPair> getWordsInSet(Set set) {
        return mWordSetRepository.getAllWordPairsInSet(set.getSetID());
    }

    public int getNumberOfLocalSets() {
        return mNumberOfLocalSets;
    }

    /*
        Word pairs
     */

    public boolean deletePair(WordPair wordPair) {
        return mWordPairRepository.deletePair(wordPair.getPairID());
    }

    public WordPair getWordPair(long pairID) {
        return mWordPairRepository.getWordPair(pairID);
    }

    public void saveWordPair(Word nativeWord, Word foreignWord) {
        mWordPairRepository.saveWordPair(nativeWord, foreignWord);
    }


    /*
        FireBase
     */

    public void downLoadSet(FireBaseSet fireBaseSet) {
        mWordSetRepository.downloadSet(fireBaseSet);
    }

    public void uploadSet(Set set) {
        mWordSetRepository.uploadSetToFireBase(set);
    }

    public void deleteSet(FireBaseSet fireBaseSet){
        mWordSetRepository.deleteFireBaseSet(fireBaseSet);
    }
}
