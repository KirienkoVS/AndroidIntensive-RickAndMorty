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
    suspend fun insertCharacters(characters: List<CharacterData>)

    @Query("SELECT * FROM characters")
    fun getAllCharacters(): PagingSource<Int, CharacterData>

    @Query("SELECT * FROM characters WHERE status = :filter OR gender = :filter")
    fun charactersByFilter(filter: String): PagingSource<Int, CharacterData>

    @Query("DELETE FROM characters")
    suspend fun clearCharacters()
}
