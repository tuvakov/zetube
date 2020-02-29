package com.tuvakov.zeyoube.android.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface VideoDao {

    @Query("SELECT * FROM videos ORDER BY published_at DESC")
    LiveData<List<Video>> selectAllVideos();

    @Query("SELECT * FROM videos WHERE id = :id")
    Video getVideoById(int id);

    @Insert
    void insert(Video video);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(List<Video> videos);

    @Update
    void update(Video video);

    @Delete
    void delete(Video video);

    @Query("DELETE FROM videos")
    void deleteAll();
}
