package com.tuvakov.zeyoube.android.data;

import android.app.Application;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {Subscription.class, Video.class}, version = 1)
public abstract class ZeYouBeDatabase extends RoomDatabase {

    private static ZeYouBeDatabase sInstance;

    public static synchronized ZeYouBeDatabase getInstance(Application application) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(application,
                    ZeYouBeDatabase.class, "zeyoube_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return sInstance;
    }

    public abstract SubscriptionDao getSubscriptionDao();
    public abstract VideoDao getVideoDao();
}