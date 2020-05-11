package com.tuvakov.zetube.android.ui.channels

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.ui.channeldetail.ChannelDetailActivity

class ChannelsAdapter : ListAdapter<Subscription, RecyclerView.ViewHolder>(SUBSCRIPTION_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ChannelViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val subscription = getItem(position)
        if (subscription != null) {
            (holder as ChannelViewHolder).bind(subscription)
        }
    }

    companion object {
        private val SUBSCRIPTION_COMPARATOR = object : DiffUtil.ItemCallback<Subscription>() {
            override fun areItemsTheSame(oldItem: Subscription, newItem: Subscription): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Subscription, newItem: Subscription): Boolean =
                    oldItem.id == newItem.id && oldItem.thumbnail == newItem.thumbnail
        }
    }
}

class ChannelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val avatar: ImageView = view.findViewById(R.id.iv_channel_avatar)
    private val title: TextView = view.findViewById(R.id.tv_channel_title)
    private var subscription: Subscription? = null

    init {
        view.setOnClickListener {
            val intent = Intent(view.context, ChannelDetailActivity::class.java)
            intent.putExtra(ChannelDetailActivity.EXTRA_CHANNEL_ID, subscription?.id)
            intent.putExtra(ChannelDetailActivity.EXTRA_CHANNEL_TITLE, subscription?.title)
            view.context.startActivity(intent)
        }
    }

    fun bind(subscription: Subscription) {
        this.subscription = subscription
        title.text = subscription.title

        Glide.with(avatar.context)
                .load(subscription.thumbnail)
                .placeholder(ColorDrawable(Color.GRAY))
                .centerCrop()
                .circleCrop()
                .into(avatar)
    }

    companion object {
        fun create(parent: ViewGroup): ChannelViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_view_channel, parent, false
            )
            return ChannelViewHolder(view)
        }
    }
}