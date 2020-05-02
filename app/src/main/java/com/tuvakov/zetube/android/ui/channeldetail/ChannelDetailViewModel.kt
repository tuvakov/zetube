package com.tuvakov.zetube.android.ui.channeldetail

import androidx.lifecycle.*
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.repository.SubscriptionRepo
import com.tuvakov.zetube.android.repository.VideoRepo
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

class ChannelDetailViewModel(
        private val subscriptionRepo: SubscriptionRepo,
        private val videoRepo: VideoRepo
) : ViewModel() {

    private val _channel = MutableLiveData<Subscription>()
    private val _channelVideos = MutableLiveData<List<Video>>()
    private val _channelState = MutableLiveData<LiveDataState>(InProgress)
    private val _videosState = MutableLiveData<LiveDataState>(InProgress)

    val channel: LiveData<Subscription>
        get() = _channel

    val channelVideos: LiveData<List<Video>>
        get() = _channelVideos

    val channelState: LiveData<LiveDataState>
        get() = _channelState

    val videosState: LiveData<LiveDataState>
        get() = _videosState

    fun fetchSubscription(channelId: String) {
        try {
            viewModelScope.launch {
                _channel.value = subscriptionRepo.getSubscriptionById(channelId)
                _channelState.value = Success
            }
        } catch (error: Exception) {
            _channelState.value = Error(error)
        }
    }

    fun fetchVideosForSubscription(channelId: String) {
        try {
            _videosState.value = InProgress
            viewModelScope.launch {
                val videos = videoRepo.getVideosByChannelId(channelId)
                if (videos.isEmpty()) {
                    _videosState.value = EmptyList
                } else {
                    _channelVideos.value = videos
                    _videosState.value = Success
                }
            }
        } catch (exception: Exception) {
            _videosState.value = Error(exception)
        }
    }

    fun setErrorOnAllStates() {
        val error = Error()
        _channelState.value = error
        _videosState.value = error
    }
}

@Singleton
class ChannelDetailViewModelFactory @Inject constructor(
        private val subscriptionRepo: SubscriptionRepo,
        private val videoRepo: VideoRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChannelDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChannelDetailViewModel(subscriptionRepo, videoRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}