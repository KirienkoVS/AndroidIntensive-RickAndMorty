package com.example.rickandmorty.db.characters

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters_remote_keys")
data class CharacterRemoteKeys(
    @PrimaryKey val characterId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)
