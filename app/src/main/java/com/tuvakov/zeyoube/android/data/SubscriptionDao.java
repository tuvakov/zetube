package com.tuvakov.zeyoube.android.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions")
    LiveData<List<Subscription>> selectAll();

    @Insert
    void insert(Subscription subscription);

    @Update
    void update(Subscription subscription);

    @Delete
    void delete(Subscription subscription);

    @Query("DELETE FROM subscriptions")
    void deleteAll();

}
