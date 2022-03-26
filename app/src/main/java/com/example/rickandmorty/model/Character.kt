package com.example.rickandmorty.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class CharacterRequestResponse(
    val results: List<CharacterInfo>,
    val info: Info
)

data class CharacterInfo(
    val id: Int,
    val name: String,
    val species: String,
    val status: String,
    val gender: String,
    val image: String,
    val type: String,
    val url: String,
    val created: String,
    val origin: Origin,
    val location: Location,
    val episode: List<String>
)

@Entity(tableName = "characters")
data class CharacterData(
    @PrimaryKey val id: Int,
    val name: String,
    val species: String,
    val status: String,
    val gender: String,
    val image: String,
    val type: String,
    val url: String,
    val created: String,
    val originName: String,
    val originUrl: String,
    val locationName: String,
    val locationUrl: String,
    val episode: List<String>
)

data class Info(
    val next: String?,
    val prev: String?
)

data class Origin(
    val name: String,
    val url: String
)

data class Location(
    val name: String,
    val url: String
)
