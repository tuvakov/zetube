package com.tuvakov.zetube.android.ui.channeldetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.ZeTubeApp
import com.tuvakov.zetube.android.databinding.ActivityChannelDetailBinding

class ChannelDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChannelDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.lToolbar.toolbar)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.layout_tabs)
        tabs.setupWithViewPager(viewPager)

        /* Setup view model */
        val viewModelFactory = (application as ZeTubeApp).appComponent.viewModelFactory()
        val viewModel = ViewModelProvider(this, viewModelFactory)
                .get(ChannelDetailViewModel::class.java)

        if (intent == null || !intent.hasExtra(EXTRA_CHANNEL_ID)) {
            viewModel.setErrorOnAllStates()
            return
        }

        /* Set toolbar title */
        intent.getStringExtra(EXTRA_CHANNEL_TITLE)?.let {
            supportActionBar?.title = it
        }

        /* Fetch data */
        if (savedInstanceState == null) {
            intent.getStringExtra(EXTRA_CHANNEL_ID)?.let {
                viewModel.fetchSubscription(it)
                viewModel.fetchVideosForSubscription(it)
            }
        }
    }

    companion object {
        const val EXTRA_CHANNEL_ID = "channel-detail-channel-id"
        const val EXTRA_CHANNEL_TITLE = "channel-detail-channel-title"
    }
}
