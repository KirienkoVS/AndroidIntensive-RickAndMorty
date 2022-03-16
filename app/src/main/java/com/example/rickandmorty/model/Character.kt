package com.example.rickandmorty.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class CharacterRequestResponse(
    val results: List<CharacterData>
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
