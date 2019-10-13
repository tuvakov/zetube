package com.tuvakov.zeyoube.android;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tuvakov.zeyoube.android.data.Subscription;
import com.tuvakov.zeyoube.android.data.Video;

import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.util.List;

public class VideoFeedAdapter extends ListAdapter<Video, VideoFeedAdapter.VideoViewHolder> {

    private ItemClickListener mItemClickListener;
    private SparseArray<Subscription> mChannelList;

    public VideoFeedAdapter() {
        super(new DiffUtil.ItemCallback<Video>() {
            @Override
            public boolean areItemsTheSame(@NonNull Video oldItem, @NonNull Video newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Video oldItem, @NonNull Video newItem) {
                return oldItem.getTitle().equals(newItem.getTitle())
                        && oldItem.getDescription().equals(newItem.getDescription())
                        && oldItem.getVideoId().equals(newItem.getVideoId());
            }
        });

        mChannelList = new SparseArray<>();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video_feed, parent, false);
        return new VideoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video currentVideo = getItem(position);
        Subscription channel = mChannelList.get(currentVideo.getChannelId());

        if (channel != null) {
            holder.textViewInfo.setText(String.format("%s \u2022 ", channel.getTitle()));
            Glide.with(holder.imageViewChannelAvatar.getContext())
                    .load(channel.getThumbnail())
                    .centerCrop()
                    .placeholder(new ColorDrawable(Color.GRAY))
                    .into(holder.imageViewChannelAvatar);
        }

        Glide.with(holder.imageViewVideoThumbnail.getContext())
                .load(currentVideo.getThumbnail())
                .centerCrop()
                .placeholder(new ColorDrawable(Color.GRAY))
                .into(holder.imageViewVideoThumbnail);

        holder.textViewTitle.setText(currentVideo.getTitle());
        String publishedAt = currentVideo.getLocalDateTimePublishedAt()
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        holder.textViewInfo.append(publishedAt);
    }


    public Video getVideoAt(int i) {
        return getItem(i);
    }


    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }


    void setChannels(List<Subscription> channels) {

        if (channels == null) return;

        mChannelList.clear();
        for (Subscription channel : channels) {
            mChannelList.append(channel.getId(), channel);
        }

        notifyDataSetChanged();
    }


    class VideoViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewVideoThumbnail;
        private TextView textViewTitle;
        private TextView textViewInfo;
        private ImageView imageViewChannelAvatar;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewVideoThumbnail = itemView.findViewById(R.id.iv_video_thumbnail);
            textViewTitle = itemView.findViewById(R.id.tv_video_title);
            textViewInfo = itemView.findViewById(R.id.tv_video_info);
            imageViewChannelAvatar = itemView.findViewById(R.id.iv_channel_avatar);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (mItemClickListener != null && position != RecyclerView.NO_POSITION) {
                    mItemClickListener.onClick(getItem(position));
                }
            });

        }

    }

    public interface ItemClickListener {
        void onClick(Video note);
    }
}
