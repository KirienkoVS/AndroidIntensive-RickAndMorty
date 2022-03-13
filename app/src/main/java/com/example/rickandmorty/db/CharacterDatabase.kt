package com.example.rickandmorty.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.rickandmorty.model.CharacterData

@Database(entities = [CharacterData::class, RemoteKeys::class], version = 1, exportSchema = false)
abstract class CharacterDatabase : RoomDatabase() {

    abstract fun characterDao(): CharacterDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {

        @Volatile
        private var INSTANCE: CharacterDatabase? = null

        fun getInstance(context: Context): CharacterDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CharacterDatabase::class.java,
                    "Character.db"
                ).build()
                INSTANCE = instance
                instance
            }
    }
}
