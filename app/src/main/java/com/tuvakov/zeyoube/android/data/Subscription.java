package com.tuvakov.zeyoube.android.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "subscriptions")
public class Subscription {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "channel_id")
    private String channelId;
    private String title;
    private String description;
    private String thumbnail;
    @ColumnInfo(name = "etag")
    private String tag;

    @Ignore
    public Subscription() { }

    public Subscription(String channelId, String title, String description,
                        String thumbnail, String tag) {
        this.channelId = channelId;
        this.title = title;
        this.description = description;
        this.thumbnail = thumbnail;
        this.tag = tag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String eTag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return getId() == that.getId() &&
                getChannelId().equals(that.getChannelId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getChannelId());
    }

    @Override
    public String toString() {
        return "Subscription{" + "id=" + id +
                ", channelId='" + channelId + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
