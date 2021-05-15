package com.tuvakov.zetube.android.ui.videos

import android.util.Log
import androidx.lifecycle.*
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.repository.Repository
import com.tuvakov.zetube.android.ui.channel.*
import com.tuvakov.zetube.android.utils.SyncUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
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