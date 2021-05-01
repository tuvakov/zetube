package com.tuvakov.zetube.android.ui.channels

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.databinding.LayoutVideoFeedBinding
import com.tuvakov.zetube.android.utils.hide
import com.tuvakov.zetube.android.utils.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChannelsActivity : AppCompatActivity() {

    private val mChannelsViewModel: ChannelsViewModel by viewModels()
    private lateinit var binding: LayoutVideoFeedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutVideoFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Setup action bar title */
        supportActionBar?.setTitle(R.string.nav_item_channels)

        // Show loading message
        showMessage(R.string.msg_info_loading, View.VISIBLE)

        /* Setup adapter and recycler view */
        val adapter = ChannelsAdapter()
        binding.rvVideoFeed.adapter = adapter

        /* Set span count */
        val orientation = resources.configuration.orientation
        val spanCount = if (orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 6
        binding.rvVideoFeed.layoutManager = GridLayoutManager(this, spanCount)

        /* Get LiveData and setup observer */
        mChannelsViewModel.getChannels().observe(this, Observer {
            if (it.isEmpty()) {
                showMessage(R.string.msg_info_empty_channel_list, View.GONE)
                return@Observer
            }
            showRecyclerView()
            adapter.submitList(it)
        })
    }

    private fun showRecyclerView() {
        with(binding) {
            progressCircular.hide()
            tvFeedback.hide()
            rvVideoFeed.show()
        }
    }

    private fun showMessage(messageStringId: Int, progressBarVisibility: Int) {
        with(binding) {
            rvVideoFeed.show()
            tvFeedback.hide()
            tvFeedback.setText(messageStringId)
            progressCircular.visibility = progressBarVisibility
        }
    }
}