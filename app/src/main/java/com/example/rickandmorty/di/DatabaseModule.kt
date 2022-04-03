package com.example.rickandmorty.di

import android.content.Context
import androidx.room.Room
import com.example.rickandmorty.db.AppDatabase
import com.example.rickandmorty.db.characters.CharacterDao
import com.example.rickandmorty.db.characters.CharacterRemoteKeysDao
import com.example.rickandmorty.db.episodes.EpisodeDao
import com.example.rickandmorty.db.episodes.EpisodeRemoteKeysDao
import com.example.rickandmorty.db.locations.LocationDao
import com.example.rickandmorty.db.locations.LocationRemoteKeysDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideCharacterDao(database: AppDatabase): CharacterDao {
        return database.characterDao()
    }
    @Provides
    fun characterRemoteKeysDao(database: AppDatabase): CharacterRemoteKeysDao {
        return database.characterRemoteKeysDao()
    }


    @Provides
    fun provideEpisodeDao(database: AppDatabase): EpisodeDao {
        return database.episodeDao()
    }
    @Provides
    fun episodeRemoteKeysDao(database: AppDatabase): EpisodeRemoteKeysDao {
        return database.episodeRemoteKeysDao()
    }


    @Provides
    fun provideLocationDao(database: AppDatabase): LocationDao {
        return database.locationDao()
    }
    @Provides
    fun locationRemoteKeysDao(database: AppDatabase): LocationRemoteKeysDao {
        return database.locationRemoteKeysDao()
    }


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "AppDatabase.db"
        ).build()
    }
}
