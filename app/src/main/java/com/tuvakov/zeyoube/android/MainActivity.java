package com.tuvakov.zeyoube.android;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mMainViewModel;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.rv_video_feed);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        VideoFeedAdapter videoFeedAdapter = new VideoFeedAdapter();
        recyclerView.setAdapter(videoFeedAdapter);

        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mMainViewModel.getVideoFeed().observe(this, videos -> {
            if (videos == null) return;
            Collections.sort(videos, (o1, o2) -> o1.getLocalDateTimePublishedAt()
                    .compareTo(o2.getLocalDateTimePublishedAt())
            );
            videoFeedAdapter.submitList(videos);
        });

        mMainViewModel.getSubscriptions().observe(this, videoFeedAdapter::setChannels);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_all_videos:
                mMainViewModel.deleteAllVideos();
                return true;
            case R.id.menu_item_insert_dummy_videos:
                mMainViewModel.insertDummyVideos();
                return true;
            default:
                return true;
        }
    }
}