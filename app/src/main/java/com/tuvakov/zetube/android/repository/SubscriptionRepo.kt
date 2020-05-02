package com.tuvakov.zetube.android.repository

import androidx.lifecycle.LiveData
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.data.SubscriptionDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepo @Inject constructor(private val mSubscriptionDao: SubscriptionDao) {

    val subscriptions: LiveData<List<Subscription>> = mSubscriptionDao.subscriptions

    suspend fun getSubscriptionById(id: String) = mSubscriptionDao.getSubscriptionById(id)

    suspend fun insert(subscription: Subscription) {
        mSubscriptionDao.insert(subscription)
    }

    suspend fun bulkInsert(subscriptions: List<Subscription>) {
        mSubscriptionDao.bulkInsert(subscriptions)
    }

    suspend fun update(subscription: Subscription) {
        mSubscriptionDao.update(subscription)
    }

    suspend fun delete(subscription: Subscription) {
        mSubscriptionDao.delete(subscription)
    }

    suspend fun deleteAll() {
        mSubscriptionDao.deleteAll()
    }
}