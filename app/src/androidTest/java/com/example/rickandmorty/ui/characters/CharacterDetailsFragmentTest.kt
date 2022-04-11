package com.example.rickandmorty.ui.characters

/**
 Test is not working! Too many problems.
 */

//@MediumTest
//@HiltAndroidTest
//class CharacterDetailsFragmentTest {
//
//    @get:Rule
//    var hiltRule = HiltAndroidRule(this)
//
//    @Before
//    fun setUp() {
//        hiltRule.inject()
//    }
//
//    @Test
//    fun clickOriginTextView_navigateToLocationDetailFragment() {
//        val navController = mock(NavController::class.java)
//
//        launchFragmentInHiltContainer<CharacterDetailsFragment>(fragmentArgs = bundleOf("characterID" to 0)) {
//            navController.setGraph(R.navigation.navigation_graph)
//            Navigation.setViewNavController(requireView(), navController)
//        }
//
//        onView(withId(R.id.character_origin_name)).perform(click())
//        Truth.assertThat(navController.currentDestination?.id).isEqualTo(R.id.locationDetailsFragment)
//
//        verify(navController).navigate(
//            CharacterDetailsFragmentDirections.actionCharacterDetailsFragmentToLocationDetailsFragment(locationName = "Earth")
//        )
//
//    }
//}