package com.tuvakov.zetube.android.ui.feed;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tuvakov.zetube.android.R;
import com.tuvakov.zetube.android.data.Video;

import static com.tuvakov.zetube.android.ui.player.PlayerActivity.VIDEO_BASE_LINK;

public class VideoFeedAdapter extends ListAdapter<Video, VideoFeedAdapter.VideoViewHolder> {

    private static final String TAG = "VideoFeedAdapter";
    private ItemClickListener mItemClickListener;

    public VideoFeedAdapter() {
        super(new DiffUtil.ItemCallback<Video>() {
            @Override
            public boolean areItemsTheSame(@NonNull Video oldItem, @NonNull Video newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Video oldItem, @NonNull Video newItem) {
                return oldItem.getTitle().equals(newItem.getTitle())
                        && oldItem.getDescription().equals(newItem.getDescription())
                        && oldItem.getPublishedAt() == newItem.getPublishedAt();
            }
        });
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

        Glide.with(holder.imageViewChannelAvatar.getContext())
                .load(currentVideo.getChannelAvatar())
                .centerCrop()
                .circleCrop()
                .placeholder(new ColorDrawable(Color.GRAY))
                .into(holder.imageViewChannelAvatar);

        Glide.with(holder.imageViewVideoThumbnail.getContext())
                .load(currentVideo.getThumbnail())
                .centerCrop()
                .placeholder(new ColorDrawable(Color.GRAY))
                .into(holder.imageViewVideoThumbnail);

        holder.textViewInfo.setText(String.format("%s \u2022 ", currentVideo.getChannelTitle()));
        holder.textViewTitle.setText(currentVideo.getTitle());
        holder.textViewInfo.append(DateUtils.getRelativeTimeSpanString(currentVideo.getPublishedAt()));
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
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
            ImageView imageViewMore = itemView.findViewById(R.id.iv_more);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (mItemClickListener != null) {
                    mItemClickListener.onClick(getItem(position));
                }
            });

            imageViewMore.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.menu_item_share) {
                        String videoId = getItem(getAdapterPosition()).getId();
                        String videoLink = VIDEO_BASE_LINK + videoId;
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, videoLink);
                        if (intent.resolveActivity(view.getContext().getPackageManager()) != null) {
                            view.getContext().startActivity(intent);
                        }
                        return true;
                    }
                    return false;
                });
                popup.inflate(R.menu.popup_menu_feed_item);
                popup.show();
            });
        }
    }

    public interface ItemClickListener {
        void onClick(Video video);
    }
}
