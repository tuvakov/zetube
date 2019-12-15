package com.tuvakov.zeyoube.android.dagger;

import android.app.Application;

import androidx.room.Room;

import com.tuvakov.zeyoube.android.data.SubscriptionDao;
import com.tuvakov.zeyoube.android.data.VideoDao;
import com.tuvakov.zeyoube.android.data.ZeYouBeDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
abstract class RoomModule {
    @Provides @Singleton
    static ZeYouBeDatabase provideRoomDb(Application application) {
        return Room.databaseBuilder(application, ZeYouBeDatabase.class, "zeyoube_db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides @Singleton
    static SubscriptionDao provideSubscriptionDao(ZeYouBeDatabase zeYouBeDb) {
        return zeYouBeDb.getSubscriptionDao();
    }

    @Provides @Singleton
    static VideoDao provideVideoDao(ZeYouBeDatabase zeYouBeDb) {
        return zeYouBeDb.getVideoDao();
    }
}
