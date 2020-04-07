package com.tuvakov.zetube.android.repository

import androidx.lifecycle.LiveData
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.data.SubscriptionDao
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepo @Inject constructor(
        private val mSubscriptionDao: SubscriptionDao,
        private val mDiskIO: ExecutorService
) {

    val allSubscriptions: LiveData<List<Subscription>> = mSubscriptionDao.selectAll()

    fun insert(subscription: Subscription) {
        mDiskIO.execute { mSubscriptionDao.insert(subscription) }
    }

    fun bulkInsertForService(subscriptions: List<Subscription>) {
        mSubscriptionDao.bulkInsert(subscriptions)
    }

    fun update(subscription: Subscription) {
        mDiskIO.execute { mSubscriptionDao.update(subscription) }
    }

    fun delete(subscription: Subscription) {
        mDiskIO.execute { mSubscriptionDao.delete(subscription) }
    }

    fun deleteAll() {
        mDiskIO.execute { mSubscriptionDao.deleteAll() }
    }

    fun deleteAllForService() {
        mSubscriptionDao.deleteAll()
    }
}