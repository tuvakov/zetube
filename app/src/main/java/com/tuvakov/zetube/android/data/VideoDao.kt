package com.tuvakov.zetube.android.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY published_at DESC")
    fun selectAllVideos(): LiveData<List<Video>>

    @Query("SELECT * FROM videos WHERE id = :id")
    fun getVideoById(id: String?): Video?

    @Insert
    fun insert(video: Video)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun bulkInsert(videos: List<Video>)

    @Update
    fun update(video: Video)

    @Delete
    fun delete(video: Video)

    @Query("DELETE FROM videos")
    fun deleteAll()
}