package com.example.rickandmorty.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rickandmorty.model.CharacterData

@Dao
interface CharacterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterData>)

    @Query("SELECT * FROM characters")
    fun allCharacters(): PagingSource<Int, CharacterData>

//    @Query("SELECT * FROM characters WHERE name = :name")
//    fun charactersByName(name: String): PagingSource<Int, CharacterData>

    @Query("DELETE FROM characters")
    suspend fun clearCharacters()
}
