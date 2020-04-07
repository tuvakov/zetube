package com.tuvakov.zetube.android.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

@Entity(tableName = "videos")
data class Video(
        @PrimaryKey @ColumnInfo(name = "id") val id: String,
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "thumbnail") val thumbnail: String,
        @ColumnInfo(name = "description") val description: String,
        @ColumnInfo(name = "channel_id") val channelId: String,
        @ColumnInfo(name = "channel_title") val channelTitle: String,
        @ColumnInfo(name = "channel_avatar") val channelAvatar: String,
        @ColumnInfo(name = "is_saved") val isSaved: Boolean = false,
        @ColumnInfo(name = "is_hidden") val isHidden: Boolean = false,
        @ColumnInfo(name = "published_at") val publishedAt: Long = 0
) {
    fun getLocalDateTimePublishedAt(): ZonedDateTime {
        return Instant.ofEpochMilli(publishedAt).atZone(ZoneId.systemDefault())
    }
}

@Entity(tableName = "subscriptions")
class Subscription(
        @PrimaryKey val id: String,
        val title: String,
        val description: String,
        val thumbnail: String
)