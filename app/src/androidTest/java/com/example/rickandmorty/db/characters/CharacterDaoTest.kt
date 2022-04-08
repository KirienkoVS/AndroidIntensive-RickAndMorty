package com.example.rickandmorty.db.characters

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.rickandmorty.db.AppDatabase
import com.example.rickandmorty.getOrAwaitValue
import com.example.rickandmorty.model.CharacterData
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class CharacterDaoTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var characterDao: CharacterDao
    private lateinit var characterData: CharacterData

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        characterDao = database.characterDao()
        characterData = CharacterData(
                id = 1, name = "Rick", species = "Human", status = "Alive", gender = "Male", image = "",
                type = "", created = "", originName = "Earth", locationName = "Earth", episode = emptyList()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertCharacters() = runTest {
        characterDao.insertCharacters(listOf(characterData))
        val character = characterDao.getCharacterDetails(1).getOrAwaitValue()

        assertThat(character).isEqualTo(characterData)
    }

    @Test
    fun clearCharacters() = runTest {
        characterDao.insertCharacters(listOf(characterData))
        characterDao.clearCharacters()
        val character = characterDao.getCharacterDetails(1).getOrAwaitValue()

        assertThat(character).isNull()
    }

}