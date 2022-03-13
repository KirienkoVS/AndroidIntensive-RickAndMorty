package com.example.rickandmorty.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class CharacterRequestResponse(
    val results: List<CharacterData>,
    val info: CharacterRequestInfo
)
@Entity(tableName = "characters")
data class CharacterData(
    @PrimaryKey val id: Int,
    val name: String,
    val species: String,
    val status: String,
    val gender: String,
    val image: String
)
data class CharacterRequestInfo(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?,
)
