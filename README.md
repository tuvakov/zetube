# ZeTube

A YouTube client app that shows videos from only user's subscriptions.
The app enables the user to watch YouTube videos without distractions such as recommendations comments, etc. 
that can lead into a deep rabbit whole. All data is stored locally, no backend is involved.

## Libraries used

- JetPack
  - [LiveData][0] - Build a observable and lifecycle-aware data objects for UI-related purposes.
  - [ViewModel][1] - Store UI-related data and protect from configuration changes.
  - [Room][2] - ORM that manages Sqlite DB without boilerplate code and well integrated with other JetPack libraries.

- Others
  - [Dagger2][3] - for depency injection.
  - [Glide][4] - for image loading.
  - [YouTubeDataV3 APIs][5] - for fetching user YouTube subscription list.
  - [Android YouTube Player][6] - for playing YouTube videos
  - [Easy Permissions][7] - for permission handling.
 
 [0]: https://developer.android.com/topic/libraries/architecture/livedata
 [1]: https://developer.android.com/topic/libraries/architecture/viewmodel
 [2]: https://developer.android.com/topic/libraries/architecture/room
 [3]: https://github.com/google/dagger
 [4]: https://github.com/bumptech/glide
 [5]: https://developers.google.com/youtube/v3/getting-started
 [6]: https://github.com/PierfrancescoSoffritti/android-youtube-player
 [7]: https://github.com/googlesamples/easypermissions
