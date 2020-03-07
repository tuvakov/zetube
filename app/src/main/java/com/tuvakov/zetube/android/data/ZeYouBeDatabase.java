package com.tuvakov.zetube.android.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;


@Database(entities = {Subscription.class, Video.class}, version = ZeYouBeDatabase.VERSION)
public abstract class ZeYouBeDatabase extends RoomDatabase {

    static final int VERSION = 1;

    public abstract SubscriptionDao getSubscriptionDao();
    public abstract VideoDao getVideoDao();
}