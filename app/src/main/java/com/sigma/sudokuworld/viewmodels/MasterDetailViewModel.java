package com.sigma.sudokuworld.viewmodels;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.sigma.sudokuworld.persistence.WordPairRepository;
import com.sigma.sudokuworld.persistence.WordSetRepository;
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
    private LiveData<List<Set>> mAllSets;
    private LiveData<List<FireBaseSet>> mOnlineSets;
    private LiveData<List<WordPair>> mAllWordPairs;

    private MutableLiveData<List<WordPair>> mFilteredWordPairs;
    private Observer<List<WordPair>> mWordPairObserver;

    private MutableLiveData<List<Set>> mFilteredSets;
    private Observer<List<Set>> mSetObserver;

    private MutableLiveData<List<FireBaseSet>> mFilteredFirebaseSets;
    private Observer<List<FireBaseSet>> mFirebaseSetObserver;

    private String filterQuery;


    public MasterDetailViewModel(@NonNull Application application) {
        super(application);
        mWordSetRepository = new WordSetRepository(mApplication);
        mWordPairRepository = new WordPairRepository(mApplication);

        mAllSets = mWordSetRepository.getAllSets();
        mAllWordPairs = mWordPairRepository.getAllWordPairs();
        mOnlineSets = mWordSetRepository.getOnlineSets();

        filterQuery = "";

        mWordPairObserver = new Observer<List<WordPair>>() {
            @Override
            public void onChanged(@Nullable List<WordPair> wordPairs) {
                filterWordPairs(filterQuery);
            }
        };
        mAllWordPairs.observeForever(mWordPairObserver);
        mFilteredWordPairs = new MutableLiveData<>();

        mSetObserver = new Observer<List<Set>>() {
            @Override
            public void onChanged(@Nullable List<Set> sets) {
                filterSets(filterQuery);
            }
        };
        mAllSets.observeForever(mSetObserver);
        mFilteredSets = new MutableLiveData<>();

        mFirebaseSetObserver = new Observer<List<FireBaseSet>>() {
            @Override
            public void onChanged(@Nullable List<FireBaseSet> fireBaseSets) {
                filterOnlineSets(filterQuery);
            }
        };
        mOnlineSets.observeForever(mFirebaseSetObserver);
        mFilteredFirebaseSets = new MutableLiveData<>();
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        mAllWordPairs.removeObserver(mWordPairObserver);
    }

    public LiveData<List<FireBaseSet>> getOnlineSets() {
        return mOnlineSets;
    }

    public LiveData<List<FireBaseSet>> getFilteredOnlineSets() {
        return mFilteredFirebaseSets;
    }

    public LiveData<List<Set>> getAllSets() {
        return mAllSets;
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

    public void filterWordPairs(String query) {
        filterQuery = query.toLowerCase();
        List<WordPair> wordPairs = mAllWordPairs.getValue();
        List<WordPair> filteredWordPairs = new ArrayList<>();

        if (wordPairs != null) {
            for (WordPair wp : wordPairs) {
                if (wp.getNativeWord().getWord().toLowerCase().contains(filterQuery) || wp.getForeignWord().getWord().toLowerCase().contains(filterQuery)) {
                    filteredWordPairs.add(wp);
                }
            }
        }

        mFilteredWordPairs.setValue(filteredWordPairs);
    }

    public void filterSets(String query) {
        filterQuery = query.toLowerCase();
        List<Set> sets = mAllSets.getValue();
        List<Set> filteredSets = new ArrayList<>();

        if (sets != null) {
            for (Set set : sets) {
                if (set.getName().toLowerCase().contains(filterQuery)) {
                    filteredSets.add(set);
                }
            }
        }

        mFilteredSets.setValue(filteredSets);
    }

    public void filterOnlineSets(String query) {
        filterQuery = query.toLowerCase();
        List<FireBaseSet> firebaseSets = mOnlineSets.getValue();
        List<FireBaseSet> filteredFirebaseSets = new ArrayList<>();

        if (firebaseSets != null) {
            for (FireBaseSet fireBaseSet : firebaseSets) {
                if (fireBaseSet.getName().toLowerCase().contains(filterQuery)) {
                    filteredFirebaseSets.add(fireBaseSet);
                }
            }
        }

        mFilteredFirebaseSets.setValue(filteredFirebaseSets);
    }

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

    public boolean deletePair(WordPair wordPair) {
        return mWordPairRepository.deletePair(wordPair.getPairID());
    }

    public WordPair getWordPair(long pairID) {
        return mWordPairRepository.getWordPair(pairID);
    }

    public void saveWordPair(Word nativeWord, Word foreignWord) {
        mWordPairRepository.saveWordPair(nativeWord, foreignWord);
    }

    //FIRE BASE
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
