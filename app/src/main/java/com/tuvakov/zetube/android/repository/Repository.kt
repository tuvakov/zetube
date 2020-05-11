package com.tuvakov.zetube.android.repository

import androidx.lifecycle.LiveData
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.data.SubscriptionDao
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.data.VideoDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
        private val mVideoDao: VideoDao,
        private val mSubscriptionDao: SubscriptionDao
) {

    val subscriptions: LiveData<List<Subscription>> = mSubscriptionDao.subscriptions

    fun getAllVideos(): LiveData<List<Video>> = mVideoDao.getAllVideos()

    fun getSavedVideos(): LiveData<List<Video>> = mVideoDao.getSavedVideos()

    suspend fun getVideosByChannelId(channelId: String) = mVideoDao.getVideosByChannelId(channelId)

    suspend fun getVideoById(videoId: String): Video = mVideoDao.getVideoById(videoId)

    suspend fun getSubscriptionById(id: String) = mSubscriptionDao.getSubscriptionById(id)

    suspend fun updateVideo(video: Video) = mVideoDao.update(video)

    suspend fun insertDataAfterSync(subscriptions: List<Subscription>, videos: List<Video>) {
        mSubscriptionDao.deleteAll()
        mVideoDao.deleteUnsavedVideos()
        mSubscriptionDao.bulkInsert(subscriptions)
        mVideoDao.bulkInsert(videos)
    }

    suspend fun emptyDatabase() {
        mVideoDao.deleteAll()
        mSubscriptionDao.deleteAll()
    }
}