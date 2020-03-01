package com.tuvakov.zeyoube.android.ui.player;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerUtils;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.tuvakov.zeyoube.android.R;
import com.tuvakov.zeyoube.android.ZeYouBe;
import com.tuvakov.zeyoube.android.data.Video;
import com.tuvakov.zeyoube.android.ui.feed.MainViewModel;
import com.tuvakov.zeyoube.android.ui.feed.MainViewModelFactory;
import com.tuvakov.zeyoube.android.utils.FullScreenHelper;

import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import javax.inject.Inject;

// TODO: Check if device is offline

public class PlayerActivity extends AppCompatActivity {

    public static final String VIDEO_BASE_LINK = "https://www.youtube.com/watch?v=";
    public static final String KEY_EXTRA_VIDEO_ID = "video-id";
    private static final String TAG = "PlayerActivity";

    @Inject
    MainViewModelFactory mMainViewModelFactory;

    /* Views */
    private ImageView mChannelIcon;
    private TextView mVideoTitle;
    private TextView mVideoInfo;
    private TextView mVideoDescription;
    private TextView mErrorMessage;
    private Button mShareButton;
    private YouTubePlayerView mPlayerView;

    private FullScreenHelper mFullScreenHelper = new FullScreenHelper(this);
    private String mVideoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        bindViews();

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(KEY_EXTRA_VIDEO_ID)) {
            Log.d(TAG, "onCreate: Intent is null or no extra.");
            showErrorMessage();
            return;
        }

        int videoDbId = getIntent().getIntExtra(KEY_EXTRA_VIDEO_ID, -1);

        ((ZeYouBe) getApplication()).getAppComponent().injectPlayerActivityFields(this);
        MainViewModel mMainViewModel = new ViewModelProvider(this, mMainViewModelFactory)
                .get(MainViewModel.class);

        Video video = mMainViewModel.getVideoById(videoDbId);

        if (video == null) {
            Log.d(TAG, "onCreate: Video is null");
            showErrorMessage();
            return;
        }

        mVideoId = video.getVideoId();
        fillViews(video);
        initYoutubePlayerView();

        /*
          If activity is created in landscape orientation,
          then force to place to switch landscape mode.
         */
        int orientation = getBaseContext().getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mPlayerView.enterFullScreen();
        }

        mShareButton.setOnClickListener(this::shareBtnClickListener);
    }

    @Override
    public void onBackPressed() {
        if (mPlayerView.isFullScreen()) mPlayerView.exitFullScreen();
        else super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mPlayerView.enterFullScreen();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mPlayerView.exitFullScreen();
        }
    }

    private void initYoutubePlayerView() {
        getLifecycle().addObserver(mPlayerView);
        mPlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                YouTubePlayerUtils.loadOrCueVideo(
                        youTubePlayer,
                        getLifecycle(),
                        mVideoId,
                        0f
                );
                addFullScreenListenerToPlayer();
            }
        });
    }

    private void addFullScreenListenerToPlayer() {
        mPlayerView.addFullScreenListener(new YouTubePlayerFullScreenListener() {
            @Override
            public void onYouTubePlayerEnterFullScreen() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mFullScreenHelper.enterFullScreen();
            }

            @Override
            public void onYouTubePlayerExitFullScreen() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mFullScreenHelper.exitFullScreen();
            }
        });
    }

    private void bindViews() {
        mChannelIcon = findViewById(R.id.iv_channel_avatar);
        mShareButton = findViewById(R.id.btn_share);
        mVideoTitle = findViewById(R.id.tv_video_title);
        mVideoInfo = findViewById(R.id.tv_video_info);
        mVideoDescription = findViewById(R.id.tv_video_description);
        mErrorMessage = findViewById(R.id.tv_error_message);
        mPlayerView = findViewById(R.id.youtube_player_view);
    }

    private void fillViews(Video video) {
        String publishedAt = video.getLocalDateTimePublishedAt()
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        String videoInfo = String.format("%s \u2022 %s", video.getChannelTitle(), publishedAt);

        mVideoTitle.setText(video.getTitle());
        mVideoInfo.setText(videoInfo);
        mVideoDescription.setText(video.getDescription());

        Glide.with(this)
                .load(video.getChannelAvatar())
                .centerCrop()
                .circleCrop()
                .placeholder(new ColorDrawable(Color.GRAY))
                .into(mChannelIcon);
    }

    private void showErrorMessage() {
        hideViews();
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    private void hideViews() {
        mChannelIcon.setVisibility(View.GONE);
        mVideoTitle.setVisibility(View.GONE);
        mVideoInfo.setVisibility(View.GONE);
        mVideoDescription.setVisibility(View.GONE);
        mPlayerView.setVisibility(View.GONE);
    }

    private void shareBtnClickListener(View view) {
        String videoLink = VIDEO_BASE_LINK + mVideoId;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, videoLink);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
