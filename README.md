# ZeTube

A YouTube client app that shows videos from only user's subscriptions.
The app enables the user to watch YouTube videos without distractions such as
recommendations, comments, etc. that can lead into a deep rabbit hole. 
All data is stored locally, no back-end is involved.

<img src="screenshots/animated.gif" alt="Animated Screenshot" width="350">

## How it works

After necessary permission (Contacts) is granted, the app fetches only last 7
days' videos for each channel in the user's subscription list. The user can navigate different
sections of the app via a navigation bar in the left.\
Synchronization is allowed after 3 hours from the last synchronization.
Older videos are deleted during the synchronization. However, the user can save
videos to prevent them being deleted during the synchronization (refer to above screenshot).\
All stored data can be deleted via delete button on the right top corner of the screen.

## Getting started

To further customize the app:

0. Download or clone this repository.
1. Change the package name (ApplicationId) of the project.
2. Make a new Google oAuth credentials for the app to fetch YouTube data.
[Link](https://developers.google.com/youtube/v3/quickstart/android) to the guide page.
3. Build and test the project.

## Libraries used

- JetPack
  - [LiveData][0] - Build a observable and lifecycle-aware data objects for UI-related purposes.
  - [ViewModel][1] - Store UI-related data and protect from configuration changes.
  - [Room][2] - ORM that manages Sqlite DB without boilerplate code and well integrated with other JetPack libraries.

- Others
  - [Kotlin Coroutines][3] - for asynchronous operations.
  - [Dagger2][4] - for dependency injection.
  - [Glide][5] - for image loading.
  - [YouTubeDataV3 APIs][6] - for fetching user YouTube subscription list.
  - [Android YouTube Player][7] - for playing YouTube videos.
  - [Easy Permissions][8] - for permission handling.

 [0]: https://developer.android.com/topic/libraries/architecture/livedata
 [1]: https://developer.android.com/topic/libraries/architecture/viewmodel
 [2]: https://developer.android.com/topic/libraries/architecture/room
 [3]: https://kotlinlang.org/docs/reference/coroutines-overview.html
 [4]: https://github.com/google/dagger
 [5]: https://github.com/bumptech/glide
 [6]: https://developers.google.com/youtube/v3/getting-started
 [7]: https://github.com/PierfrancescoSoffritti/android-youtube-player
 [8]: https://github.com/googlesamples/easypermissions
