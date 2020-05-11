package com.tuvakov.zetube.android.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY published_at DESC")
    fun getAllVideos(): LiveData<List<Video>>

    @Query("SELECT * FROM videos WHERE is_saved = 1 ORDER BY published_at DESC")
    fun getSavedVideos(): LiveData<List<Video>>

    @Query("SELECT * FROM videos WHERE channel_id = :channelId ORDER BY published_at DESC")
    suspend fun getVideosByChannelId(channelId: String): List<Video>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoById(id: String): Video

    @Insert
    suspend fun insert(video: Video)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun bulkInsert(videos: List<Video>)

    @Update
    suspend fun update(video: Video)

    @Delete
    suspend fun delete(video: Video)

    @Query("DELETE FROM videos WHERE is_saved = 0")
    suspend fun deleteUnsavedVideos()

    @Query("DELETE FROM videos")
    suspend fun deleteAll()
}