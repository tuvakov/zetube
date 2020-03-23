package com.tuvakov.zetube.android.ui.feed;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.tuvakov.zetube.android.data.Video;
import com.tuvakov.zetube.android.repository.SubscriptionRepo;
import com.tuvakov.zetube.android.repository.VideoRepo;

import java.util.List;

public class MainViewModel extends ViewModel {

    private SubscriptionRepo mSubscriptionRepo;
    private VideoRepo mVideoRepo;

    private boolean mIsSyncing = false;
    private Video cachedVideo;

    MainViewModel(SubscriptionRepo subscriptionRepo, VideoRepo videoRepo) {
        mSubscriptionRepo = subscriptionRepo;
        mVideoRepo = videoRepo;
    }

    LiveData<List<Video>> getVideoFeed() {
        return mVideoRepo.getAllVideos();
    }

    void deleteAllVideos() {
        mVideoRepo.deleteAll();
    }

    void deleteAllSubscriptions() {
        mSubscriptionRepo.deleteAll();
    }

    public Video getVideoById(String getVideoId) {
        if (cachedVideo != null && cachedVideo.getId().equals(getVideoId)) {
            return cachedVideo;
        }
        cachedVideo = mVideoRepo.getVideoById(getVideoId);
        return cachedVideo;
    }

    void setIsSyncing(boolean flag) {
        mIsSyncing = flag;
    }

    boolean isSyncing() {
        return mIsSyncing;
    }
}