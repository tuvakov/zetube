package com.tuvakov.zeyoube.android.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.util.Objects;

@Entity(tableName = "videos")
public class Video {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String thumbnail;
    private String description;
    @ColumnInfo(name = "channel_title")
    private String channelTitle;
    @ColumnInfo(name = "channel_avatar")
    private String channelAvatar;
    @ColumnInfo(name = "video_id")
    private String videoId;
    @ColumnInfo(name = "is_seen")
    private boolean isSeen;
    @ColumnInfo(name = "published_at")
    private long publishedAt;

    @Ignore
    public Video() {}

    public Video(String title, String thumbnail, String description,
                 String channelTitle, String channelAvatar, String videoId,
                 boolean isSeen, long publishedAt) {

        this.title = title;
        this.thumbnail = thumbnail;
        this.description = description;
        this.channelTitle = channelTitle;
        this.channelAvatar = channelAvatar;
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

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public String getChannelAvatar() {
        return channelAvatar;
    }

    public void setChannelAvatar(String channelAvatar) {
        this.channelAvatar = channelAvatar;
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

    public long getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(long publishedAt) {
        this.publishedAt = publishedAt;
    }

    public ZonedDateTime getLocalDateTimePublishedAt() {
        return Instant.ofEpochMilli(publishedAt).atZone(ZoneId.systemDefault());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return getId() == video.getId() && getVideoId().equals(video.getVideoId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getVideoId());
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
