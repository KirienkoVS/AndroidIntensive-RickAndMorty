package com.example.rickandmorty.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodes")
data class EpisodeData(
    @PrimaryKey val id: Int,
    val name: String,
    val airDate: String,
    val episodeNumber: String,
    val characters: List<String>,
    val url: String,
    val created: String
)
