package com.example.rickandmorty.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

//@Dao
//interface CharacterDetailsDao {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertCharacterDetails(character: List<CharacterDetails>)
//
//    @Query("SELECT * FROM character_details WHERE id = :id")
//    fun getCharacterDetails(id: Int): CharacterDetails
//
//    @Query("DELETE FROM character_details")
//    suspend fun clearCharacterDetails()
//}