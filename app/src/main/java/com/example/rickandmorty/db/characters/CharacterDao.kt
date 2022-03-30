package com.example.rickandmorty.db.characters

import androidx.lifecycle.LiveData
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

    @Query("SELECT * FROM characters WHERE " +
            "(name LIKE :name OR :name IS NULL) AND " +
            "(species LIKE :species OR :species IS NULL) AND " +
            "(status LIKE :status OR :status IS NULL) AND " +
            "(gender LIKE :gender OR :gender IS NULL)")
    fun charactersByFilter(
        name: String?,
        species: String?,
        status: String?,
        gender: String?
    ): PagingSource<Int, CharacterData>

    @Query("SELECT * FROM characters WHERE id = :id")
    fun getCharacterDetails(id: Int): LiveData<CharacterData>

    @Query("SELECT * FROM characters WHERE id IN (:id)")
    fun getLocationOrEpisodeCharacters(id: List<Int>): LiveData<List<CharacterData>>

    @Query("DELETE FROM characters")
    suspend fun clearCharacters()

}
