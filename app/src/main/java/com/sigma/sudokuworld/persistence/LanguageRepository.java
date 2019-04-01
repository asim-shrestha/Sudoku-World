package com.sigma.sudokuworld.persistence;

import android.app.Application;
import android.support.annotation.NonNull;
import com.sigma.sudokuworld.persistence.db.AppDatabase;
import com.sigma.sudokuworld.persistence.db.daos.LanguageDao;
import com.sigma.sudokuworld.persistence.db.entities.Language;

import java.util.List;

public class LanguageRepository {
    private LanguageDao mLanguageDao;

    public LanguageRepository(@NonNull Application application) {
        mLanguageDao = AppDatabase.Companion.getInstance(application).getLanguageDao();
    }

    public List<Language> getAllLanguages() {
        return mLanguageDao.getAll();
    }

    public Language getLanguageByCode(String code) {
        return mLanguageDao.getLanguageByCode(code);
    }

    public Language getLanguageByName(String name) {
        return mLanguageDao.getLanguageByName(name);
    }
}
