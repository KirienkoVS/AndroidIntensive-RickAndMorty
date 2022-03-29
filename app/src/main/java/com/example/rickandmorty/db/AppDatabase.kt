package com.example.rickandmorty.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.rickandmorty.db.characters.CharacterDao
import com.example.rickandmorty.db.characters.CharacterRemoteKeys
import com.example.rickandmorty.db.characters.CharacterRemoteKeysDao
import com.example.rickandmorty.db.episodes.EpisodeDao
import com.example.rickandmorty.db.episodes.EpisodeRemoteKeys
import com.example.rickandmorty.db.episodes.EpisodeRemoteKeysDao
import com.example.rickandmorty.model.CharacterData
import com.example.rickandmorty.model.EpisodeData

@Database(
    entities = [
        CharacterData::class, CharacterRemoteKeys::class,
        EpisodeData::class, EpisodeRemoteKeys::class
               ], version = 1, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun characterDao(): CharacterDao
    abstract fun characterRemoteKeysDao(): CharacterRemoteKeysDao

    abstract fun episodeDao(): EpisodeDao
    abstract fun episodeRemoteKeysDao(): EpisodeRemoteKeysDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "AppDatabase.db"
                ).build()
                INSTANCE = instance
                instance
            }
    }
}
