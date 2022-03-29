package com.example.rickandmorty

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.data.characters.CharacterRepository
import com.example.rickandmorty.data.episodes.EpisodeRepository
import com.example.rickandmorty.db.AppDatabase
import com.example.rickandmorty.ui.characters.CharacterViewModelFactory
import com.example.rickandmorty.ui.episodes.EpisodeViewModelFactory

object Injection {

    private fun provideCharacterRepository(context: Context): CharacterRepository {
        return CharacterRepository(RickAndMortyApi.create(), AppDatabase.getInstance(context))
    }

    fun provideCharacterViewModelFactory(context: Context): ViewModelProvider.Factory {
        return CharacterViewModelFactory(provideCharacterRepository(context))
    }

    private fun provideEpisodeRepository(context: Context): EpisodeRepository {
        return EpisodeRepository(RickAndMortyApi.create(), AppDatabase.getInstance(context))
    }

    fun provideEpisodeViewModelFactory(context: Context): ViewModelProvider.Factory {
        return EpisodeViewModelFactory(provideEpisodeRepository(context))
    }
}