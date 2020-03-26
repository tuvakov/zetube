package com.tuvakov.zetube.android.ui.channels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.repository.SubscriptionRepo
import javax.inject.Inject
import javax.inject.Singleton

class ChannelsViewModel(private val subscriptionRepo: SubscriptionRepo) : ViewModel() {
    fun getChannels(): LiveData<List<Subscription>> = subscriptionRepo.allSubscriptions
}

@Singleton
class ChannelsViewModelFactory @Inject constructor(private val repository: SubscriptionRepo) :
        ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChannelsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChannelsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}