package com.tuvakov.zetube.android.ui.feed

import android.util.Log
import androidx.lifecycle.*
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.tuvakov.zetube.android.data.SyncStatus
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.repository.SubscriptionRepo
import com.tuvakov.zetube.android.repository.VideoRepo
import com.tuvakov.zetube.android.utils.SyncUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

class MainViewModel internal constructor(
        private val syncUtils: SyncUtils,
        private val mSubscriptionRepo: SubscriptionRepo,
        private val mVideoRepo: VideoRepo
) : ViewModel() {
    private val _status = MutableLiveData<SyncStatus>()
    var isSyncing = false
    val videoFeed: LiveData<List<Video>>
        get() = mVideoRepo.videos
    val status: LiveData<SyncStatus>
        get() = _status

    fun deleteAllVideos() {
        viewModelScope.launch { mVideoRepo.deleteAll() }
    }

    fun deleteAllSubscriptions() {
        viewModelScope.launch { mSubscriptionRepo.deleteAll() }
    }

    fun getVideoById(videoId: String): Video {
        // TODO: Fix this later
        return runBlocking { mVideoRepo.getVideoById(videoId) }
    }

    fun sync() {
        viewModelScope.launch {
            try {
                isSyncing = true
                _status.value = SyncStatus(SyncUtils.STATUS_SYNC_STARTED)
                syncUtils.sync()
                _status.value = SyncStatus(SyncUtils.STATUS_SYNC_SUCCESS)
            } catch (e: GooglePlayServicesAvailabilityIOException) {
                Log.d(TAG, "onHandleIntent: GooglePlayServicesAvailabilityIOException")
                _status.value = SyncStatus(SyncUtils.STATUS_SYNC_GOOGLE_PLAY_FAILURE, e)
            } catch (e: UserRecoverableAuthIOException) {
                Log.d(TAG, "onHandleIntent: UserRecoverableAuthIOException")
                _status.value = SyncStatus(SyncUtils.STATUS_SYNC_AUTH_FAILURE, e)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "onHandleIntent: Exception " + e.message)
                if (e.message == "immature-sync") {
                    _status.value = SyncStatus(SyncUtils.STATUS_IMMATURE_SYNC)
                } else {
                    _status.value = SyncStatus(SyncUtils.STATUS_SYNC_FAILURE)
                }
            }
            finally {
                isSyncing = false
            }
        }
    }

    fun setStatusIdle() {
       _status.value = SyncStatus(SyncUtils.STATUS_SYNC_IDLE)
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}

@Singleton
class MainViewModelFactory @Inject internal constructor(
        private val subscriptionRepo: SubscriptionRepo,
        private val videoRepo: VideoRepo,
        private val syncUtils: SyncUtils
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(syncUtils, subscriptionRepo, videoRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}