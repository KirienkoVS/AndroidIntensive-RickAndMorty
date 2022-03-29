package com.example.rickandmorty.db.episodes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EpisodeRemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeys(episodeRemoteKeys: List<EpisodeRemoteKeys>)

    @Query("SELECT * FROM episodes_remote_keys WHERE episodeId = :episodeId")
    suspend fun episodeIdRemoteKeys(episodeId: Int): EpisodeRemoteKeys?

    @Query("DELETE FROM episodes_remote_keys")
    suspend fun clearRemoteKeys()
}