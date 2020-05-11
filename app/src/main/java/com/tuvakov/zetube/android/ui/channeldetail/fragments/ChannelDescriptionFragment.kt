package com.tuvakov.zetube.android.ui.channeldetail.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.ZeTubeApp
import com.tuvakov.zetube.android.data.Subscription
import com.tuvakov.zetube.android.ui.channeldetail.ChannelDetailViewModel
import com.tuvakov.zetube.android.ui.channeldetail.Error
import com.tuvakov.zetube.android.ui.channeldetail.Success
import com.tuvakov.zetube.android.ui.feed.ViewModelFactory
import com.tuvakov.zetube.android.utils.hide
import com.tuvakov.zetube.android.utils.show

class ChannelDescriptionFragment : Fragment() {

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ChannelDetailViewModel

    /* Views */
    private lateinit var avatar: ImageView
    private lateinit var title: TextView
    private lateinit var description: TextView
    private lateinit var message: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_channel_description, container, false)
        avatar = v.findViewById(R.id.iv_channel_avatar)
        title = v.findViewById(R.id.tv_channel_title)
        description = v.findViewById(R.id.tv_channel_description)
        message = v.findViewById(R.id.tv_feedback)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let { it ->
            val app = (it.application as ZeTubeApp)
            viewModelFactory = app.appComponent.viewModelFactory()
            viewModel = ViewModelProvider(it, viewModelFactory).get(ChannelDetailViewModel::class.java)

            viewModel.channel.observe(viewLifecycleOwner, Observer { sub ->
                populateView(sub)
            })

            viewModel.channelState.observe(viewLifecycleOwner, Observer {
                when (it) {
                    Success -> {
                        message.hide()
                    }
                    is Error -> {
                        message.setText(R.string.msg_error_generic)
                        message.show()
                    }
                }
            })
        }
    }

    private fun populateView(sub: Subscription) {
        title.text = sub.title
        description.text = sub.description
        Glide.with(this)
                .load(sub.thumbnail)
                .centerCrop()
                .circleCrop()
                .placeholder(ColorDrawable(Color.GRAY))
                .into(avatar)
    }

    companion object {
        fun newInstance() = ChannelDescriptionFragment()
    }
}
