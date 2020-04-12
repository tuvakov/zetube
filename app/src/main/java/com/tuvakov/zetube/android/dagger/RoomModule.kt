package com.tuvakov.zetube.android.dagger

import android.app.Application
import androidx.room.Room
import com.tuvakov.zetube.android.data.SubscriptionDao
import com.tuvakov.zetube.android.data.VideoDao
import com.tuvakov.zetube.android.data.ZeTubeDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal object RoomModule {
    @JvmStatic
    @Provides
    @Singleton
    fun provideRoomDb(application: Application): ZeTubeDatabase {
        return Room.databaseBuilder(application, ZeTubeDatabase::class.java, "zetube_db")
                .fallbackToDestructiveMigration()
                .build()
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideSubscriptionDao(database: ZeTubeDatabase): SubscriptionDao {
        return database.subscriptionDao
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideVideoDao(database: ZeTubeDatabase): VideoDao {
        return database.videoDao
    }
}