package com.example.rickandmorty.db.characters

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CharacterRemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeys(characterRemoteKeys: List<CharacterRemoteKeys>)

    @Query("SELECT * FROM characters_remote_keys WHERE characterId = :characterId")
    suspend fun characterIdRemoteKeys(characterId: Int): CharacterRemoteKeys?

    @Query("DELETE FROM characters_remote_keys")
    suspend fun clearRemoteKeys()
}
