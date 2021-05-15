package com.tuvakov.zetube.android.ui.videos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tuvakov.zetube.android.databinding.FragmentVideosBinding

class VideosFragment : Fragment() {

    private lateinit var binding: FragmentVideosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideosBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}