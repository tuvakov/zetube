package com.tuvakov.zetube.android.ui.channel

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.ui.channel.fragments.ChannelDescriptionFragment
import com.tuvakov.zetube.android.ui.channel.fragments.ChannelVideosFragment

private val TAB_TITLES = arrayOf(
        R.string.tab_text_videos,
        R.string.tab_text_description
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
        FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            ChannelVideosFragment.newInstance()
        } else {
            ChannelDescriptionFragment.newInstance()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }
}