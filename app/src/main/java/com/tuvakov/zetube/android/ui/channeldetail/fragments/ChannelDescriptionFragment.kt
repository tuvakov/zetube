package com.tuvakov.zetube.android.ui.channeldetail.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.ZeTubeApp
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.databinding.FragmentChannelDescriptionBinding
import com.tuvakov.zetube.android.ui.channeldetail.ChannelDetailViewModel
import com.tuvakov.zetube.android.ui.channeldetail.Error
import com.tuvakov.zetube.android.ui.channeldetail.Success
import com.tuvakov.zetube.android.ui.feed.ViewModelFactory
import com.tuvakov.zetube.android.utils.hide
import com.tuvakov.zetube.android.utils.show

class ChannelDescriptionFragment : Fragment() {

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ChannelDetailViewModel

    private lateinit var binding: FragmentChannelDescriptionBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentChannelDescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let { it ->
            val app = (it.application as ZeTubeApp)
            viewModelFactory = app.appComponent.viewModelFactory()
            viewModel = ViewModelProvider(it, viewModelFactory).get(ChannelDetailViewModel::class.java)

            viewModel.channel.observe(viewLifecycleOwner, { sub -> populateView(sub) })

            viewModel.channelState.observe(viewLifecycleOwner, {
                when (it) {
                    Success -> {
                        binding.tvFeedback.hide()
                    }
                    is Error -> {
                        binding.tvFeedback.setText(R.string.msg_error_generic)
                        binding.tvFeedback.show()
                    }
                }
            })
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
