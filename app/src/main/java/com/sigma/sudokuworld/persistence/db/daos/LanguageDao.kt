package com.sigma.sudokuworld.persistence.db.daos

import android.arch.persistence.room.*
import com.sigma.sudokuworld.persistence.db.entities.Language

@Dao
interface LanguageDao {

    @Query("SELECT * FROM languages")
    fun getAll(): List<Language>

    @Query("SELECT * FROM languages WHERE code = :languageCode")
    fun getLanguageByCode(languageCode: String): Language?

    @Query("SELECT * FROM languages WHERE name = :languageName")
    fun getLanguageByName(languageName: String): Language?

    @Query("SELECT * FROM languages WHERE languageID = :languageID")
    fun getLanguageByID(languageID: Int): Language?

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(vararg languages: Language)

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(language: Language): Long

    @Query("DELETE FROM languages")
    fun deleteAll()
}