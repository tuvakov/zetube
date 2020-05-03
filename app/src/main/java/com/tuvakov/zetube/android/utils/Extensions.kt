package com.tuvakov.zetube.android.utils

import android.view.View
import com.google.api.services.youtube.model.PlaylistItem
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.data.Video

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

/* YouTube API client extension functions. */
fun PlaylistItem.toLocalVideo(subscription: Subscription): Video {
    return Video(id = snippet.resourceId.videoId,
            title = snippet.title,
            thumbnail = snippet.thumbnails.high.url,
            description = snippet.description,
            channelId = subscription.id,
            channelTitle = subscription.title,
            channelAvatar = subscription.thumbnail,
            publishedAt = snippet.publishedAt.value
    )
}

fun com.google.api.services.youtube.model.Subscription.toLocalSubscription(): Subscription {
    return Subscription(
            id = snippet.resourceId.channelId,
            title = snippet.title,
            description = snippet.description,
            thumbnail = snippet.thumbnails.medium.url
    )
}