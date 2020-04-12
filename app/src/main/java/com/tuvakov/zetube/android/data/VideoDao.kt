package com.tuvakov.zetube.android.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VideoDao {
    @get:Query("SELECT * FROM videos ORDER BY published_at DESC")
    val videos: LiveData<List<Video>>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoById(id: String): Video

    @Insert
    suspend fun insert(video: Video)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun bulkInsert(videos: List<Video>)

    @Update
    suspend fun update(video: Video)

    @Delete
    suspend fun delete(video: Video)

    @Query("DELETE FROM videos")
    suspend fun deleteAll()
}