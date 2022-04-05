# Description
### Application to search and explore animated science fiction sitcom [Rick and Morty](https://en.wikipedia.org/wiki/Rick_and_Morty).

# API
### https://rickandmortyapi.com
![Screenshot_3](https://user-images.githubusercontent.com/80069416/161750365-9c4efab9-094d-4648-8987-2328322309ae.jpg)

# App architecture
### Application is designed following [recommended app architecture](https://developer.android.com/jetpack/guide#recommended-app-arch) by Google
![Screenshot_4](https://user-images.githubusercontent.com/80069416/161754934-a00961d6-da76-400c-9add-2dd8731c0485.jpg)

# Libraries Used
- Architecture components
  - [View Binding](https://developer.android.com/topic/libraries/view-binding) - allows more easily write code that interacts with views.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - designed to store and manage UI-related data in a lifecycle conscious way.
  - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - an observable lifecycle-aware data holder class.
  - [Paging library](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) - for load and display pages of data from a larger dataset from local storage or over network.
- App navigation
  - [Navigation component](https://developer.android.com/guide/navigation) - allows to navigate across, into, and back out from the different pieces of content within app.
  - [Fragments](https://developer.android.com/guide/fragments) - represents a reusable portion of app's UI. A fragment defines and manages its own layout, has its own lifecycle, and can handle its own input events.
- Persist data
  - [Room](https://developer.android.com/training/data-storage/room) - allows to cache relevant pieces of data so that when the device cannot access the network, the users can still browse that content while they are offline.
- Third party libraries
  - [Retrofit](https://square.github.io/retrofit/) - a type-safe HTTP client for Android and Java.
  - [Glide](https://bumptech.github.io/glide/) - for image loading.
  - [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) - for dependency injection.
  - [Kotlin coroutines](https://developer.android.com/topic/libraries/architecture/coroutines) - provide an API that enables to write asynchronous code.
