package com.tuvakov.zeyoube.android.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Objects;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "videos",
        foreignKeys = @ForeignKey(entity = Subscription.class,
                parentColumns = "id", childColumns = "channel_id", onDelete = CASCADE)
)
public class Video {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String thumbnail;
    private String description;
    @ColumnInfo(name = "channel_id")
    private int channelId;
    @ColumnInfo(name = "video_id")
    private String videoId;
    @ColumnInfo(name = "is_seen")
    private boolean isSeen;
    @ColumnInfo(name = "published_at")
    private String publishedAt;
    @Ignore
    private ZonedDateTime localPublishedAt;

    @Ignore
    public Video() {}

    public Video(String title, String thumbnail, String description,
                 int channelId, String videoId, boolean isSeen, String publishedAt) {
        this.title = title;
        this.thumbnail = thumbnail;
        this.description = description;
        this.channelId = channelId;
        this.videoId = videoId;
        this.isSeen = isSeen;
        this.publishedAt = publishedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }


    public ZonedDateTime getLocalDateTimePublishedAt() {
        if (localPublishedAt != null) {
            return localPublishedAt;
        }
        ZonedDateTime utc = ZonedDateTime.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME);
        localPublishedAt = utc.withZoneSameInstant(ZoneId.systemDefault());
        return localPublishedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return getId() == video.getId() &&
                getChannelId() == video.getChannelId() &&
                getVideoId().equals(video.getVideoId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getChannelId(), getVideoId());
    }

    @Override
    public String toString() {
        return "Video{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", videoId='" + videoId + '\'' +
                ", publishedAt=" + publishedAt +
                '}';
    }
}
