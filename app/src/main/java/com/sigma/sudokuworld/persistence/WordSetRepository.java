package com.sigma.sudokuworld.persistence;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;
import com.sigma.sudokuworld.persistence.db.AppDatabase;
import com.sigma.sudokuworld.persistence.db.daos.SetDao;
import com.sigma.sudokuworld.persistence.db.daos.PairWithSetDao;
import com.sigma.sudokuworld.persistence.db.entities.Language;
import com.sigma.sudokuworld.persistence.db.entities.Set;
import com.sigma.sudokuworld.persistence.db.entities.PairWithSet;
import com.sigma.sudokuworld.persistence.db.entities.Word;
import com.sigma.sudokuworld.persistence.db.views.WordPair;
import com.sigma.sudokuworld.persistence.firebase.FireBaseSet;
import com.sigma.sudokuworld.persistence.firebase.FireBaseWordPair;
import com.sigma.sudokuworld.persistence.firebase.FireBaseWordSet;

import java.util.LinkedList;
import java.util.List;

public class WordSetRepository {
    private PairWithSetDao mPairWithSetDao;
    private FirebaseDatabase mFireBase;
    private SetDao setDao;

    private LiveData<List<Set>> mAllSets;
    private MutableLiveData<List<FireBaseSet>> mOnlineSets;
    private LanguageRepository mLanguageRepository;
    private WordPairRepository mWordPairRepository;

    public WordSetRepository(@NonNull Application application) {
        final AppDatabase database = AppDatabase.Companion.getInstance(application);
        FirebaseApp.initializeApp(application);
        mLanguageRepository = new LanguageRepository(application);
        mWordPairRepository = new WordPairRepository(application);

        mFireBase = FirebaseDatabase.getInstance();

        mPairWithSetDao = database.getPairWithSetDao();
        setDao = database.getSetDao();
        mAllSets = setDao.getAllLiveData();


        mOnlineSets = new MutableLiveData<>();
        mFireBase.getReference().child("sets").addChildEventListener(mFireBaseSetEventListener);
    }

    /*
        Live data
     */

    public LiveData<List<FireBaseSet>> getOnlineSets() {
        return mOnlineSets;
    }

    public LiveData<List<Set>> getAllSets() {
        return mAllSets;
    }

    /*
        Fire Base
     */

    /**
     * Gets a set from FireBase db and adds it to the local db
     * @param fireBaseSet set to get
     */
    public void downloadSet(final FireBaseSet fireBaseSet) {
        mFireBase.getReference().child("sets").child(fireBaseSet.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FireBaseWordSet fireBaseWordSet = dataSnapshot.getValue(FireBaseWordSet.class);

                if (fireBaseWordSet != null) {

                    Set set = new Set(0, true, fireBaseWordSet.getName(), fireBaseWordSet.getDescription());
                    long setID = setDao.insert(set);

                    Language nLang = mLanguageRepository.getLanguageByCode(fireBaseWordSet.getNativeLanguageCode());
                    Language fLang = mLanguageRepository.getLanguageByCode(fireBaseWordSet.getForeignLanguageCode());

                    for (FireBaseWordPair wp : fireBaseWordSet.getWordPairs()) {
                        Word nWord = new Word(0, nLang.getLanguageID(), wp.getNativeWord());
                        Word fWord = new Word(0, fLang.getLanguageID(), wp.getForeignWord());

                        long pairID = mWordPairRepository.saveWordPair(nWord, fWord);
                        mPairWithSetDao.insert(new PairWithSet(setID, pairID));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Inserts a set into the FireBase database
     * @param set set to upload
     */
    public void uploadSetToFireBase(Set set) {
        String nLangName = "";
        String fLangName = "";



        List<FireBaseWordPair> fireBaseWordPairs = new LinkedList<>();
        for (WordPair wp: getAllWordPairsInSet(set.getSetID())) {

            if (nLangName.isEmpty() || fLangName.isEmpty()) {
                nLangName = wp.getNativeLanguageName();
                fLangName = wp.getForeignLanguageName();
            }

            fireBaseWordPairs.add(new FireBaseWordPair(wp.getNativeWord().getWord(), wp.getForeignWord().getWord()));
        }

        if (nLangName.isEmpty()) nLangName = "English";
        if (fLangName.isEmpty()) fLangName = "French";

        FireBaseWordSet wordSet = new FireBaseWordSet(
                "parent",
                set.getName(),
                set.getDescription(),
                mLanguageRepository.getLanguageByName(nLangName).getCode(),
                mLanguageRepository.getLanguageByName(fLangName).getCode(),
                fireBaseWordPairs);

        mFireBase.getReference().child("sets").push().setValue(wordSet);
    }

    /**
     * Deletes a set from FireBase
     * @param fireBaseSet set to delete
     */
    public void deleteFireBaseSet(FireBaseSet fireBaseSet) {
        mFireBase.getReference().child("sets").child(fireBaseSet.getKey()).removeValue();
    }

    /**
     * Listens for whens sets are added and removed from FireBase
     */
    @SuppressWarnings("FieldCanBeLocal")
    private ChildEventListener mFireBaseSetEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            FireBaseSet newSet = dataSnapshot.getValue(FireBaseSet.class);
            String key = dataSnapshot.getKey();

            if (newSet != null && key != null) {
                newSet.setKey(dataSnapshot.getKey());

                List<FireBaseSet> sets = mOnlineSets.getValue();
                if (sets == null) sets = new LinkedList<>();
                sets.add(newSet);

                mOnlineSets.setValue(sets);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            FireBaseSet removedSet = dataSnapshot.getValue(FireBaseSet.class);

            List<FireBaseSet> sets = mOnlineSets.getValue();
            if (sets != null) {
                if (sets.contains(removedSet)) {
                    sets.remove(removedSet);
                    mOnlineSets.setValue(sets);
                }
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    /*
        Local sets
     */

    public Set getSet(long setId) {
        return setDao.getSetByID(setId);
    }

    public long getSetSize(long setId) {
        return mPairWithSetDao.getPairsInSetCount(setId);
    }

    public void deleteSet(Set set) {
        setDao.delete(set);
    }

    public void saveSet(String name, String description, List<WordPair> wordPairs) {
        Set set = new Set(0, false, name, description);
        long setId = setDao.insert(set);

        for (WordPair wp : wordPairs) {
            mPairWithSetDao.insert(new PairWithSet(setId, wp.getPairID()));
        }
    }

    public Set getFirstSet() {
        return setDao.getFirstSet();
    }

    public List<WordPair> getAllWordPairsInSet(long setID) {
        return mPairWithSetDao.getAllWordPairsInSet(setID);
    }
}