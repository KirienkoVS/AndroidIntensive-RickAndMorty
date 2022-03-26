package com.example.rickandmorty.db

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun listToString(list: List<String>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun stringToList(string: String): List<String> {
        return Json.decodeFromString(string)
    }
}