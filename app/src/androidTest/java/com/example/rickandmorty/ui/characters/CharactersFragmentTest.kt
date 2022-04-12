package com.example.rickandmorty.ui.characters


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import com.example.rickandmorty.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
@HiltAndroidTest
class CharactersFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun isFragmentDisplayed() {
        val fragment = onView(
            allOf(withId(R.id.characters_page), withContentDescription("Characters"),
                withParent(withParent(withId(R.id.bottom_navigation_view))),
                isDisplayed()
            )
        )

        fragment.check(matches(isDisplayed()))
    }
}
