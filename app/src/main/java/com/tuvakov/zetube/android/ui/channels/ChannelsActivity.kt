package com.tuvakov.zetube.android.ui.channels

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.ZeYouBe
import kotlinx.android.synthetic.main.layout_video_feed.*
import javax.inject.Inject

class ChannelsActivity : AppCompatActivity() {

    @Inject
    lateinit var mChannelsViewModelFactory: ChannelsViewModelFactory

    private lateinit var mChannelsViewModel: ChannelsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_video_feed)

        // Inject dependencies
        (application as ZeYouBe).appComponent.injectChannelsActivity(this)

        /* Setup action bar title */
        supportActionBar?.setTitle(R.string.nav_item_channels)

        // Show loading message
        showMessage(R.string.msg_info_loading, View.VISIBLE)

        /* Setup view model */
        mChannelsViewModel = ViewModelProvider(this, mChannelsViewModelFactory)
                .get(ChannelsViewModel::class.java)

        /* Setup adapter and recycler view */
        val adapter = ChannelsAdapter()
        rv_video_feed.adapter = adapter

        /* Set span count */
        val orientation = resources.configuration.orientation
        val spanCount = if (orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 6
        rv_video_feed.layoutManager = GridLayoutManager(this, spanCount)

        /* Get LiveData and setup observer */
        mChannelsViewModel.getChannels().observe(this, Observer {
            Log.d("Channels: channel list", it.toString())
            if (it.isEmpty()) {
                showMessage(R.string.msg_info_empty_channel_list, View.GONE)
                return@Observer
            }
            showRecyclerView()
            adapter.submitList(it)
        })
    }

    private fun showRecyclerView() {
        progress_circular.visibility = View.GONE
        tv_feedback.visibility = View.GONE
        rv_video_feed.visibility = View.VISIBLE
    }

    private fun showMessage(messageStringId: Int, progressBarVisibility: Int) {
        rv_video_feed.visibility = View.GONE
        progress_circular.visibility = progressBarVisibility
        tv_feedback.visibility = View.VISIBLE
        tv_feedback.setText(messageStringId)
    }
}