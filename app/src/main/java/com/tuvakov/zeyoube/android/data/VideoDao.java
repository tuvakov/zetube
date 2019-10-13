package com.tuvakov.zeyoube.android.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface VideoDao {

    @Query("SELECT * FROM videos")
    LiveData<List<Video>> selectAllVideos();

    @Insert
    void insert(Video video);

    @Update
    void update(Video video);

    @Delete
    void delete(Video video);

    @Query("DELETE FROM videos")
    void deleteAll();
}
