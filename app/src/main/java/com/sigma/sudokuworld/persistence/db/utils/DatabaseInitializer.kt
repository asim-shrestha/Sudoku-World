package com.sigma.sudokuworld.persistence.db.utils

import com.sigma.sudokuworld.persistence.db.AppDatabase
import com.sigma.sudokuworld.persistence.db.entities.*
import com.sigma.sudokuworld.persistence.db.entities.Set

abstract class DatabaseInitializer {

    //Kotlin equivalent to static
    companion object {

        fun initLanguages(db: AppDatabase) {
            val english = Language(0, "English", "en")
            val french = Language(0, "French", "fr")
            val spanish = Language(0, "Spanish", "es")
            val russian = Language(0, "Russian", "ru")

            val languageDao = db.getLanguageDao()

            if (languageDao.getLanguageByCode("en") == null) languageDao.insert(english)
            if (languageDao.getLanguageByCode("fr") == null) languageDao.insert(french)
            if (languageDao.getLanguageByCode("es") == null) languageDao.insert(spanish)
            if (languageDao.getLanguageByCode("ru") == null) languageDao.insert(russian)
        }

        fun initDefaultSet(db: AppDatabase) {
            val languageDao = db.getLanguageDao()

            if (db.getSetDao().getAll().isEmpty()) {

                //Making sure we have a clean slate for insertion
                db.getWordDao().deleteAll()

                val english = languageDao.getLanguageByCode("en")
                val french = languageDao.getLanguageByCode("fr")

                if (english != null && french != null) {
                    insertDefaultSet(db, english.languageID, french.languageID)
                }
            }
        }

        private fun insertDefaultSet(db: AppDatabase, nativeID: Long, foreignID: Long) {

            val words = arrayOf(
                    Word(1, nativeID, "Red"),
                    Word(2, nativeID, "Pink"),
                    Word(3, nativeID, "Green"),
                    Word(4, nativeID, "Purple"),
                    Word(5, nativeID, "Yellow"),
                    Word(6, nativeID, "White"),
                    Word(7, nativeID, "Black"),
                    Word(8, nativeID, "Brown"),
                    Word(9, nativeID, "Blue"),
                    Word(10, foreignID, "Rouge"),
                    Word(11, foreignID, "Rose"),
                    Word(12, foreignID, "Vert"),
                    Word(13, foreignID, "Violet"),
                    Word(14, foreignID, "Jaune"),
                    Word(15, foreignID, "Blanc"),
                    Word(16, foreignID, "Noir"),
                    Word(17, foreignID, "Marron"),
                    Word(18, foreignID, "Bleu")
            )

            val pairs = arrayOf(
                    Pair(1, 1, 10),
                    Pair(2, 2, 11),
                    Pair(3, 3, 12),
                    Pair(4, 4, 13),
                    Pair(5, 5, 14),
                    Pair(6, 6, 15),
                    Pair(7, 7, 16),
                    Pair(8, 8, 17),
                    Pair(9, 9, 18)
            )

            val set = Set(1, false,"Default Word Set", "Learn your french colours")

            val pairsWithSet = arrayOf(
                    PairWithSet(1, 1),
                    PairWithSet(1, 2),
                    PairWithSet(1, 3),
                    PairWithSet(1, 4),
                    PairWithSet(1, 5),
                    PairWithSet(1, 6),
                    PairWithSet(1, 7),
                    PairWithSet(1, 8),
                    PairWithSet(1, 9)
            )

            db.getWordDao().insert(*words)
            db.getPairDao().insert(*pairs)
            db.getSetDao().insert(set)
            db.getPairWithSetDao().insert(*pairsWithSet)
        }
    }
}