package com.tuvakov.zetube.android.ui.channel.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.databinding.FragmentChannelDescriptionBinding
import com.tuvakov.zetube.android.ui.channel.ChannelDetailViewModel
import com.tuvakov.zetube.android.ui.channel.Error
import com.tuvakov.zetube.android.ui.channel.Success
import com.tuvakov.zetube.android.utils.hide
import com.tuvakov.zetube.android.utils.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChannelDescriptionFragment : Fragment() {

    private val viewModel: ChannelDetailViewModel by activityViewModels()

    private lateinit var binding: FragmentChannelDescriptionBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentChannelDescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.channel.observe(viewLifecycleOwner, { sub -> populateView(sub) })

        viewModel.channelState.observe(viewLifecycleOwner) {
            when (it) {
                Success -> {
                    binding.tvFeedback.hide()
                }
                is Error -> {
                    binding.tvFeedback.setText(R.string.msg_error_generic)
                    binding.tvFeedback.show()
                }
                else -> {}
            }
        }
    }

    private fun populateView(sub: Subscription) {
        with(binding) {
            tvChannelTitle.text = sub.title
            tvChannelDescription.text = sub.description
            Glide.with(this@ChannelDescriptionFragment)
                    .load(sub.thumbnail)
                    .centerCrop()
                    .circleCrop()
                    .placeholder(ColorDrawable(Color.GRAY))
                    .into(ivChannelAvatar)
        }
    }

    companion object {
        fun newInstance() = ChannelDescriptionFragment()
    }
}
