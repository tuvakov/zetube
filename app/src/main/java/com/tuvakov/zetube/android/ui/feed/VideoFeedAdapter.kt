package com.tuvakov.zetube.android.ui.feed

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.ui.channeldetail.ChannelDetailActivity
import com.tuvakov.zetube.android.ui.player.PlayerActivity

class VideoFeedAdapter : ListAdapter<Video, VideoViewHolder>(VIDEO_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val currentVideo = getItem(position)
        if (currentVideo != null) {
            holder.bind(currentVideo)
        }
    }

    companion object {
        private const val TAG = "VideoFeedAdapter"
        private val VIDEO_COMPARATOR = object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
                return oldItem.title == newItem.title && oldItem.description == newItem.description
                        && oldItem.publishedAt == newItem.publishedAt
            }
        }
    }
}

class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    /* Views */
    private val imageViewVideoThumbnail: ImageView = itemView.findViewById(R.id.iv_video_thumbnail)
    private val textViewTitle: TextView = itemView.findViewById(R.id.tv_video_title)
    private val textViewInfo: TextView = itemView.findViewById(R.id.tv_video_info)
    private val imageViewChannelAvatar: ImageView = itemView.findViewById(R.id.iv_channel_avatar)
    private val imageViewMore = itemView.findViewById<ImageView>(R.id.iv_more)

    private lateinit var video: Video

    init {

        /* Open PlayerActivity on Video item click */
        itemView.setOnClickListener {
            val intent = Intent(it.context, PlayerActivity::class.java)
            intent.putExtra(PlayerActivity.KEY_EXTRA_VIDEO_ID, video.id)
            it.context.startActivity(intent)
        }

        /* Open ChannelDetailActivity on channel avatar click  */
        imageViewChannelAvatar.setOnClickListener {
            // If already in ChannelDetailActivity then don't relaunch
            if (it.context is ChannelDetailActivity) {
                return@setOnClickListener
            }
            val intent = Intent(it.context, ChannelDetailActivity::class.java)
            intent.putExtra(ChannelDetailActivity.EXTRA_CHANNEL_ID, video.channelId)
            intent.putExtra(ChannelDetailActivity.EXTRA_CHANNEL_TITLE, video.channelTitle)
            it.context.startActivity(intent)
        }

        /* Show pop-up menu on 'more' icon click */
        imageViewMore.setOnClickListener { view: View ->
            val popup = PopupMenu(view.context, view)
            popup.setOnMenuItemClickListener { item: MenuItem ->
                if (item.itemId == R.id.menu_item_share) {
                    val videoLink = PlayerActivity.VIDEO_BASE_LINK + video.id
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, videoLink)
                    intent.resolveActivity(view.context.packageManager)?.let {
                        view.context.startActivity(intent)
                    }
                    return@setOnMenuItemClickListener true
                }
                false
            }
            popup.inflate(R.menu.popup_menu_feed_item)
            popup.show()
        }
    }

    fun bind(video: Video) {
        this.video = video
        Glide.with(imageViewChannelAvatar.context)
                .load(video.channelAvatar)
                .centerCrop()
                .circleCrop()
                .placeholder(ColorDrawable(Color.GRAY))
                .into(imageViewChannelAvatar)

        Glide.with(imageViewVideoThumbnail.context)
                .load(video.thumbnail)
                .centerCrop()
                .placeholder(ColorDrawable(Color.GRAY))
                .into(imageViewVideoThumbnail)

        textViewInfo.text = String.format("%s \u2022 ", video.channelTitle)
        textViewTitle.text = video.title
        textViewInfo.append(DateUtils.getRelativeTimeSpanString(video.publishedAt))
    }

    companion object {
        fun inflate(parent: ViewGroup): VideoViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_video_feed, parent, false)
            return VideoViewHolder(itemView)
        }
    }
}