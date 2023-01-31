package com.tuvakov.zetube.android.utils

import android.util.Log
import com.google.api.services.youtube.YouTube
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.repository.Repository
import com.tuvakov.zetube.android.ui.channel.ImmatureSyncException
import kotlinx.coroutines.*
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/*
 * TODO (1): Limit coroutine pool to 10 courtines at a time.
 */

@Singleton
class SyncUtils @Inject constructor(
        private val mYouTubeApiUtils: YouTubeApiUtils,
        private val mPrefUtils: PrefUtils,
        private val repository: Repository,
        private val mDateTimeUtils: DateTimeUtils
) {

    suspend fun sync() = withContext(Dispatchers.IO) {

        if (!mDateTimeUtils.isSyncAllowed(mPrefUtils.lastSyncTime)) {
            Log.d(TAG, "Immature sync")
            throw ImmatureSyncException()
        }

        val youTubeService = mYouTubeApiUtils.youTubeService
        if (youTubeService == null) {
            Log.d(TAG, "Couldn't get service. Probably account name is null")
            throw Exception()
        }

        val startingDay = mDateTimeUtils.getUtcEpochNDaysAgo(LAST_DAY_NO)
        val subscriptions = getSubscriptions(youTubeService)
        val videos = getVideos(youTubeService, subscriptions, startingDay)
        repository.insertDataAfterSync(subscriptions, videos)
        mPrefUtils.saveLastSyncTime(mDateTimeUtils.utcEpoch)
    }


    @Throws(IOException::class)
    private suspend fun getSubscriptions(youTubeService: YouTube): List<Subscription> =
            withContext(Dispatchers.IO) {
                val list = youTubeService.subscriptions()
                        .list(listOf("snippet"))
                        .setMine(true)
                        .setMaxResults(MAX_LIMIT_SUBS)
                        .setOrder("alphabetical")
                var subResponse = list.execute()
                val totalResults = subResponse.pageInfo.totalResults
                val subscriptions = mutableListOf<Subscription>()
                subscriptions.addAll(subResponse.items.map { it.toLocalSubscription() })

                /* If all of the fetched subscriptions were added then there might be more
                   in the next page. */
                while (subscriptions.size < totalResults) {
                    list.pageToken = subResponse.nextPageToken
                    subResponse = list.execute()
                    subscriptions.addAll(subResponse.items.map { it.toLocalSubscription() })
                }
                return@withContext subscriptions
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
                        .list(listOf("snippet"))
                        .setMaxResults(MAX_LIMIT_VIDEOS)
                        .setPlaylistId(uploadPlayListId)

                val videos = mutableListOf<Video>()
                var added = MAX_LIMIT_VIDEOS

                /* If all of the fetched videos were added then there might be
                   more in the next page */
                while (added == MAX_LIMIT_VIDEOS) {
                    val response = list.execute()
                    val items = response.items.filter {
                        it.snippet.publishedAt.value >= startingDay
                    }.map {
                        it.toLocalVideo(subscription)
                    }
                    videos.addAll(items)
                    added = items.size.toLong()
                    list.pageToken = response.nextPageToken
                }
                return@withContext videos
            }

    companion object {
        const val SYNC_INTERVAL_HOURS = 3
        private const val MAX_LIMIT_SUBS: Long = 50
        private const val MAX_LIMIT_VIDEOS: Long = 10
        private const val LAST_DAY_NO = 7
        private const val TAG = "SyncUtils"
    }
}