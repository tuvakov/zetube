package com.tuvakov.zetube.android.ui.channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tuvakov.zetube.android.databinding.FragmentChannelBinding

class ChannelFragment : Fragment() {

    private lateinit var binding: FragmentChannelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChannelBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}