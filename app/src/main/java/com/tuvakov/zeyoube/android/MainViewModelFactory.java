package com.tuvakov.zeyoube.android;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.tuvakov.zeyoube.android.repository.SubscriptionRepo;
import com.tuvakov.zeyoube.android.repository.VideoRepo;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MainViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private SubscriptionRepo mSubscriptionRepo;
    private VideoRepo mVideoRepo;

    @Inject
    MainViewModelFactory(SubscriptionRepo subscriptionRepo, VideoRepo videoRepo) {
        this.mSubscriptionRepo = subscriptionRepo;
        this.mVideoRepo = videoRepo;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MainViewModel(mSubscriptionRepo, mVideoRepo);
    }
}