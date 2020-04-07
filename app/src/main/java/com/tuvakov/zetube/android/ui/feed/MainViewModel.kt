package com.tuvakov.zetube.android.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.repository.SubscriptionRepo
import com.tuvakov.zetube.android.repository.VideoRepo
import javax.inject.Inject
import javax.inject.Singleton

class MainViewModel internal constructor(
        private val mSubscriptionRepo: SubscriptionRepo,
        private val mVideoRepo: VideoRepo
) : ViewModel() {
    var isSyncing = false
    private var cachedVideo: Video? = null
    val videoFeed: LiveData<List<Video>>
        get() = mVideoRepo.allVideos

    fun deleteAllVideos() {
        mVideoRepo.deleteAll()
    }

    fun deleteAllSubscriptions() {
        mSubscriptionRepo.deleteAll()
    }

    fun getVideoById(getVideoId: String): Video? {
        if (cachedVideo != null && cachedVideo!!.id == getVideoId) {
            return cachedVideo
        }
        cachedVideo = mVideoRepo.getVideoById(getVideoId)
        return cachedVideo
    }
}

@Singleton
class MainViewModelFactory @Inject internal constructor(
        private val mSubscriptionRepo: SubscriptionRepo,
        private val mVideoRepo: VideoRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(mSubscriptionRepo, mVideoRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}