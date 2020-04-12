package com.tuvakov.zetube.android.utils

import android.util.Log
import com.google.api.services.youtube.YouTube
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.repository.SubscriptionRepo
import com.tuvakov.zetube.android.repository.VideoRepo
import kotlinx.coroutines.*
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/*
 * TODO (1): If the subscription or videos responses have more items in next pages, they should
 *           be fetched.
 *
 * TODO (2): Limit coroutine pool to 10 courtines at a time.
 *
 * TODO (3): Test sequential fetch and parallel fetch.
 */

@Singleton
class SyncUtils @Inject constructor(
        private val mYouTubeApiUtils: YouTubeApiUtils,
        private val mPrefUtils: PrefUtils,
        private val mVideoRepo: VideoRepo,
        private val mSubscriptionRepo: SubscriptionRepo,
        private val mDateTimeUtils: DateTimeUtils
) {

    suspend fun sync() = withContext(Dispatchers.IO) {

        if (!mDateTimeUtils.hasDayPassed(mPrefUtils.lastSyncTime)) {
            Log.d(TAG, "onHandleIntent: Immature sync")
            throw Exception("immature-sync")
        }

        val youTubeService = mYouTubeApiUtils.youTubeService
        if (youTubeService == null) {
            Log.d(TAG, "onHandleIntent: Couldn't get service. Probably account name is null")
            throw Exception()
        }

        val startingDay = mDateTimeUtils.getUtcEpochNDaysAgo(LAST_DAY_NO)
        val subscriptions = getSubscriptions(youTubeService)
        val videos = getVideos(youTubeService, subscriptions, startingDay)
        mSubscriptionRepo.deleteAll()
        // TODO: Hidden and saved videos should be excluded later.
        mVideoRepo.deleteAll()
        mSubscriptionRepo.bulkInsert(subscriptions)
        mVideoRepo.bulkInsert(videos)
        mPrefUtils.saveLastSyncTime(mDateTimeUtils.utcEpoch)
    }


    @Throws(IOException::class)
    private suspend fun getSubscriptions(youTubeService: YouTube): List<Subscription> =
            withContext(Dispatchers.IO) {
                val list = youTubeService.subscriptions()
                        .list("snippet")
                        .setMine(true)
                        .setMaxResults(MAX_LIMIT_SUBS)
                        .setOrder("alphabetical")
                val subResponse = list.execute()
                subResponse.items.map {
                    val st = it.snippet
                    Subscription(st.resourceId.channelId, st.title,
                            st.description, st.thumbnails.medium.url)
                }
            }

    @Throws(IOException::class)
    private suspend fun getVideos(youTubeService: YouTube,
                                  subscriptions: List<Subscription>,
                                  startingDay: Long): List<Video> {
        val videos = mutableListOf<Video>()
        supervisorScope {
            val deferreds = mutableListOf<Deferred<Boolean>>()
            for (subscription in subscriptions) {
                deferreds.add(async {
                    videos.addAll(getVideosForSubscription(youTubeService, subscription, startingDay))
                })
            }
            deferreds.awaitAll()
        }
        return videos
    }

    private suspend fun getVideosForSubscription(youTubeService: YouTube,
                                                 subscription: Subscription,
                                                 startingDay: Long): List<Video> =
            withContext(Dispatchers.IO) {

                val uploadPlayListId = subscription.id.replaceFirst("[C]".toRegex(), "U")

                val list = youTubeService.playlistItems()
                        .list("snippet")
                        .setMaxResults(MAX_LIMIT_VIDEOS)
                        .setPlaylistId(uploadPlayListId)

                val response = list.execute()
                val items = response.items

                items.filter { it.snippet.publishedAt.value >= startingDay }.map {
                    val st = it.snippet
                    Video(id = st.resourceId.videoId,
                            title = st.title,
                            thumbnail = st.thumbnails.high.url,
                            description = st.description,
                            channelId = subscription.id,
                            channelTitle = subscription.title,
                            channelAvatar = subscription.thumbnail,
                            publishedAt = st.publishedAt.value
                    )
                }
            }

    companion object {
        private const val TAG = "SyncUtils"

        const val STATUS_SYNC_IDLE = 0
        const val STATUS_SYNC_STARTED = 10
        const val STATUS_SYNC_SUCCESS = 11
        const val STATUS_SYNC_FAILURE = 12
        const val STATUS_SYNC_GOOGLE_PLAY_FAILURE = 13
        const val STATUS_SYNC_AUTH_FAILURE = 14
        const val STATUS_IMMATURE_SYNC = 20

        private const val MAX_LIMIT_SUBS: Long = 50
        private const val MAX_LIMIT_VIDEOS: Long = 30
        private const val LAST_DAY_NO = 7
    }
}