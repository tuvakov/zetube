package com.tuvakov.zetube.android.ui.channeldetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.repository.Repository
import kotlinx.coroutines.launch

class ChannelDetailViewModel(private val repository: Repository) : ViewModel() {

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
                _channel.value = repository.getSubscriptionById(channelId)
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
                val videos = repository.getVideosByChannelId(channelId)
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