package com.tuvakov.zetube.android.ui.channel.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.databinding.FragmentChannelVideosBinding
import com.tuvakov.zetube.android.ui.channel.*
import com.tuvakov.zetube.android.ui.videos.VideoFeedAdapter
import com.tuvakov.zetube.android.utils.hide
import com.tuvakov.zetube.android.utils.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChannelVideosFragment : Fragment() {

    private val viewModel: ChannelDetailViewModel by activityViewModels()
    private val adapter = VideoFeedAdapter()

    private lateinit var binding: FragmentChannelVideosBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentChannelVideosBinding.inflate(layoutInflater, container, false)
        binding.container.rvVideoFeed.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
