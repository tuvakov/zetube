package com.tuvakov.zetube.android.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SubscriptionDao {

    @get: Query("SELECT * FROM subscriptions ORDER BY title ASC")
    val subscriptions: LiveData<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE id = :channelId")
    suspend fun getSubscriptionById(channelId: String): Subscription

    @Insert
    suspend fun insert(subscription: Subscription)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun bulkInsert(subscriptions: List<Subscription>)

    @Update
    suspend fun update(subscription: Subscription)

    @Delete
    suspend fun delete(subscription: Subscription)

    @Query("DELETE FROM subscriptions")
    suspend fun deleteAll()
}