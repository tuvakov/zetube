package com.tuvakov.zetube.android.ui.player

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.ZeTubeApp
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.ui.feed.MainViewModel
import com.tuvakov.zetube.android.ui.feed.MainViewModelFactory
import com.tuvakov.zetube.android.utils.FullScreenHelper
import com.tuvakov.zetube.android.utils.hide
import com.tuvakov.zetube.android.utils.show
import kotlinx.android.synthetic.main.activity_player.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import javax.inject.Inject

// TODO: Check if device is offline
class PlayerActivity : AppCompatActivity() {

    @Inject
    lateinit var mMainViewModelFactory: MainViewModelFactory
    private lateinit var mVideoId: String
    private val mFullScreenHelper = FullScreenHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        /* Check for intent and video id extra */
        if (intent == null || !intent.hasExtra(KEY_EXTRA_VIDEO_ID)) {
            Log.d(TAG, "onCreate: Intent is null or no extra.")
            showErrorMessage()
            return
        }

        // Get video id extra
        mVideoId = intent.getStringExtra(KEY_EXTRA_VIDEO_ID) ?: ""

        // Inject dependencies
        (application as ZeTubeApp).appComponent.injectPlayerActivityFields(this)

        /* Build MainViewModel */
        val mMainViewModel = ViewModelProvider(this, mMainViewModelFactory)
                .get(MainViewModel::class.java)

        /* Get Video object from DB and check for nullness */
        val video = mMainViewModel.getVideoById(mVideoId)
        if (video == null) {
            Log.d(TAG, "onCreate: Video is null")
            showErrorMessage()
            return
        }

        fillViews(video)
        initYoutubePlayerView()

        /* If activity is created in landscape orientation, then force to switch landscape mode. */
        val orientation = baseContext.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            youtube_player_view.enterFullScreen()
        }

        btn_share.setOnClickListener {
            val videoLink = VIDEO_BASE_LINK + mVideoId
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, videoLink)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {
        if (youtube_player_view.isFullScreen())
            youtube_player_view.exitFullScreen()
        else
            super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            youtube_player_view.enterFullScreen()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            youtube_player_view.exitFullScreen()
        }
    }

    private fun initYoutubePlayerView() {
        lifecycle.addObserver(youtube_player_view)
        youtube_player_view.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadOrCueVideo(lifecycle, mVideoId, 0f)
                addFullScreenListenerToPlayer()
            }
        })
    }

    private fun addFullScreenListenerToPlayer() {
        youtube_player_view.addFullScreenListener(object : YouTubePlayerFullScreenListener {
            override fun onYouTubePlayerEnterFullScreen() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                mFullScreenHelper.enterFullScreen()
            }

            override fun onYouTubePlayerExitFullScreen() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                mFullScreenHelper.exitFullScreen()
            }
        })
    }

    private fun fillViews(video: Video) {
        val publishedAt = video.getLocalDateTimePublishedAt()
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        val videoInfo = String.format("%s \u2022 %s", video.channelTitle, publishedAt)
        tv_video_title.text = video.title
        tv_video_info.text = videoInfo
        tv_video_description.text = video.description
        Glide.with(this)
                .load(video.channelAvatar)
                .centerCrop()
                .circleCrop()
                .placeholder(ColorDrawable(Color.GRAY))
                .into(iv_channel_avatar)
    }

    private fun showErrorMessage() {
        hideViews()
        tv_error_message.show()
    }

    private fun hideViews() {
        iv_channel_avatar.hide()
        btn_share.hide()
        tv_video_title.hide()
        tv_video_info.hide()
        tv_video_description.hide()
        youtube_player_view.hide()
    }

    companion object {
        const val VIDEO_BASE_LINK = "https://www.youtube.com/watch?v="
        const val KEY_EXTRA_VIDEO_ID = "video-id"
        private const val TAG = "PlayerActivity"
    }
}