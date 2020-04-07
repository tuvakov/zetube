package com.tuvakov.zetube.android.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions")
    fun selectAll(): LiveData<List<Subscription>>

    @Insert
    fun insert(subscription: Subscription)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun bulkInsert(subscriptions: List<Subscription>)

    @Update
    fun update(subscription: Subscription)

    @Delete
    fun delete(subscription: Subscription)

    @Query("DELETE FROM subscriptions")
    fun deleteAll()
}