package com.tuvakov.zetube.android.repository

import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.data.VideoDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepo @Inject constructor(private val mVideoDao: VideoDao) {
    val videos = mVideoDao.videos

    suspend fun getVideosByChannelId(channelId: String) = mVideoDao.getVideosByChannelId(channelId)

    suspend fun insert(video: Video) {
        mVideoDao.insert(video)
    }

    suspend fun bulkInsert(videos: List<Video>) {
        mVideoDao.bulkInsert(videos)
    }

    suspend fun update(video: Video) {
        mVideoDao.update(video)
    }

    suspend fun delete(video: Video) {
        mVideoDao.delete(video)
    }

    suspend fun deleteAll() {
        mVideoDao.deleteAll()
    }

    suspend fun getVideoById(videoId: String): Video = mVideoDao.getVideoById(videoId)
}