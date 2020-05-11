package com.tuvakov.zetube.android.ui.channeldetail.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.ZeTubeApp
import com.tuvakov.zetube.android.ui.channeldetail.*
import com.tuvakov.zetube.android.ui.feed.VideoFeedAdapter
import com.tuvakov.zetube.android.ui.feed.ViewModelFactory
import com.tuvakov.zetube.android.utils.hide
import com.tuvakov.zetube.android.utils.show


class ChannelVideosFragment : Fragment() {

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ChannelDetailViewModel
    private val adapter = VideoFeedAdapter()

    /* Views */
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvFeedback: TextView
    private lateinit var progressBar: ProgressBar


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_channel_videos, container, false)
        tvFeedback = v.findViewById(R.id.tv_feedback)
        progressBar = v.findViewById(R.id.progress_circular)
        recyclerView = v.findViewById(R.id.rv_video_feed)
        recyclerView.adapter = adapter
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let { it ->
            val app = (it.application as ZeTubeApp)
            viewModelFactory = app.appComponent.viewModelFactory()
            viewModel = ViewModelProvider(it, viewModelFactory).get(ChannelDetailViewModel::class.java)

            viewModel.channelVideos.observe(viewLifecycleOwner, Observer { videos ->
                adapter.submitList(videos)
            })

            viewModel.videosState.observe(viewLifecycleOwner, Observer {
                when (it) {
                    Success -> {
                        showRecyclerView()
                    }
                    InProgress -> {
                        showMessage(R.string.msg_info_loading, showProgressBar = true)
                    }
                    EmptyList -> {
                        showMessage(R.string.msg_info_empty_result_set, showProgressBar = false)
                    }
                    is Error -> {
                        showMessage(R.string.msg_error_generic, showProgressBar = false)
                    }
                }
            })
        }
    }

    /* TODO: This logic is repeated in several places. Try to generalize. */
    private fun showRecyclerView() {
        progressBar.hide()
        tvFeedback.hide()
        recyclerView.show()
    }

    private fun showMessage(stringId: Int, showProgressBar: Boolean = false) {
        recyclerView.hide()
        tvFeedback.setText(stringId)
        if (showProgressBar) {
            progressBar.show()
        } else {
            progressBar.hide()
        }
    }

    companion object {
        fun newInstance() = ChannelVideosFragment()
    }
}
