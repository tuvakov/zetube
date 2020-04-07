package com.tuvakov.zetube.android.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Subscription::class, Video::class], version = ZeTubeDatabase.VERSION)
abstract class ZeTubeDatabase : RoomDatabase() {
    abstract val subscriptionDao: SubscriptionDao
    abstract val videoDao: VideoDao

    companion object {
        const val VERSION = 1
    }
}