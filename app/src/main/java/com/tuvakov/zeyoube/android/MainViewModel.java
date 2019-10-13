package com.tuvakov.zeyoube.android;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.tuvakov.zeyoube.android.data.Subscription;
import com.tuvakov.zeyoube.android.data.Video;
import com.tuvakov.zeyoube.android.repository.SubscriptionRepo;
import com.tuvakov.zeyoube.android.repository.VideoRepo;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private SubscriptionRepo mSubscriptionRepo;
    private VideoRepo mVideoRepo;

    public MainViewModel(@NonNull Application application) {
        super(application);

        mSubscriptionRepo = new SubscriptionRepo(application);
        mVideoRepo = new VideoRepo(application);
    }

    public LiveData<List<Video>> getVideoFeed() {
        return mVideoRepo.getAllVideos();
    }

    public LiveData<List<Subscription>> getSubscriptions() {
        return mSubscriptionRepo.getAllSubscriptions();
    }

    public void deleteAllVideos() {
        mVideoRepo.deleteAll();
    }

    public void insertDummyVideos() {
        mVideoRepo.insertDummies();
    }
}