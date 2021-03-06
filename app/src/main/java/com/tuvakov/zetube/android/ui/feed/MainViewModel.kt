package com.tuvakov.zetube.android.ui.feed

import android.util.Log
import androidx.lifecycle.*
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.repository.Repository
import com.tuvakov.zetube.android.ui.channeldetail.*
import com.tuvakov.zetube.android.ui.channels.ChannelsViewModel
import com.tuvakov.zetube.android.utils.SyncUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

class MainViewModel internal constructor(
        private val syncUtils: SyncUtils,
        private val repository: Repository
) : ViewModel() {
    private val isVideoSourceAll = MutableLiveData(true)
    private val _status = MutableLiveData<LiveDataState>()

    val isSyncing
        get() = _status.value == InProgress

    val isSuccess
        get() = _status.value == Success

    val status: LiveData<LiveDataState>
        get() = _status

    val videoFeed: LiveData<List<Video>> = isVideoSourceAll.switchMap {
        liveData {
            _status.value = InProgress
            val data = if (it) repository.getAllVideos() else repository.getSavedVideos()
            _status.value = Success
            emitSource(data)
        }
    }

    fun loadSavedVideos() {
        isVideoSourceAll.value = false
    }

    fun loadAllVideos() {
        isVideoSourceAll.value = true
    }

    fun setEmptyListStatus() {
        _status.value = EmptyList
    }

    fun getVideoById(videoId: String): Video {
        // TODO: Fix this later
        return runBlocking { repository.getVideoById(videoId) }
    }

    fun sync() {
        viewModelScope.launch {
            _status.value = InProgress
            try {
                syncUtils.sync()
                _status.value = Success
            } catch (e: Exception) {
                Log.e(TAG, "Exception", e)
                _status.value = Error(e)
            }
        }
    }

    fun updateVideo(video: Video) = viewModelScope.launch { repository.updateVideo(video) }

    fun emptyDatabase() = viewModelScope.launch { repository.emptyDatabase() }

    companion object {
        private const val TAG = "MainViewModel"
    }
}

@Singleton
class ViewModelFactory @Inject internal constructor(
        private val repository: Repository,
        private val syncUtils: SyncUtils
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(syncUtils, repository) as T
            }
            modelClass.isAssignableFrom(ChannelsViewModel::class.java) -> {
                ChannelsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ChannelDetailViewModel::class.java) -> {
                ChannelDetailViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}