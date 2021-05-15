package com.tuvakov.zetube.android.ui.player

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.databinding.ActivityPlayerBinding
import com.tuvakov.zetube.android.ui.videos.MainViewModel
import com.tuvakov.zetube.android.utils.FullScreenHelper
import com.tuvakov.zetube.android.utils.hide
import com.tuvakov.zetube.android.utils.show
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// TODO: Check if device is offline
@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    private val mFullScreenHelper = FullScreenHelper(this)
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var mVideoId: String
    private lateinit var binding: ActivityPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Check for intent and video id extra */
        if (intent == null || !intent.hasExtra(KEY_EXTRA_VIDEO_ID)) {
            Log.d(TAG, "onCreate: Intent is null or no extra.")
            showErrorMessage()
            return
        }

        // Get video id extra
        mVideoId = intent.getStringExtra(KEY_EXTRA_VIDEO_ID) ?: ""

        /* Get Video object from DB and check for nullness */
        val video = mainViewModel.getVideoById(mVideoId)

        fillViews(video)
        initYoutubePlayerView()

        /* If activity is created in landscape orientation, then force to switch landscape mode. */
        val orientation = baseContext.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.youtubePlayerView.enterFullScreen()
        }

        binding.btnShare.setOnClickListener {
            val videoLink = VIDEO_BASE_LINK + mVideoId
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, videoLink)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }

        binding.btnSave.setOnClickListener {
            video.isSaved = !video.isSaved
            val msg = if (video.isSaved) {
                R.string.msg_info_video_saved
            } else {
                R.string.msg_info_video_removed
            }
            mainViewModel.updateVideo(video)
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            setSaveButton(video)
        }
    }

    override fun onBackPressed() {
        if (binding.youtubePlayerView.isFullScreen())
            binding.youtubePlayerView.exitFullScreen()
        else
            super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.youtubePlayerView.enterFullScreen()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.youtubePlayerView.exitFullScreen()
        }
    }

    private fun initYoutubePlayerView() {
        lifecycle.addObserver(binding.youtubePlayerView)
        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadOrCueVideo(lifecycle, mVideoId, 0f)
                addFullScreenListenerToPlayer()
            }
        })
    }

    private fun addFullScreenListenerToPlayer() {
        binding.youtubePlayerView.addFullScreenListener(object : YouTubePlayerFullScreenListener {
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
        binding.tvVideoTitle.text = video.title
        binding.tvVideoInfo.text = videoInfo
        binding.tvVideoDescription.text = video.description
        setSaveButton(video)
        Glide.with(this)
                .load(video.channelAvatar)
                .centerCrop()
                .circleCrop()
                .placeholder(ColorDrawable(Color.GRAY))
                .into(binding.ivChannelAvatar)
    }

    private fun setSaveButton(video: Video) {
        if (video.isSaved) {
            binding.btnSave.setText(R.string.btn_txt_remove)
            val icon = AppCompatResources.getDrawable(this, R.drawable.ic_remove_white_24dp)
            (binding.btnSave as MaterialButton).icon = icon
        } else {
            binding.btnSave.setText(R.string.btn_txt_save)
            val icon = AppCompatResources.getDrawable(this, R.drawable.ic_add_white_24dp)
            (binding.btnSave as MaterialButton).icon = icon
        }
    }

    private fun showErrorMessage() {
        hideViews()
        binding.tvErrorMessage.show()
    }

    private fun hideViews() {
        with(binding) {
            ivChannelAvatar.hide()
            btnSave.hide()
            btnShare.hide()
            tvVideoTitle.hide()
            tvVideoInfo.hide()
            tvVideoDescription.hide()
            youtubePlayerView.hide()
        }
    }

    companion object {
        const val VIDEO_BASE_LINK = "https://www.youtube.com/watch?v="
        const val KEY_EXTRA_VIDEO_ID = "video-id"
        private const val TAG = "PlayerActivity"
    }
}