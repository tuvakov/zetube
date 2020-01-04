package com.tuvakov.zeyoube.android;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.tuvakov.zeyoube.android.data.Video;
import com.tuvakov.zeyoube.android.repository.SubscriptionRepo;
import com.tuvakov.zeyoube.android.repository.VideoRepo;

import java.util.List;

class MainViewModel extends ViewModel {

    private SubscriptionRepo mSubscriptionRepo;
    private VideoRepo mVideoRepo;
    private boolean mIsSyncing = false;

    private static final String TAG = "MainViewModel";

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

    void setIsSyncing(boolean flag) {
        mIsSyncing = flag;
    }

    boolean isSyncing() {
        return mIsSyncing;
    }
}