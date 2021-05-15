package com.tuvakov.zetube.android.ui.videos

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.databinding.ItemVideoFeedBinding
import com.tuvakov.zetube.android.ui.channel.ChannelDetailActivity
import com.tuvakov.zetube.android.ui.player.PlayerActivity

class VideoFeedAdapter : ListAdapter<Video, VideoViewHolder>(VIDEO_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoFeedBinding.inflate(LayoutInflater.from(parent.context))
        return VideoViewHolder(binding)
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

class VideoViewHolder(
        private val vBinding: ItemVideoFeedBinding
) : RecyclerView.ViewHolder(vBinding.root) {

    private lateinit var video: Video

    init {

        /* Open PlayerActivity on Video item click */
        itemView.setOnClickListener {
            val intent = Intent(it.context, PlayerActivity::class.java)
            intent.putExtra(PlayerActivity.KEY_EXTRA_VIDEO_ID, video.id)
            it.context.startActivity(intent)
        }

        /* Open ChannelDetailActivity on channel avatar click  */
        vBinding.ivChannelAvatar.setOnClickListener {
            // If already in ChannelDetailActivity then don't relaunch
            if (it.context !is VideoFeedActivity) {
                return@setOnClickListener
            }
            val intent = Intent(it.context, ChannelDetailActivity::class.java)
            intent.putExtra(ChannelDetailActivity.EXTRA_CHANNEL_ID, video.channelId)
            intent.putExtra(ChannelDetailActivity.EXTRA_CHANNEL_TITLE, video.channelTitle)
            it.context.startActivity(intent)
        }

        /* Show pop-up menu on 'more' icon click */
        vBinding.ivMore.setOnClickListener { view: View ->
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
        Glide.with(vBinding.root.context)
                .load(video.channelAvatar)
                .centerCrop()
                .circleCrop()
                .placeholder(ColorDrawable(Color.GRAY))
                .into(vBinding.ivChannelAvatar)

        Glide.with(vBinding.root.context)
                .load(video.thumbnail)
                .centerCrop()
                .placeholder(ColorDrawable(Color.GRAY))
                .into(vBinding.ivVideoThumbnail)

        vBinding.tvVideoInfo.text = String.format("%s \u2022 ", video.channelTitle)
        vBinding.tvVideoTitle.text = video.title
        vBinding.tvVideoInfo.append(DateUtils.getRelativeTimeSpanString(video.publishedAt))
    }
}