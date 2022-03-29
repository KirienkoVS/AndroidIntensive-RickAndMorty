package com.example.rickandmorty.db.episodes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodes_remote_keys")
data class EpisodeRemoteKeys(
    @PrimaryKey val episodeId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)