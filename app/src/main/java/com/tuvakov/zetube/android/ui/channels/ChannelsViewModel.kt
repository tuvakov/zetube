package com.tuvakov.zetube.android.ui.channels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.repository.Repository

class ChannelsViewModel(private val repo: Repository) : ViewModel() {
    fun getChannels(): LiveData<List<Subscription>> = repo.subscriptions
}