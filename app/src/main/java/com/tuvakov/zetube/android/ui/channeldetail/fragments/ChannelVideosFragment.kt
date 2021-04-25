package com.tuvakov.zetube.android.ui.channeldetail.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.ZeTubeApp
import com.tuvakov.zetube.android.databinding.FragmentChannelVideosBinding
import com.tuvakov.zetube.android.ui.channeldetail.*
import com.tuvakov.zetube.android.ui.feed.VideoFeedAdapter
import com.tuvakov.zetube.android.ui.feed.ViewModelFactory
import com.tuvakov.zetube.android.utils.hide
import com.tuvakov.zetube.android.utils.show


class ChannelVideosFragment : Fragment() {

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ChannelDetailViewModel
    private val adapter = VideoFeedAdapter()

    private lateinit var binding: FragmentChannelVideosBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentChannelVideosBinding.inflate(layoutInflater, container, false)
        binding.container.rvVideoFeed.adapter = adapter
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireActivity().let { it ->
            val app = (it.application as ZeTubeApp)
            viewModelFactory = app.appComponent.viewModelFactory()
            viewModel = ViewModelProvider(it, viewModelFactory).get(ChannelDetailViewModel::class.java)

            viewModel.channelVideos.observe(viewLifecycleOwner, { videos -> adapter.submitList(videos) })

            viewModel.videosState.observe(viewLifecycleOwner, {
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
        with(binding.container) {
            progressCircular.hide()
            tvFeedback.hide()
            rvVideoFeed.show()
        }
    }

    private fun showMessage(stringId: Int, showProgressBar: Boolean = false) {
        with(binding.container) {
            rvVideoFeed.hide()
            tvFeedback.setText(stringId)
            progressCircular.isVisible = showProgressBar
        }
    }

    companion object {
        fun newInstance() = ChannelVideosFragment()
    }
}
