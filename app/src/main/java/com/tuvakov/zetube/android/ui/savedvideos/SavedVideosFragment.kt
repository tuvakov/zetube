package com.tuvakov.zetube.android.ui.savedvideos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tuvakov.zetube.android.databinding.FragmentChannelsBinding

class SavedVideosFragment : Fragment() {

    private lateinit var binding: FragmentChannelsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChannelsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}