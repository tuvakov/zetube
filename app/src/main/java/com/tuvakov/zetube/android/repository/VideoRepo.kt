package com.tuvakov.zetube.android.repository

import androidx.lifecycle.LiveData
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.data.VideoDao
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepo @Inject constructor(
        private val mVideoDao: VideoDao,
        private val mDiskIO: ExecutorService
) {
    val allVideos: LiveData<List<Video>> = mVideoDao.selectAllVideos()

    fun insert(video: Video) {
        mDiskIO.execute { mVideoDao.insert(video) }
    }

    fun bulkInsertForService(videos: List<Video>) {
        mVideoDao.bulkInsert(videos)
    }

    fun update(video: Video) {
        mDiskIO.execute { mVideoDao.update(video) }
    }

    fun delete(video: Video) {
        mDiskIO.execute { mVideoDao.delete(video) }
    }

    fun deleteAll() {
        mDiskIO.execute { mVideoDao.deleteAll() }
    }

    fun deleteAllForService() {
        mVideoDao.deleteAll()
    }

    fun getVideoById(videoId: String): Video? {
        val video: Future<Video?> = mDiskIO.submit(Callable { mVideoDao.getVideoById(videoId) })
        return try {
            video.get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        }
    }
}