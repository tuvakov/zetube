package com.tuvakov.zetube.android.ui.channels

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.databinding.ItemViewChannelBinding
import com.tuvakov.zetube.android.ui.channel.ChannelDetailActivity

class ChannelsAdapter : ListAdapter<Subscription, RecyclerView.ViewHolder>(SUBSCRIPTION_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemViewChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChannelViewHolder(binding)
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

class ChannelViewHolder(
        private val vBinding: ItemViewChannelBinding
) : RecyclerView.ViewHolder(vBinding.root) {

    private var subscription: Subscription? = null

    init {
        vBinding.root.setOnClickListener {
            val intent = Intent(it.context, ChannelDetailActivity::class.java)
            intent.putExtra(ChannelDetailActivity.EXTRA_CHANNEL_ID, subscription?.id)
            intent.putExtra(ChannelDetailActivity.EXTRA_CHANNEL_TITLE, subscription?.title)
            it.context.startActivity(intent)
        }
    }

    fun bind(subscription: Subscription) {
        this.subscription = subscription
        vBinding.tvChannelTitle.text = subscription.title

        Glide.with(vBinding.root.context)
                .load(subscription.thumbnail)
                .placeholder(ColorDrawable(Color.GRAY))
                .centerCrop()
                .circleCrop()
                .into(vBinding.ivChannelAvatar)
    }
}