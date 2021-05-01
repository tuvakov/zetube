package com.tuvakov.zetube.android.hilt

import android.content.Context
import androidx.room.Room
import com.tuvakov.zetube.android.data.SubscriptionDao
import com.tuvakov.zetube.android.data.VideoDao
import com.tuvakov.zetube.android.data.ZeTubeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object RoomModule {

    @Provides
    @Singleton
    fun provideRoomDb(@ApplicationContext application: Context): ZeTubeDatabase {
        return Room.databaseBuilder(application, ZeTubeDatabase::class.java, "zetube_db")
                .fallbackToDestructiveMigration()
                .build()
    }

    @Provides
    @Singleton
    fun provideSubscriptionDao(database: ZeTubeDatabase): SubscriptionDao {
        return database.subscriptionDao
    }

    @Provides
    @Singleton
    fun provideVideoDao(database: ZeTubeDatabase): VideoDao {
        return database.videoDao
    }
}