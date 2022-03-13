package com.example.rickandmorty

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.rickandmorty.api.RickAndMortyApi
import com.example.rickandmorty.data.CharacterRepository
import com.example.rickandmorty.db.CharacterDatabase
import com.example.rickandmorty.ui.characters.CharacterViewModelFactory

object Injection {

    private fun provideCharacterRepository(context: Context): CharacterRepository {
        return CharacterRepository(RickAndMortyApi.create(), CharacterDatabase.getInstance(context))
    }

    fun provideCharacterViewModelFactory(context: Context): ViewModelProvider.Factory {
        return CharacterViewModelFactory(provideCharacterRepository(context))
    }
}